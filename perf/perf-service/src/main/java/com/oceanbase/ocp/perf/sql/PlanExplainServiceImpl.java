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

package com.oceanbase.ocp.perf.sql;

import static com.oceanbase.ocp.common.util.time.TimeUtils.atUtcStartOfDay;
import static com.oceanbase.ocp.common.util.time.TimeUtils.toUs;
import static com.oceanbase.ocp.perf.sql.util.SqlStatUtils.isTimeoutException;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.oceanbase.ocp.core.exception.IllegalArgumentException;
import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.obsdk.operator.sql.entity.PlanExplainEntity;
import com.oceanbase.ocp.obsdk.operator.sql.model.PlanUid;
import com.oceanbase.ocp.obsdk.operator.sql.param.QueryExplainByPlan;
import com.oceanbase.ocp.perf.sql.dao.PlanExplainAccess;
import com.oceanbase.ocp.perf.sql.model.PlanExplain;
import com.oceanbase.ocp.perf.sql.model.PlanOperation;
import com.oceanbase.ocp.perf.sql.model.PlanStatDetail;
import com.oceanbase.ocp.perf.sql.param.QueryPlanExplainParam;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PlanExplainServiceImpl implements PlanExplainService {

    @Autowired
    private SqlStatProperties propertyManager;

    @Autowired
    private SqlStatContextServiceImpl sqlStatContextService;

    @Autowired
    private PlanExplainAccess planExplainDao;

    private String env(QueryPlanExplainParam param, SqlStatDataContext context) {
        return String.format("clusterId=%d(%s:%d), tenantId=%d(%d)", param.clusterId,
                context.clusterName, context.obClusterId, param.tenantId, context.obTenantId);
    }

    @Override
    public PlanExplain getExplain(QueryPlanExplainParam param) {
        List<PlanExplainEntity> data = getExplainEntity(param);
        return createBuilder().compute(data).build();
    }

    @Override
    public List<PlanExplainEntity> getExplainEntity(QueryPlanExplainParam param) {
        SqlStatDataContext context = sqlStatContextService.getContext(param.tenantId);
        List<PlanExplainEntity> data = new ArrayList<>();
        try {
            data = planExplainDao.queryByPlan(buildQuery(param, context, param.uid));
        } catch (Exception e) {
            if (isTimeoutException(e)) {
                throw new IllegalArgumentException(ErrorCodes.COMMON_TIME_RANGE_TOO_LARGE);
            }
            throw e;
        }
        if (log.isDebugEnabled()) {
            log.debug("Plan explain query ret: {}", data.size());
        }
        return data.stream()
                .collect(Collectors.groupingBy(p -> p.planId))
                .values()
                .stream()
                .limit(1)
                .findAny()
                .orElse(Collections.emptyList());
    }

    @Override
    public PlanExplain getPlanExplain(SqlStatDataContext context, PlanStatDetail plan) {
        QueryPlanExplainParam param = QueryPlanExplainParam.builder()
                .startTime(plan.getFirstLoadTime())
                .endTime(plan.getFirstLoadTime().plusMinutes(10L))
                .planUid(plan.uid)
                .tenantId(context.obTenantId)
                .build();
        return getExplain(param);
    }

    private QueryExplainByPlan buildQuery(QueryPlanExplainParam param, SqlStatDataContext context, PlanUid uid) {
        if (log.isDebugEnabled()) {
            log.debug("Plan explain query build: {}, {}", env(param, context), uid.info());
        }
        return QueryExplainByPlan.builder()
                .timeout(propertyManager.getQueryTimeout())
                .startTimeUs(toUs(atUtcStartOfDay(param.interval.start)))
                .endTimeUs(toUs(atUtcStartOfDay(param.interval.end).plus(1L, ChronoUnit.DAYS)))
                .obClusterId(context.obClusterId)
                .clusterName(context.clusterName)
                .obTenantId(context.obTenantId)
                .obServerId(uid.obServerId)
                .planId(uid.planId)
                .firstLoadTimeUs(uid.firstLoadTimeUs)
                .build();
    }

    private OperationTreeBuilder createBuilder() {
        return new OperationTreeBuilder();
    }

    @RequiredArgsConstructor
    private static final class PlanOperationWrapper {

        private final PlanOperation operation;
        private final int indent;
        private List<PlanOperationWrapper> children;

        private boolean isLosted() {
            return indent == -1;
        }

        private boolean isChildOf(PlanOperationWrapper other) {
            return indent == other.indent + 1;
        }

        private boolean isSiblingOf(PlanOperationWrapper other) {
            return indent == other.indent;
        }

        private void addChildren(PlanOperationWrapper other) {
            if (children == null) {
                children = new ArrayList<>();
            }
            children.add(other);
        }

        private PlanOperation build(boolean self) {
            if (children == null || children.isEmpty() || self) {
                operation.children = null;
                return operation;
            }
            operation.children = children.stream().map(it -> it.build(false)).collect(Collectors.toList());
            return operation;
        }
    }

    private final class OperationTreeBuilder {

        private final Deque<PlanOperationWrapper> stack = new LinkedList<>();
        private final List<PlanOperationWrapper> lost = new ArrayList<>();

        private OperationTreeBuilder compute(List<PlanExplainEntity> entities) {
            if (entities == null || entities.isEmpty()) {
                return this;
            }
            entities.sort(Comparator.comparingLong(it -> it.planId));
            entities.forEach(it -> {
                PlanOperationWrapper wrapper = mapToModel(it);
                if (wrapper.isLosted()) {
                    lost.add(wrapper);
                } else {
                    for (;;) {
                        if (stack.isEmpty()) {
                            stack.addLast(wrapper);
                            break;
                        }
                        PlanOperationWrapper peek = stack.peekLast();
                        if (wrapper.isChildOf(peek)) {
                            peek.addChildren(wrapper);
                            stack.addLast(wrapper);
                            break;
                        } else if (wrapper.isSiblingOf(peek)) {
                            if (stack.size() == 1) {
                                lost.add(wrapper);
                                break;
                            } else {
                                stack.pollLast();
                            }
                        } else {
                            stack.pollLast();
                        }
                    }
                }
            });
            return this;
        }

        private PlanExplain build() {
            List<PlanOperation> rootOperations = new ArrayList<>();
            if (stack.peekFirst() != null) {
                rootOperations.add(stack.peekFirst().build(false));
            }
            for (PlanOperationWrapper it : lost) {
                rootOperations.add(it.build(true));
            }
            PlanExplain explain = new PlanExplain();
            explain.setRootOperations(rootOperations);
            return explain;
        }
    }

    private PlanOperationWrapper mapToModel(PlanExplainEntity entity) {
        PlanOperation operation = new PlanOperation();
        int indent = indent(entity.operator);
        operation.operator = operator(entity.operator, indent);
        operation.objectName = entity.objectName;
        operation.rows = entity.rows;
        operation.cost = entity.cost;
        operation.property = entity.property;
        return new PlanOperationWrapper(operation, indent);
    }

    static int indent(String value) {
        if (isBlank(value)) {
            return -1;
        }
        int i = 0;
        while (i < value.length() && Character.isWhitespace(value.charAt(i))) {
            i++;
        }
        return i;
    }

    static String operator(String value, int indent) {
        if (indent == -1) {
            return null;
        } else if (isBlank(value)) {
            return null;
        }
        return value.substring(indent);
    }
}
