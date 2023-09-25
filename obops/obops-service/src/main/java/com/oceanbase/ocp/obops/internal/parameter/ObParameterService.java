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

package com.oceanbase.ocp.obops.internal.parameter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.obsdk.ObAccessorFactory;
import com.oceanbase.ocp.core.obsdk.ObOperatorFactory;
import com.oceanbase.ocp.core.util.ExceptionUtils;
import com.oceanbase.ocp.obops.internal.parameter.model.ObParameterValue;
import com.oceanbase.ocp.obops.parameter.model.ObParameterInfo;
import com.oceanbase.ocp.obops.parameter.model.ObParameterStaticInfo;
import com.oceanbase.ocp.obsdk.accessor.ObAccessor;
import com.oceanbase.ocp.obsdk.accessor.ParameterAccessor;
import com.oceanbase.ocp.obsdk.operator.ParameterOperator;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObParameter;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObTenantParameter;
import com.oceanbase.ocp.obsdk.operator.parameter.model.SetObParameter;

@Service
public class ObParameterService {

    @Autowired
    private ObOperatorFactory obOperatorFactory;

    @Autowired
    private ObAccessorFactory obAccessorFactory;

    private static final String[] READONLY_PARAMETER_ARRAY = new String[] {
            "cluster",
            "cluster_id",
            "data_dir",
            "devname",
            "mysql_port",
            "rpc_port",
    };

    private static final String[] CAPACITY_PARAMETER_ARRAY = new String[] {
            "__easy_memory_limit",
            "__hash_area_size",
            "__sort_area_size",
            "__temporary_file_io_area_size",
            "_chunk_row_store_mem_limit",
            "_hash_area_size",
            "_hidden_sys_tenant_memory",
            "_max_trx_size",
            "_memory_large_chunk_cache_size",
            "_ob_checkpoint_space_limit",
            "_ob_replay_memory_limit",
            "_parallel_min_message_pool",
            "_private_buffer_size",
            "_sort_area_size",
            "_temporary_file_io_area_size",
            "_tenant_max_trx_size",
            "backup_data_file_size",
            "backup_net_limit",
            "cache_wash_threshold",
            "clog_ofs_usage_limit_size",
            "clog_usage_limit_size",
            "datafile_size",
            "dtl_buffer_size",
            "log_archive_batch_buffer_limit",
            "log_disk_size",
            "memory_chunk_cache_size",
            "memory_limit",
            "memory_reserved",
            "multiblock_read_gap_size",
            "multiblock_read_size",
            "plan_cache_high_watermark",
            "plan_cache_low_watermark",
            "px_task_size",
            "range_optimizer_max_mem_size",
            "rebuild_replica_data_lag_threshold",
            "rootservice_memory_limit",
            "slog_size",
            "sql_audit_memory_limit",
            "sql_work_area",
            "stack_size",
            "syslog_io_bandwidth_limit",
            "system_memory",
            "tablet_size",
            "tenant_disk_max_size",
    };

    public static final Set<String> READONLY_PARAMETERS = Sets.newHashSet(READONLY_PARAMETER_ARRAY);

    public static final Set<String> CAPACITY_PARAMETERS = Sets.newHashSet(CAPACITY_PARAMETER_ARRAY);

    public ObParameterStaticInfo getObParameterStaticInfo(String name) {
        return ObParameterStaticInfo.builder()
                .name(name)
                .readonly(READONLY_PARAMETERS.contains(name))
                .build();
    }

    public List<ObParameterInfo> listTenantParameterInfo() {
        ParameterOperator operator = obOperatorFactory.createParameterOperator();

        List<ObTenantParameter> obTenantParameters = operator.listTenantParameters();

        ListMultimap<String, ObTenantParameter> map = Multimaps.index(obTenantParameters, ObParameter::getName);
        return map.keySet().stream()
                .map(name -> ObParameterInfo.tenant(map.get(name).get(0), getObParameterStaticInfo(name)))
                .collect(Collectors.toList());
    }

    public void validateCapacityValue(String name, String value) {
        if (CAPACITY_PARAMETERS.contains(name)) {
            boolean pureNumberValue = NumberUtils.isParsable(value);
            ExceptionUtils.require(!pureNumberValue, ErrorCodes.OB_PARAMETER_VALUE_UNIT_REQUIRED, name, value);
        }
    }

    public Optional<ObParameterValue> getHiddenClusterParameter(String parameterName) {
        ParameterOperator parameterOperator = obOperatorFactory.createParameterOperator();
        List<ObParameter> obParameters = parameterOperator.getHiddenClusterParameter(parameterName);
        return ObParameterValue.fromObParameters(obParameters);
    }

    public void setTenantParameters(Long tenantId, Map<String, String> parameters) {
        List<SetObParameter> setObParameters = parameters.entrySet().stream()
                .map(e -> SetObParameter.plain(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
        setTenantParameters(tenantId, setObParameters);
    }

    public void setTenantParameters(Long tenantId, List<SetObParameter> parameters) {
        ObAccessor obAccessor = obAccessorFactory.createObAccessor(tenantId);
        ParameterAccessor parameterAccessor = obAccessor.parameter();
        for (SetObParameter parameter : parameters) {
            parameterAccessor.setParameter(parameter);
        }
    }
}
