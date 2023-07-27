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
package com.oceanbase.ocp.obops.internal.host;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.oceanbase.ocp.core.agent.ObAgentEntity;
import com.oceanbase.ocp.core.constants.ObAgentOperation;
import com.oceanbase.ocp.core.executor.AgentExecutorFactory;
import com.oceanbase.ocp.core.util.ExceptionUtils;
import com.oceanbase.ocp.executor.executor.AgentExecutor;
import com.oceanbase.ocp.executor.model.agent.AgentServiceStatus;
import com.oceanbase.ocp.executor.model.agent.AgentState;
import com.oceanbase.ocp.executor.model.agent.AgentStatus;
import com.oceanbase.ocp.monitor.ExporterService;
import com.oceanbase.ocp.obops.host.ObAgentService;
import com.oceanbase.ocp.obops.host.model.ExporterType;
import com.oceanbase.ocp.obops.host.model.ObAgent;
import com.oceanbase.ocp.obops.host.model.ObAgentDetail;
import com.oceanbase.ocp.obops.host.model.ObAgentProcess;
import com.oceanbase.ocp.obops.host.param.InitAgentParam;
import com.oceanbase.ocp.obops.internal.host.repository.ObAgentRepository;
import com.oceanbase.ocp.obops.internal.host.subtask.FinishObAgentOperationTask;
import com.oceanbase.ocp.obops.internal.host.subtask.PrepareRestartObAgentTask;
import com.oceanbase.ocp.obops.internal.host.subtask.RestartObAgentTask;
import com.oceanbase.ocp.task.TaskInstanceManager;
import com.oceanbase.ocp.task.constants.ContextKey;
import com.oceanbase.ocp.task.model.Argument;
import com.oceanbase.ocp.task.model.TaskInstance;
import com.oceanbase.ocp.task.runtime.Template;
import com.oceanbase.ocp.task.runtime.TemplateBuilder;

@Service
public class ObAgentServiceImpl implements ObAgentService {

    @Autowired
    private ObAgentRepository agentRepository;

    @Autowired
    private HostProperties hostProperties;

    @Autowired
    private ExporterService exporterService;

    @Autowired
    private AgentExecutorFactory agentExecutorFactory;

    @Autowired
    private TaskInstanceManager taskInstanceManager;

    private static final String EXPORTER_URL_TEMPLATE = "http://%s:%d%s";
    private static final String SLASH = "/";

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void initAgent(List<InitAgentParam> initAgentParams) {
        initAgentRecord(initAgentParams);

        initExporters(initAgentParams);
    }

    @Override
    public Optional<ObAgentDetail> findById(Long obAgentId) {
        Optional<ObAgentEntity> opt = agentRepository.findById(obAgentId);
        if (!opt.isPresent()) {
            return Optional.empty();
        }
        ObAgentEntity entity = opt.get();
        return getAgentDetail(entity);
    }

    @Override
    public Optional<ObAgentDetail> findDetailByIpAndObSvrPort(String ip, Integer obSvrPort) {
        Optional<ObAgentEntity> opt = agentRepository.findByIpAndObServerSvrPort(ip, obSvrPort);
        if (!opt.isPresent()) {
            return Optional.empty();
        }
        ObAgentEntity entity = opt.get();
        return getAgentDetail(entity);
    }

    @Override
    public TaskInstance restartObAgent(String ip, Integer obSvrPort) {
        Optional<ObAgentEntity> opt = agentRepository.findByIpAndObServerSvrPort(ip, obSvrPort);
        ExceptionUtils.notFound(opt.isPresent(), "ob-agent", "ip=" + ip + ",obSvrPort=" + obSvrPort);
        Template template = new TemplateBuilder().name("Restart OB agent")
                .andThen(new PrepareRestartObAgentTask())
                .andThen(new RestartObAgentTask())
                .andThen(new FinishObAgentOperationTask())
                .build();
        Argument args = new Argument();
        args.putLong(ContextKey.OB_AGENT_ID, opt.get().getId());
        return taskInstanceManager.submitManualTask(template, args);
    }

    @Override
    public void updateOperation(Long obAgentId, ObAgentOperation operation) {
        agentRepository.updateAgentOperation(obAgentId, operation);
    }

    @Override
    public AgentExecutor getAgentExecutor(String ip, int obSvrPort) {
        Optional<ObAgentEntity> opt = agentRepository.findByIpAndObServerSvrPort(ip, obSvrPort);
        ExceptionUtils.notFound(opt.isPresent(), "obagent", ip);
        ObAgentEntity entity = opt.get();
        return agentExecutorFactory.create(entity.getIp(), entity.getMgrPort());
    }

    private Optional<ObAgentDetail> getAgentDetail(ObAgentEntity entity) {
        AgentExecutor agentExecutor = agentExecutorFactory.create(entity.getIp(), entity.getMgrPort());
        AgentStatus agentStatus = agentExecutor.agentStatus();
        return Optional.of(toDetail(entity, agentStatus));
    }

    private void initAgentRecord(List<InitAgentParam> infoLst) {
        agentRepository.deleteAllInBatch();
        List<ObAgentEntity> agentEntities = new ArrayList<>();
        for (InitAgentParam param : infoLst) {
            ObAgentEntity agentEntity = ObAgentEntity.builder()
                    .ip(param.getIp())
                    .obServerSvrPort(param.getObServerSvrPort())
                    .mgrPort(param.getMgrPort())
                    .monPort(param.getMonPort())
                    .operation(ObAgentOperation.EXECUTE)
                    .build();
            agentEntities.add(agentEntity);
        }
        agentRepository.saveAll(agentEntities);
    }

    private void initExporters(List<InitAgentParam> infoLst) {
        List<String> exporters = new ArrayList<>();
        for (InitAgentParam param : infoLst) {
            if (param.getObServerSvrPort() == null || param.getObServerSvrPort() <= 0) {
                continue;
            }
            List<String> paths = getExporterPaths(ExporterType.OB);
            if (paths.isEmpty()) {
                continue;
            }
            for (String path : paths) {
                exporters.add(getExporterUrl(param.getIp(), param.getMonPort(), path));
            }
        }
        exporterService.registerExporters(exporters);
    }

    private List<String> getExporterPaths(ExporterType type) {
        List<String> exporterPaths = new ArrayList<>(hostProperties.getHostExporters());
        if (type == null) {
            return exporterPaths;
        }
        switch (type) {
            case OB:
                exporterPaths.addAll(hostProperties.getObServerExporters());
                break;
            case HOST:
            default:
                break;
        }
        return exporterPaths;
    }

    private String getExporterUrl(String ip, int port, String path) {
        if (!path.startsWith(SLASH)) {
            path = SLASH + path;
        }
        return String.format(EXPORTER_URL_TEMPLATE, ip, port, path);
    }

    private ObAgent toObAgent(ObAgentEntity entity) {
        if (entity == null) {
            return null;
        }
        return ObAgent.builder()
                .id(entity.getId())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .ip(entity.getIp())
                .mgrPort(entity.getMgrPort())
                .monPort(entity.getMonPort())
                .obServerSvrPort(entity.getObServerSvrPort())
                .build();
    }

    private ObAgentDetail toDetail(ObAgentEntity entity, AgentStatus agentStatus) {
        if (entity == null) {
            return null;
        }
        List<ObAgentProcess> agentProcesses = new ArrayList<>();
        ObAgentProcess agentdProcess = getAgentdProcess(agentStatus);
        if (agentdProcess != null) {
            for (Map.Entry<String, AgentServiceStatus> entry : agentStatus.getServices().entrySet()) {
                agentProcesses.add(ObAgentProcess.builder()
                        .name(entry.getKey())
                        .state(entry.getValue().getState())
                        .version(entry.getValue().getVersion())
                        .pid(entry.getValue().getPid())
                        .startAtMillis(entry.getValue().getStartAt() / 1_000_000)
                        .build());
            }
        }
        return ObAgentDetail.builder()
                .id(entity.getId())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .ip(entity.getIp())
                .mgrPort(entity.getMgrPort())
                .monPort(entity.getMonPort())
                .obServerSvrPort(entity.getObServerSvrPort())
                .obAgentProcesses(agentProcesses)
                .operation(entity.getOperation())
                .ready(agentStatus != null && agentStatus.isReady())
                .version(agentStatus.getVersion())
                .build();
    }

    private ObAgentProcess getAgentdProcess(AgentStatus agentStatus) {
        if (agentStatus == null || agentStatus.getPid() <= 0) {
            return null;
        }
        return ObAgentProcess.builder()
                .name("ob_agentd")
                .pid(agentStatus.getPid())
                .state(AgentState.RUNNING)
                .version(agentStatus.getVersion())
                .build();
    }

}
