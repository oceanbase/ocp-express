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
package com.oceanbase.ocp.core.i18n;

import com.oceanbase.ocp.core.exception.AuthenticationException;
import com.oceanbase.ocp.core.exception.AuthorizationException;
import com.oceanbase.ocp.core.exception.BadRequestException;
import com.oceanbase.ocp.core.exception.ConflictException;
import com.oceanbase.ocp.core.exception.IllegalArgumentException;
import com.oceanbase.ocp.core.exception.NotFoundException;
import com.oceanbase.ocp.core.exception.OcpException;
import com.oceanbase.ocp.core.exception.UnexpectedException;

import lombok.Getter;

@Getter
public enum ErrorCodes implements ErrorCode {

    /**
     * common error code, range: [10000~10999]
     */
    COMMON_ILLEGAL_ARGUMENT(10001, Kind.ILLEGAL_ARGUMENT, "error.common.illegal.argument"),
    COMMON_NOT_FOUND(10002, Kind.NOT_FOUND, "error.common.not.found"),
    COMMON_START_END_TIME_NOT_VALID(10005, Kind.ILLEGAL_ARGUMENT, "error.common.start.end.time.not.valid"),
    COMMON_START_END_TIME_EITHER_FILLED_IN_ALL_OR_NONE(10006,
            "error.common.start.end.time.either.filled.in.all.or.none"),
    COMMON_TIME_RANGE_TOO_LARGE(10007, "error.common.time.range.too.large"),
    COMMON_INIT_ILLEGAL_ARGUMENT(10008, "error.common.init.illegal.argument"),
    COMMON_UNEXPECTED(10010, Kind.UNEXPECTED, "error.common.unexpected"),
    COMMON_OB_OPERATION_FAILED(10011, "error.common.ob.operation.failed"),

    /**
     * task error code, range: [20000, 20999]
     */
    TASK_NOT_EXISTS(20000, Kind.NOT_FOUND, "error.task.not.exists"),
    TASK_CYCLE_EXISTS(20001, "error.task.cycle.exists"),
    TASK_CONTEXT_ERROR(20002, Kind.UNEXPECTED, "error.task.context.error"),
    TASK_STATE_INVALID(20003, "error.task.state.invalid"),
    TASK_PROHIBIT_ROLLBACK(20004, "error.task.prohibit.rollback"),

    /**
     * iam error code, range: [30000~30999]
     */
    IAM_USER_NOT_AUTHENTICATED(30000, Kind.AUTHENTICATION, "error.iam.user.not.authenticated"),
    IAM_USER_ACCOUNT_DISABLED(30001, "error.iam.user.account.disabled"),
    IAM_USER_ACCOUNT_EXPIRED(30002, "error.iam.user.account.expired"),
    IAM_USER_ACCOUNT_LOCKED(30003, "error.iam.user.account.locked"),
    IAM_BAD_CREDENTIALS(30004, "error.iam.bad.credentials"),
    IAM_CREDENTIALS_EXPIRED(30005, "error.iam.credentials.expired"),
    IAM_USERNAME_NOT_FOUND(30006, "error.iam.username.not.found"),
    IAM_USER_LOGIN_BLOCKED(30007, "error.iam.user.login.blocked"),
    IAM_USERNAME_NOT_VALID(30008, "error.iam.username.not.valid"),
    IAM_PASSWORD_NOT_VALID(30009, "error.iam.password.not.valid"),
    IAM_USERNAME_NOT_ACTIVATED(30010, "error.iam.username.not.activated"),
    IAM_CURRENT_PASSWORD_NOT_MATCH(30011, "error.iam.current.password.not.match"),
    IAM_USER_ACCOUNT_PASSWORD_INACTIVE(30012, "error.iam.user.account.password.inactive"),
    IAM_NEW_PASSWORD_DUPLICATED(30013, "error.iam.new.password.duplicated"),
    IAM_OPERATION_NOT_PERMITTED(30014, "error.iam.operation.not.permitted"),
    IAM_SYSTEM_ACCOUNT_CANNOT_DELETE(30015, "error.iam.system.account.cannot.delete"),
    IAM_SYSTEM_PRESERVED_USERNAME_NOT_ALLOWED(30016, "error.iam.system.preserved.username.not.allowed"),
    IAM_SYSTEM_CHANGE_ROLE_NOT_ALLOWED(30017, "error.iam.system.change.role.not.allowed"),
    IAM_SYSTEM_ACCOUNT_CANNOT_LOCK(30018, "error.iam.system.account.cannot.lock"),
    IAM_ACCOUNT_CANNOT_DELETE_ONESELF(30019, "error.iam.account.cannot.delete.self"),
    IAM_SECURITY_FAILED_TO_DECRYPT_SENSITIVE_ATTRIBUTE(30020,
            "error.iam.security.failed.to.decrypt.sensitive.attribute"),

    /**
     * compute, range: [40000, 40999].
     */
    COMPUTE_HOST_AGENT_STATUS_NOT_VALID(40000, "error.compute.host.agent.status.not.valid"),
    COMPUTE_HOST_AGENT_TASK_TOKEN_NOT_SET(40001, Kind.UNEXPECTED, "error.compute.host.agent.task.token.not.set"),
    COMPUTE_HOST_AGENT_QUERY_LOG_TIMEOUT(40002, Kind.UNEXPECTED, "error.compute.host.agent.query.log.timeout"),

    // cluster, range: [50000, 50999]
    OB_CLUSTER_PASSWORD_EMPTY(50000, Kind.ILLEGAL_ARGUMENT, "error.ob.cluster.password.empty"),
    OB_CLUSTER_PRIMARY_ZONE_INVALID(50001, "error.ob.cluster.primary.zone.invalid"),
    OB_CLUSTER_OPS_NOT_ALLOWED(50002, "error.ob.cluster.ops.not.allowed"),
    OB_CLUSTER_IS_NOT_INIT(50003, Kind.UNEXPECTED, "error.ob.cluster.is.not.init"),
    OB_ZONE_NAME_NOT_FOUND(50004, "error.ob.zone.name.not.found"),
    OB_PARAMETER_VALUE_UNIT_REQUIRED(50005, Kind.ILLEGAL_ARGUMENT, "error.ob.parameter.value.unit.required"),

    // tenant, range: [60000, 60999]
    OB_TENANT_NAME_EXIST(60000, "error.ob.tenant.name.exist"),
    OB_TENANT_NAME_NOT_FOUND(60001, "error.ob.tenant.name.not.found"),
    OB_TENANT_ID_NOT_FOUND(60002, Kind.ILLEGAL_ARGUMENT, "error.ob.tenant.id.not.found"),
    OB_TENANT_STATUS_NOT_NORMAL(60003, Kind.UNEXPECTED, "error.ob.tenant.status.not.normal"),
    OB_TENANT_DELETE_NOT_ALLOWED(60004, "error.ob.tenant.delete.not.allowed"),
    OB_TENANT_SET_PASSWORD_FAILED(60005, "error.ob.tenant.set.password.failed"),
    OB_TENANT_REMAIN_ZONE_COUNT_INVALID(60006, "error.ob.tenant.remain.zone.count.invalid"),

    OB_TENANT_ZONE_NOT_VALID(60007, Kind.ILLEGAL_ARGUMENT, "error.ob.tenant.zone.not.valid"),
    OB_TENANT_ZONE_NOT_FOUND(60008, Kind.NOT_FOUND, "error.ob.tenant.zone.not.found"),
    OB_TENANT_RESOURCE_POOL_NOT_FOUND(60009, "error.ob.tenant.resource.pool.not.found"),
    OB_TENANT_UNIT_COUNT_NOT_VALID(60010, Kind.ILLEGAL_ARGUMENT, "error.ob.tenant.unit.count.not.valid"),
    OB_TENANT_UNIT_COUNT_EXCEED_ACTIVE_SERVER(60011, Kind.ILLEGAL_ARGUMENT,
            "error.ob.tenant.unit.count.exceed.active.server"),
    OB_TENANT_UNIT_COUNT_NOT_SAME_IN_EACH_ZONES(60012, Kind.ILLEGAL_ARGUMENT,
            "error.ob.tenant.unit.count.not.same.in.each.zones"),
    OB_TENANT_UNIT_COUNT_PARTIALLY_MODIFIED(60013, Kind.ILLEGAL_ARGUMENT,
            "error.ob.tenant.unit.count.partial.modified"),
    OB_TENANT_ALTER_LOCALITY_NOT_FINISHED(60014, "error.ob.tenant.alter.locality.not.finished"),
    OB_TENANT_SHRINK_POOL_NOT_FINISHED(60015, "error.ob.tenant.shrink.pool.not.finished"),
    OB_TENANT_INVALID_COMPACTION_STATUS(60016, Kind.UNEXPECTED, "error.ob.tenant.invalid.compaction.status"),
    OB_TENANT_CONNECT_FAILED(60017, Kind.UNEXPECTED, "error.ob.tenant.connect.failed"),
    OB_TENANT_CREDENTIAL_NOT_FOUND(60018, Kind.NOT_FOUND, "error.ob.tenant.credential.not.found"),
    OB_TENANT_METADB_OPERATION_RESTRICTED(60019, Kind.BAD_REQUEST, "error.ob.tenant.metadb.operation.restricted"),
    OB_TENANT_SYS_OPERATION_RESTRICTED(60029, Kind.BAD_REQUEST, "error.ob.tenant.sys.operation.restricted"),
    // database
    OB_DATABASE_NAME_EXISTS(60030, "error.ob.database.name.exists"),
    OB_DATABASE_NAME_NOT_FOUND(60040, Kind.ILLEGAL_ARGUMENT, "error.ob.database.name.not.found"),
    OB_DATABASE_MODIFY_PARAM_EMPTY(60041, "error.ob.database.modify.param.empty"),
    OB_DATABASE_OPERATION_NOT_ALLOW(60042, "error.ob.database.operation.not.allow"),
    OB_DATABASE_COLLATION_NOT_VALID(60043, "error.ob.database.collation.not.valid"),
    OB_DATABASE_ORACLE_MODE_NOT_SUPPORTED(60044, "error.ob.database.oracle.mode.not.supported"),
    // db user
    OB_USER_NAME_INVALID(60045, "error.ob.user.name.invalid"),
    OB_USER_NAME_EXISTS(60046, "error.ob.user.name.exists"),
    OB_USER_NAME_NOT_FOUND(60047, "error.ob.user.name.not.found"),
    OB_USER_OPERATION_NOT_ALLOW(60048, "error.ob.user.operation.not.allow"),
    OB_USER_ORACLE_MODE_NOT_SUPPORTED(60049, "error.ob.user.oracle.mode.not.supported"),
    OB_USER_DB_PRIVILEGE_INVALID(60050, Kind.ILLEGAL_ARGUMENT, "error.ob.user.db.privilege.invalid"),
    OB_USER_MYSQL_MODE_NOT_SUPPORTED(60051, "error.ob.user.mysql.mode.not.supported"),
    OB_USER_OBJECT_PRIVILEGE_INVALID(60052, "error.ob.user.object.privilege.invalid"),
    OB_USER_NAME_INVALID_FOR_ORACLE_MODE(60053, "error.ob.user.name.invalid.oracle"),
    OB_USER_PRIVILEGE_OPERATION_FAILED(60054, "error.ob.user.privilege.operation.failed"),

    // db role
    OB_ROLE_NAME_EXISTS(60055, "error.ob.role.name.exists"),
    OB_ROLE_NAME_NOT_FOUND(60056, "error.ob.role.name.not.found"),
    OB_ROLE_OBJECT_PRIVILEGE_INVALID(60057, "error.ob.role.object.privilege.invalid"),

    /**
     * PERF [70000, 70999]
     */
    PERF_SQL_SEARCH_ATTR_INVALID(70000, Kind.ILLEGAL_ARGUMENT, "error.perf.topsql.search.attr.invalid"),
    PERF_SQL_SEARCH_OP_INVALID(70001, Kind.ILLEGAL_ARGUMENT, "error.perf.topsql.search.op.invalid"),
    PERF_SQL_SEARCH_VALUE_INVALID(70002, Kind.ILLEGAL_ARGUMENT, "error.perf.topsql.search.value.invalid"),
    PERF_SQL_PLAN_UID_INVALID(70003, Kind.ILLEGAL_ARGUMENT, "error.perf.topsql.plan.uid.invalid"),
    PERF_SQL_EXCEED_MAX_TIME_RANGE(70004, Kind.ILLEGAL_ARGUMENT, "error.perf.topsql.exceed.max.time.range"),
    PERF_SQL_QUERY_TIMEOUT(70005, Kind.UNEXPECTED, "error.perf.topsql.query.timeout"),
    PERF_SQL_TEXT_NOT_EXIT(70006, Kind.NOT_FOUND, "error.perf.sql.text.not.found"),

    /**
     * SQL_DIAG [80000~80999]
     */
    SQL_DIAG_OUTLINE_NOT_EXISTS(80000, Kind.ILLEGAL_ARGUMENT, "error.sql.diag.outline.not.exists"),
    SQL_DIAG_OUTLINE_CONCURRENT_NUM_REQUIRED_LARGER_OR_EQUALS_ZERO(80001, Kind.ILLEGAL_ARGUMENT,
            "error.diag.outline.concurrent.num.required.larger.or.equals.zero"),
    SQL_DIAG_OUTLINE_DATABASE_NOT_EXISTS(80002, Kind.ILLEGAL_ARGUMENT, "error.diag.outline.database.not.exists"),
    SQL_DIAG_OUTLINE_NOT_SUPPORTED(80003, Kind.ILLEGAL_ARGUMENT, "error.sql.diag.outline.not.supported"),

    /**
     * Expression Language [90000, 90999]
     */
    EL_ALIAS_DUPLICATED(90000, Kind.UNEXPECTED, "error.el.alias.duplicated"),
    EL_ILLEGAL_ACCESS(90001, Kind.UNEXPECTED, "error.el.illegal.access"),
    EL_ILLEGAL_ARGUMENT(90002, Kind.UNEXPECTED, "error.el.illegal.argument"),
    EL_NO_SUCH_FIELD(90003, Kind.UNEXPECTED, "error.el.no.such.field"),
    EL_UNSUPPORTED_BINARY_OPERATOR(90004, Kind.UNEXPECTED, "error.el.unsupported.binary.operator"),
    EL_UNSUPPORTED_UNARY_OPERATOR(90005, Kind.UNEXPECTED, "error.el.unsupported.unary.operator"),
    EL_FUNCTION_CALL_EXCEPTION(90006, Kind.UNEXPECTED, "error.el.function.call.exception"),
    EL_NO_SUCH_CONTEXT_VALUE(90007, Kind.UNEXPECTED, "error.el.no.such.context.value"),
    EL_UNEXPECTED_EXCEPTION(90008, Kind.UNEXPECTED, "error.el.unexpected.exception"),
    EL_SYNTAX_ERROR(90009, Kind.UNEXPECTED, "error.el.syntax.error"),
    EL_ILLEGAL_BOOLEAN_RESULT(90010, Kind.UNEXPECTED, "error.el.illegal.boolean.result"),
    EL_DIVIDE_ZERO_EXCEPTION(90011, Kind.UNEXPECTED, "error.el.divide.zero.exception"),
    ;

    final int code;
    final String key;
    final Kind kind;

    ErrorCodes(int code, String key) {
        this(code, Kind.UNEXPECTED, key);
    }

    ErrorCodes(int code, Kind kind, String key) {
        this.code = code;
        this.key = key;
        this.kind = kind;
    }

    public OcpException exception() {
        if (kind == null) {
            throw new IllegalStateException("Kind undefined: " + this.name());
        }
        return kind.exception(this);
    }

    public OcpException exception(Object... arguments) {
        if (kind == null) {
            throw new IllegalStateException("Kind undefined: " + this.name());
        }
        return kind.exception(this, arguments);
    }

    enum Kind {

        /**
         * Kind of error.
         */
        AUTHENTICATION() {

            @Override
            public OcpException exception(ErrorCodes code) {
                return new AuthenticationException(code);
            }

            @Override
            public OcpException exception(ErrorCodes code, Object... args) {
                return new AuthenticationException(code, args);
            }
        },

        AUTHORIZATION() {

            @Override
            public OcpException exception(ErrorCodes code) {
                return new AuthorizationException(code);
            }

            @Override
            public OcpException exception(ErrorCodes code, Object... args) {
                return new AuthorizationException(code, args);
            }
        },

        BAD_REQUEST {

            @Override
            public OcpException exception(ErrorCodes code) {
                return new BadRequestException(code);
            }

            @Override
            public OcpException exception(ErrorCodes code, Object... args) {
                return new BadRequestException(code, args);
            }
        },

        ILLEGAL_ARGUMENT {

            @Override
            public OcpException exception(ErrorCodes code) {
                return new IllegalArgumentException(code);
            }

            @Override
            public OcpException exception(ErrorCodes code, Object... args) {
                return new IllegalArgumentException(code, args);
            }
        },

        CONFLICT {

            @Override
            public OcpException exception(ErrorCodes code) {
                return new ConflictException(code);
            }

            @Override
            public OcpException exception(ErrorCodes code, Object... args) {
                return new ConflictException(code, args);
            }
        },

        NOT_FOUND {

            @Override
            public OcpException exception(ErrorCodes code) {
                return new NotFoundException(code);
            }

            @Override
            public OcpException exception(ErrorCodes code, Object... args) {
                return new NotFoundException(code, args);
            }
        },

        UNEXPECTED {

            @Override
            public OcpException exception(ErrorCodes code) {
                return new UnexpectedException(code);
            }

            @Override
            public OcpException exception(ErrorCodes code, Object... args) {
                return new UnexpectedException(code, args);
            }
        },

        ;

        abstract OcpException exception(ErrorCodes code);

        abstract OcpException exception(ErrorCodes code, Object... args);
    }
}
