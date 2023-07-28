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

package com.oceanbase.ocp.obops.host;

import java.io.IOException;

import org.springframework.core.io.Resource;

import com.oceanbase.ocp.obops.host.model.QueryLogResult;
import com.oceanbase.ocp.obops.host.param.DownloadLogRequest;
import com.oceanbase.ocp.obops.host.param.QueryLogRequest;

public interface QueryLogService {

    QueryLogResult queryLog(QueryLogRequest queryLogRequest) throws IOException;

    Resource downloadLog(DownloadLogRequest downloadAllLogReq) throws IOException;

}