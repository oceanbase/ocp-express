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

import static com.oceanbase.ocp.core.i18n.ErrorCodes.PERF_SQL_TEXT_NOT_EXIT;
import static com.oceanbase.ocp.perf.sql.SqlAuditRawStatServiceImpl.FAKE_DB_NAME;
import static com.oceanbase.ocp.perf.sql.util.SqlStatUtils.intern;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.oceanbase.ocp.obsdk.operator.sql.entity.SqlTextEntity;
import com.oceanbase.ocp.obsdk.operator.sql.param.QuerySqlTextAny;
import com.oceanbase.ocp.perf.sql.dao.SqlTextAccess;
import com.oceanbase.ocp.perf.sql.model.SqlText;
import com.oceanbase.ocp.perf.sql.param.QuerySqlTextParam;
import com.oceanbase.ocp.perf.sql.util.SqlStatMapper;
import com.oceanbase.ocp.perf.sql.util.SqlStatUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SqlTextServiceImpl implements SqlTextService {

    @Autowired
    private SqlTextAccess sqlTextAccess;

    @Autowired
    private SqlStatContextServiceImpl contextService;

    @Autowired
    private SqlStatProperties properties;

    @Autowired
    private SqlStatContextServiceImpl sqlStatContextService;

    private static final int MAX_STATEMENT_LENGTH_IN_OB = 4096;

    @Override
    public SqlText getAny(QuerySqlTextParam param) {
        SqlStatDataContext context = contextService.getContext(param.tenantId);
        SqlText sqlText = getAny(context, param);
        if (sqlText == null) {
            throw PERF_SQL_TEXT_NOT_EXIT.exception();
        }
        return sqlText;
    }

    @Override
    public SqlText getAny(SqlStatDataContext context, QuerySqlTextParam param) {
        QuerySqlTextAny query = buildQuerySqlText(param, context);
        Long obDbId = 0L;
        if (param.getDbName().equals(FAKE_DB_NAME)) {
            obDbId = null;
        } else {
            obDbId = sqlStatContextService.getObDatabaseId(param.getTenantId(), param.getDbName());
        }
        query.setObDbId(obDbId);
        SqlTextEntity entity = sqlTextAccess.querySqlTextAny(query);
        if (entity == null) {
            log.info("query sqlText from view is null, sqlId:" + param.getSqlId());
            return null;
        }
        SqlText sqlText = SqlStatMapper.mapToModel(entity);
        String sql;
        if (isNotBlank(sqlText.getStatement()) && sqlText.getStatement().length() < MAX_STATEMENT_LENGTH_IN_OB) {
            sql = sqlText.getStatement();
        } else {
            sql = intern(SqlStatUtils.safeParameterizeSqlLiteral(context.getMode(), sqlText.getFulltext(),
                    properties.isDisplayParseFailedSql()));
            if ((StringUtils.isBlank(sql) || sql.equals(sqlText.getFulltext()))
                    && StringUtils.isNotBlank(sqlText.getStatement())) {
                log.info("parse sql Literal failed sqlId:" + param.getSqlId());
                sql = sqlText.getStatement();
            }
            sqlText.setStatement(sql);
        }
        sqlText.setFulltext(sql);
        sqlText.setDbName(entity.getDbName());
        return sqlText;
    }

    private QuerySqlTextAny buildQuerySqlText(QuerySqlTextParam param, SqlStatDataContext context) {
        return QuerySqlTextAny.builder()
                .timeout(properties.getQueryTimeout())
                .sqlId(param.sqlId)
                .obTenantId(context.obTenantId)
                .build();
    }
}
