/*
 * Copyright (c) 2023 OceanBase
 * OCP Express is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package com.oceanbase.ocp.monitor.calculate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import com.oceanbase.ocp.core.exception.OcpException;
import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.util.BeanUtils;
import com.oceanbase.ocp.monitor.calculate.model.OcpPrometheusQueryMeta;
import com.oceanbase.ocp.monitor.model.OcpPrometheusData;
import com.oceanbase.ocp.monitor.model.OcpPrometheusQueryParam;
import com.oceanbase.ocp.monitor.model.OcpPrometheusQueryResult;
import com.oceanbase.ocp.monitor.model.OcpPrometheusScanResp;
import com.oceanbase.ocp.monitor.model.metric.OcpPrometheusMeasurement;
import com.oceanbase.ocp.monitor.query.util.MetricQueryScanner;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class OcpPrometheusAstNode {

    private OcpPrometheusAstNodeTypeEnum nodeType;
    private OcpPrometheusAstNode leftNode = null;
    private OcpPrometheusAstNode rightNode = null;
    private OcpPrometheusAstFunctionEnum nodeFunction;
    private double value;
    private List<String> gbLabels;
    private OcpPrometheusQueryMeta queryMeta;
    private LinkedList<OcpPrometheusQueryResult> tmpResultList;
    private Future<List<OcpPrometheusScanResp>> resultFuture;

    public OcpPrometheusAstNode() {}

    private void prepare(long startTime, long endTime, long step) {
        switch (this.nodeType) {
            case COMPARE:
            case MATHOP: {
                this.leftNode.prepare(startTime, endTime, step);
                this.rightNode.prepare(startTime, endTime, step);
                break;
            }
            case FUNCTION:
            case AGG_FUNCTION: {
                this.leftNode.prepare(startTime, endTime, step);
                break;
            }
            case SCALAR: {
                this.tmpResultList = new LinkedList<>();
                this.tmpResultList.add(new OcpPrometheusQueryResult(this.value));
                break;
            }
            case ITEM: {
                this.tmpResultList = new LinkedList<>();
                this.resultFuture = this.scanData(startTime, endTime, step);
                break;
            }
            default: {
                throw new RuntimeException("Get Unexpected NodeType");
            }
        }
    }

    private void afterPrepare() {
        switch (this.nodeType) {
            case COMPARE:
            case MATHOP: {
                this.leftNode.afterPrepare();
                this.rightNode.afterPrepare();
                break;
            }
            case FUNCTION:
            case AGG_FUNCTION: {
                this.leftNode.afterPrepare();
                break;
            }
            case SCALAR: {
                break;
            }
            case ITEM: {
                List<OcpPrometheusScanResp> res = doGet();
                log.debug("scan data size:{}", res.size());
                for (OcpPrometheusScanResp item : res) {
                    OcpPrometheusQueryResult tResult = new OcpPrometheusQueryResult(item.getMeasurementParse(),
                            this.queryMeta.getInterval(), item.getData());
                    this.tmpResultList.add(tResult);
                }
                break;
            }
            default: {
                throw new RuntimeException("Get Unexpected NodeType");
            }
        }
    }

    private List<OcpPrometheusScanResp> doGet() {
        if (resultFuture == null) {
            return new ArrayList<>();
        }
        List<OcpPrometheusScanResp> resultList;
        try {
            resultList = resultFuture.get();
        } catch (Exception e) {
            log.warn("Scan metrics failed, message={}", e.getMessage());
            throw new OcpException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodes.COMMON_UNEXPECTED, e);
        }
        Map<String, List<OcpPrometheusScanResp>> groupedData =
                resultList.stream()
                        .filter(ocpPrometheusScanResp -> ocpPrometheusScanResp.getMeasurement() != null)
                        .collect(Collectors.groupingBy(OcpPrometheusScanResp::getMeasurement));
        resultList = new ArrayList<>();
        groupedData.values().forEach(resultList::addAll);
        return resultList;
    }

    private Future<List<OcpPrometheusScanResp>> scanData(long startTime, long endTime, long step) {
        long qStartTime = startTime - step;
        OcpPrometheusQueryParam param = new OcpPrometheusQueryParam();
        param.setMetric(this.queryMeta.getMetric());
        param.setLabels(this.queryMeta.getLabels());
        param.setStartTime(qStartTime);
        param.setEndTime(endTime);
        param.setStep(step);
        log.debug("scan data with param {}", param);
        return BeanUtils.getBean(MetricQueryScanner.class).scan(param);
    }

    public List<OcpPrometheusQueryResult> eval(long startTime, long endTime, long step) throws Exception {
        this.prepare(startTime, endTime, step);
        this.afterPrepare();
        Map<String, OcpPrometheusQueryResult> resultMap = new HashMap<>();
        long alignStartTime = startTime % step == 0 ? startTime : startTime + step - startTime % step;
        for (long ts = alignStartTime; ts <= endTime; ts += step) {
            LinkedList<OcpPrometheusQueryResult> resultList = this.evalTsData(ts);
            String k;
            for (OcpPrometheusQueryResult tResult : resultList) {
                if (tResult.getIsScalar()) {
                    k = "";
                } else {
                    k = tResult.getMeasurement().getLabelStr();
                }
                if (!resultMap.containsKey(k)) {
                    if (tResult.getIsScalar()) {
                        resultMap.put(k, new OcpPrometheusQueryResult(tResult.getValue()));
                    } else {
                        LinkedList<OcpPrometheusData> vData = new LinkedList<>();
                        OcpPrometheusQueryResult vResult =
                                new OcpPrometheusQueryResult(tResult.getMeasurement(), tResult.getInterval(), vData);
                        resultMap.put(k, vResult);
                    }
                }
                OcpPrometheusQueryResult v = resultMap.get(k);
                LinkedList<OcpPrometheusData> vDataList = v.getData();
                if (!tResult.getIsScalar()) {
                    for (OcpPrometheusData tData : tResult.getData()) {
                        if (vDataList.isEmpty() || tData.getTimestamp() > vDataList.getLast().getTimestamp()) {
                            tData.setValue((double) (long) (tData.getValue() * 1000) / 1000);
                            vDataList.add(tData);
                        }
                    }
                }
            }
        }
        return new LinkedList<>(resultMap.values());
    }

    private LinkedList<OcpPrometheusQueryResult> handleEmptyResult(LinkedList<OcpPrometheusQueryResult> resultList) {
        if (!resultList.isEmpty()) {
            return resultList;
        }
        switch (this.nodeFunction) {
            case ADD_L_FILL_0_IF_ABSENT:
            case ADD_R_FILL_0_IF_ABSENT:
            case ADD_FILL_0_IF_ABSENT:
                LinkedList<OcpPrometheusQueryResult> results = new LinkedList<>();
                results.add(new OcpPrometheusQueryResult(0.0D));
                return results;
            case ADD:
            case SUB:
            case MUL:
            case DIV:
            default:
                return resultList;
        }
    }

    private double doOp(double l, double r) {
        switch (this.nodeFunction) {
            case ADD:
            case ADD_L_FILL_0_IF_ABSENT:
            case ADD_R_FILL_0_IF_ABSENT:
            case ADD_FILL_0_IF_ABSENT: {
                return l + r;
            }
            case SUB: {
                return l - r;
            }
            case MUL: {
                return l * r;
            }
            case DIV: {
                return r == 0 ? Double.NaN : l / r;
            }
            default: {
                throw new RuntimeException("Get Unexpected Function");
            }
        }
    }

    private LinkedList<OcpPrometheusData> doFunctionCall(List<OcpPrometheusData> dataList, long ts) {
        LinkedList<OcpPrometheusData> aDataList = new LinkedList<>();
        if (!dataList.isEmpty()) {
            switch (this.nodeFunction) {
                case ABS: {
                    for (OcpPrometheusData data : dataList) {
                        aDataList.add(new OcpPrometheusData(ts, Math.abs(data.getValue())));
                    }
                    break;
                }
                case ROUND: {
                    for (OcpPrometheusData data : dataList) {
                        aDataList.add(new OcpPrometheusData(ts, (double) Math.round(data.getValue())));
                    }
                    break;
                }
                default: {
                    throw new RuntimeException("Get Unexpected Agg Function");
                }
            }
        }
        return aDataList;
    }

    private LinkedList<OcpPrometheusQueryResult> doLabelAgg(List<OcpPrometheusQueryResult> resultList, long ts) {
        Map<String, OcpPrometheusAggData> aggMap = new HashMap<>();
        LinkedList<OcpPrometheusQueryResult> retList = new LinkedList<>();
        List<String> labels = this.getGbLabels();
        Collections.sort(labels);
        for (OcpPrometheusQueryResult tResult : resultList) {
            OcpPrometheusMeasurement measurement = tResult.getMeasurement();
            Map<String, String> labelMap = measurement.getLabels();
            Map<String, String> newLabelMap = new HashMap<>();
            LinkedList<OcpPrometheusData> dataList = tResult.getData();
            List<String> labelList = new ArrayList<>();
            for (String label : labels) {
                if (labelMap.containsKey(label)) {
                    String labelValue = labelMap.get(label);
                    labelValue = StringUtils.isEmpty(labelValue) ? "" : labelValue;
                    labelList.add(label + "=" + labelValue);
                    newLabelMap.put(label, labelValue);
                } else {
                    labelList.add(label + "=");
                    newLabelMap.put(label, "");
                }
            }
            if (CollectionUtils.isNotEmpty(labelList)) {
                String labelStr = String.join("|", labelList);
                if (!aggMap.containsKey(labelStr)) {
                    aggMap.put(labelStr, new OcpPrometheusAggData(new OcpPrometheusMeasurement(labelStr, newLabelMap)));
                }
                OcpPrometheusAggData aggData = aggMap.get(labelStr);
                for (OcpPrometheusData data : dataList) {
                    if (data.getTimestamp() == ts) {
                        aggData.addValue(data);
                        break;
                    }
                }
            }
        }
        for (OcpPrometheusAggData aggData : aggMap.values()) {
            LinkedList<OcpPrometheusData> aDataList = new LinkedList<>();
            switch (this.nodeFunction) {
                case SUM: {
                    if (aggData.hasData()) {
                        aDataList.add(new OcpPrometheusData(ts, aggData.getSum()));
                    }
                    break;
                }
                case COUNT: {
                    if (aggData.hasData()) {
                        aDataList.add(new OcpPrometheusData(ts, aggData.getCount()));
                    }
                    break;
                }
                case MAX: {
                    if (aggData.hasData()) {
                        aDataList.add(new OcpPrometheusData(ts, aggData.getMax()));
                    }
                    break;
                }
                case MIN: {
                    if (aggData.hasData()) {
                        aDataList.add(new OcpPrometheusData(ts, aggData.getMin()));
                    }
                    break;
                }
                case AVG: {
                    if (aggData.hasData()) {
                        aDataList.add(new OcpPrometheusData(ts, aggData.getAvg()));
                    }
                    break;
                }
                default: {
                    throw new RuntimeException("Get Unexpected Agg Function");
                }
            }
            retList.add(new OcpPrometheusQueryResult(aggData.getMeasurement(), 0, aDataList));
        }
        return retList;
    }

    private LinkedList<OcpPrometheusData> doIntervalAgg(List<OcpPrometheusData> dataList, long ts, long interval) {
        LinkedList<OcpPrometheusData> aDataList = new LinkedList<>();
        OcpPrometheusAggData aggData = new OcpPrometheusAggData();
        if (!dataList.isEmpty()) {
            OcpPrometheusData first = null;
            OcpPrometheusData last = null;
            for (OcpPrometheusData data : dataList) {
                if (data.getTimestamp() >= ts - interval && data.getTimestamp() <= ts) {
                    if (first == null) {
                        first = data;
                    }
                    last = data;
                    aggData.addValue(data);
                }
            }
            switch (this.nodeFunction) {
                case SUM: {
                    if (aggData.hasData()) {
                        aDataList.add(new OcpPrometheusData(ts, aggData.getSum()));
                    }
                    break;
                }
                case COUNT: {
                    if (aggData.hasData()) {
                        aDataList.add(new OcpPrometheusData(ts, aggData.getCount()));
                    }
                    break;
                }
                case MAX: {
                    if (aggData.hasData()) {
                        aDataList.add(new OcpPrometheusData(ts, aggData.getMax()));
                    }
                    break;
                }
                case MIN: {
                    if (aggData.hasData()) {
                        aDataList.add(new OcpPrometheusData(ts, aggData.getMin()));
                    }
                    break;
                }
                case AVG: {
                    if (aggData.hasData()) {
                        aDataList.add(new OcpPrometheusData(ts, aggData.getAvg()));
                    }
                    break;
                }
                case RATE: {
                    if (first != null && last != null) {
                        long firstTs = first.getTimestamp();
                        long lastTs = last.getTimestamp();
                        double firstValue = first.getValue();
                        double lastValue = last.getValue();
                        if (lastTs > firstTs) {
                            aDataList.add(new OcpPrometheusData(ts, (lastValue - firstValue) / (lastTs - firstTs)));
                        }
                    }
                    break;
                }
                case DELTA: {
                    if (first != null && last != null) {
                        long firstTs = first.getTimestamp();
                        long lastTs = last.getTimestamp();
                        double firstValue = first.getValue();
                        double lastValue = last.getValue();
                        if (lastTs > firstTs) {
                            aDataList.add(new OcpPrometheusData(ts,
                                    (lastValue - firstValue) / (lastTs - firstTs) * interval));
                        }
                    }
                    break;
                }
                default: {
                    throw new RuntimeException("Get Unexpected Agg Function");
                }
            }
        }
        return aDataList;
    }

    private boolean doCompare(double l, double r) {
        double epsilon = 0.000001;
        switch (this.nodeFunction) {
            case LT: {
                return l < r;
            }
            case GT: {
                return l > r;
            }
            case GE: {
                return l >= r;
            }
            case LE: {
                return l <= r;
            }
            case EQ: {
                return ((l - epsilon) <= r && (l + epsilon) >= r);
            }
            case NE: {
                return Double.compare(l, 4) != 0;
            }
            default: {
                throw new RuntimeException("Get Unexpected Compare Function");
            }
        }
    }

    private LinkedList<OcpPrometheusQueryResult> evalTsData(long ts) {
        switch (this.nodeType) {
            case SCALAR: {
                return this.tmpResultList;
            }
            case ITEM: {
                for (OcpPrometheusQueryResult tResult : this.tmpResultList) {
                    tResult.prepareForTs(ts);
                }
                return this.tmpResultList;
            }
            case COMPARE: {
                this.tmpResultList = new LinkedList<>();
                Map<String, OcpPrometheusQueryResult> resultMap = new HashMap<>();
                LinkedList<OcpPrometheusQueryResult> lResultList = this.leftNode.evalTsData(ts);
                LinkedList<OcpPrometheusQueryResult> rResultList = this.rightNode.evalTsData(ts);
                if (!lResultList.isEmpty() && !rResultList.isEmpty()) {
                    boolean isLeftScalar = lResultList.getFirst().getIsScalar();
                    boolean isRightScalar = rResultList.getFirst().getIsScalar();
                    if (!isLeftScalar) {
                        if (isRightScalar) {
                            double rightScalar = rResultList.getFirst().getValue();
                            for (OcpPrometheusQueryResult tResult : lResultList) {
                                LinkedList<OcpPrometheusData> resultData = new LinkedList<>();
                                LinkedList<OcpPrometheusData> lData = tResult.getData();
                                for (OcpPrometheusData d : lData) {
                                    if (this.doCompare(d.getValue(), rightScalar)) {
                                        resultData.add(d);
                                    }
                                }
                                this.tmpResultList.add(new OcpPrometheusQueryResult(tResult.getMeasurement(),
                                        tResult.getInterval(), resultData));
                            }
                        } else {
                            for (OcpPrometheusQueryResult tResult : lResultList) {
                                String k = tResult.getMeasurement().getLabelStr();
                                resultMap.put(k, tResult);
                            }
                            for (OcpPrometheusQueryResult tResult : rResultList) {
                                String k = tResult.getMeasurement().getLabelStr();
                                if (!resultMap.containsKey(k)) {
                                    continue;
                                }
                                LinkedList<OcpPrometheusData> resultData = new LinkedList<>();
                                LinkedList<OcpPrometheusData> rData = tResult.getData();
                                LinkedList<OcpPrometheusData> lData = resultMap.get(k).getData();
                                long rt = Long.MIN_VALUE;
                                Iterator<OcpPrometheusData> rIter = rData.iterator();
                                OcpPrometheusData rd = null;
                                for (OcpPrometheusData ld : lData) {
                                    long lt = ld.getTimestamp();
                                    while (rt < lt && rIter.hasNext()) {
                                        rd = rIter.next();
                                        rt = rd.getTimestamp();
                                    }
                                    if (lt == rt && rd != null) {
                                        if (this.doCompare(ld.getValue(), rd.getValue())) {
                                            resultData.add(ld);
                                        }
                                    }
                                }
                                this.tmpResultList.add(new OcpPrometheusQueryResult(tResult.getMeasurement(),
                                        tResult.getInterval(), resultData));
                            }
                        }
                    } else if (!isRightScalar) {
                        double leftScalar = lResultList.getFirst().getValue();
                        for (OcpPrometheusQueryResult tResult : rResultList) {
                            LinkedList<OcpPrometheusData> resultData = new LinkedList<>();
                            LinkedList<OcpPrometheusData> rData = tResult.getData();
                            for (OcpPrometheusData d : rData) {
                                if (this.doCompare(leftScalar, d.getValue())) {
                                    resultData.add(d);
                                }
                            }
                            this.tmpResultList.add(new OcpPrometheusQueryResult(tResult.getMeasurement(),
                                    tResult.getInterval(), resultData));
                        }
                    } else {
                        double lValue = lResultList.getFirst().getValue();
                        double rValue = rResultList.getFirst().getValue();
                        if (this.doCompare(lValue, rValue)) {
                            this.tmpResultList = lResultList;
                        }
                    }
                }
                return this.tmpResultList;
            }

            case MATHOP: {
                this.tmpResultList = new LinkedList<>();
                Map<String, OcpPrometheusQueryResult> resultMap = new HashMap<>();
                LinkedList<OcpPrometheusQueryResult> lResultList = handleEmptyResult(this.leftNode.evalTsData(ts));
                LinkedList<OcpPrometheusQueryResult> rResultList = handleEmptyResult(this.rightNode.evalTsData(ts));
                if (!lResultList.isEmpty() && !rResultList.isEmpty()) {
                    boolean isLeftScalar = lResultList.getFirst().getIsScalar();
                    boolean isRightScalar = rResultList.getFirst().getIsScalar();
                    if (!isLeftScalar) {
                        if (isRightScalar) {
                            double rightScalar = rResultList.getFirst().getValue();
                            for (OcpPrometheusQueryResult tResult : lResultList) {
                                LinkedList<OcpPrometheusData> resultData = new LinkedList<>();
                                LinkedList<OcpPrometheusData> lData = tResult.getData();
                                for (OcpPrometheusData d : lData) {
                                    resultData.add(new OcpPrometheusData(d.getTimestamp(),
                                            this.doOp(d.getValue(), rightScalar)));
                                }
                                this.tmpResultList.add(new OcpPrometheusQueryResult(tResult.getMeasurement(),
                                        tResult.getInterval(), resultData));
                            }
                        } else {
                            for (OcpPrometheusQueryResult tResult : lResultList) {
                                String k = tResult.getMeasurement().getLabelStr();
                                resultMap.put(k, tResult);
                            }
                            for (OcpPrometheusQueryResult tResult : rResultList) {
                                String k = tResult.getMeasurement().getLabelStr();
                                if (!resultMap.containsKey(k)) {
                                    continue;
                                }
                                LinkedList<OcpPrometheusData> lData = resultMap.get(k).getData();
                                LinkedList<OcpPrometheusData> rData = tResult.getData();
                                LinkedList<OcpPrometheusData> resultData = calMathOp(lData, rData);
                                this.tmpResultList.add(new OcpPrometheusQueryResult(tResult.getMeasurement(),
                                        tResult.getInterval(), resultData));
                            }
                        }
                    } else if (!isRightScalar) {
                        double leftScalar = lResultList.getFirst().getValue();
                        for (OcpPrometheusQueryResult tResult : rResultList) {
                            LinkedList<OcpPrometheusData> resultData = new LinkedList<>();
                            LinkedList<OcpPrometheusData> rData = tResult.getData();
                            for (OcpPrometheusData d : rData) {
                                resultData.add(
                                        new OcpPrometheusData(d.getTimestamp(), this.doOp(leftScalar, d.getValue())));
                            }
                            this.tmpResultList.add(new OcpPrometheusQueryResult(tResult.getMeasurement(),
                                    tResult.getInterval(), resultData));
                        }
                    } else {
                        double lValue = lResultList.getFirst().getValue();
                        double rValue = rResultList.getFirst().getValue();
                        this.tmpResultList.add(new OcpPrometheusQueryResult(this.doOp(lValue, rValue)));
                    }
                }
                return this.tmpResultList;
            }
            case FUNCTION: {
                this.tmpResultList = new LinkedList<>();
                LinkedList<OcpPrometheusQueryResult> lResultList = this.leftNode.evalTsData(ts);
                if (!lResultList.isEmpty() && !lResultList.getFirst().getIsScalar()) {
                    // agg function called on time interval;
                    for (OcpPrometheusQueryResult tResult : lResultList) {
                        LinkedList<OcpPrometheusData> aData;
                        if (lResultList.getFirst().getInterval() > 0) {
                            aData = this.doIntervalAgg(tResult.getData(), ts, tResult.getInterval());
                        } else {
                            aData = this.doFunctionCall(tResult.getData(), ts);
                        }
                        this.tmpResultList.add(new OcpPrometheusQueryResult(tResult.getMeasurement(), 0, aData));
                    }
                }
                return this.tmpResultList;
            }
            case AGG_FUNCTION: {
                this.tmpResultList = new LinkedList<>();
                LinkedList<OcpPrometheusQueryResult> lResultList = this.leftNode.evalTsData(ts);
                if (!lResultList.isEmpty() && !lResultList.getFirst().getIsScalar()) {
                    this.tmpResultList = this.doLabelAgg(lResultList, ts);
                }
                return this.tmpResultList;
            }
            default: {
                throw new RuntimeException("Get Unexpected NodeType");
            }
        }
    }

    private LinkedList<OcpPrometheusData> calMathOp(LinkedList<OcpPrometheusData> lData,
            LinkedList<OcpPrometheusData> rData) {
        LinkedList<OcpPrometheusData> resultData = new LinkedList<>();
        if (lData.isEmpty() && rData.isEmpty()) {
            return resultData;
        }
        boolean isSpecialAdd = this.nodeFunction == OcpPrometheusAstFunctionEnum.ADD_L_FILL_0_IF_ABSENT
                || this.nodeFunction == OcpPrometheusAstFunctionEnum.ADD_R_FILL_0_IF_ABSENT
                || this.nodeFunction == OcpPrometheusAstFunctionEnum.ADD_FILL_0_IF_ABSENT;
        if (isSpecialAdd) {
            return calSpecialAdd(lData, rData);
        }
        long rt = Long.MIN_VALUE;
        Iterator<OcpPrometheusData> rIter = rData.iterator();
        OcpPrometheusData rd = null;
        for (OcpPrometheusData ld : lData) {
            long lt = ld.getTimestamp();
            while (rt < lt && rIter.hasNext()) {
                rd = rIter.next();
                rt = rd.getTimestamp();
            }
            if (lt == rt && rd != null) {
                resultData.add(new OcpPrometheusData(lt, this.doOp(ld.getValue(), rd.getValue())));
            }
        }
        return resultData;
    }

    private LinkedList<OcpPrometheusData> calSpecialAdd(LinkedList<OcpPrometheusData> lData,
            LinkedList<OcpPrometheusData> rData) {
        LinkedList<OcpPrometheusData> resultData = new LinkedList<>();
        LinkedHashSet<Long> allTs = new LinkedHashSet<>();
        switch (this.nodeFunction) {
            case ADD_L_FILL_0_IF_ABSENT:
                allTs.addAll(rData.stream().map(OcpPrometheusData::getTimestamp).collect(Collectors.toList()));
                break;
            case ADD_R_FILL_0_IF_ABSENT:
                allTs.addAll(lData.stream().map(OcpPrometheusData::getTimestamp).collect(Collectors.toList()));
                break;
            case ADD_FILL_0_IF_ABSENT:
                allTs.addAll(lData.stream().map(OcpPrometheusData::getTimestamp).collect(Collectors.toList()));
                allTs.addAll(rData.stream().map(OcpPrometheusData::getTimestamp).collect(Collectors.toList()));
                break;
            default:
        }

        BiFunction<LinkedList<OcpPrometheusData>, Long, Double> getOrZero = (ocpPrometheusData, t) -> {
            double value = 0.0D;
            for (OcpPrometheusData data : ocpPrometheusData) {
                if (t.equals(data.getTimestamp())) {
                    value = data.getValue();
                    break;
                }
            }
            return value;
        };
        for (Long timestamp : allTs) {
            double value = this.doOp(getOrZero.apply(lData, timestamp), getOrZero.apply(rData, timestamp));
            resultData.add(new OcpPrometheusData(timestamp, value));
        }
        return resultData;
    }

}
