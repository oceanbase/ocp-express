package com.oceanbase.ocp.obsdk.accessor.user;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.core.RowMapper;

import com.oceanbase.ocp.obsdk.accessor.user.model.DbObject;
import com.oceanbase.ocp.obsdk.accessor.user.model.ObRole;
import com.oceanbase.ocp.obsdk.accessor.user.model.ObUser;
import com.oceanbase.ocp.obsdk.accessor.user.model.ObjectPrivilege;
import com.oceanbase.ocp.obsdk.accessor.user.model.UserObjectPrivilege;
import com.oceanbase.ocp.obsdk.connector.HintQueryer;
import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;
import com.oceanbase.ocp.obsdk.enums.DbPrivType;
import com.oceanbase.ocp.obsdk.enums.GlobalPrivilege;
import com.oceanbase.ocp.obsdk.enums.ObjectPrivilegeType;
import com.oceanbase.ocp.obsdk.enums.ObjectType;
import com.oceanbase.ocp.obsdk.enums.OracleSystemPrivilege;

@RunWith(MockitoJUnitRunner.class)
public class OracleUserAccessorTest {

    private static final String USERNAME = "USER1";
    private static final String VALUE = "whatever1";
    private static final String NEW_VALUE = "whatever2";
    private static final String DB_NAME = "db1";
    private static final List<GlobalPrivilege> ORACLE_SYSTEM_PRIVILEGES =
            Arrays.asList(OracleSystemPrivilege.CREATE_ANY_TABLE, OracleSystemPrivilege.CREATE_ANY_INDEX);
    private static final List<DbPrivType> DB_PRIVILEGES = Arrays.asList(DbPrivType.INSERT, DbPrivType.DELETE);
    private static final String ROLE_NAME = "ROLE1";
    private static final List<String> ROLE_LIST = Arrays.asList("ROLE1", "ROLE2", "ROLE3");
    private static final DbObject DB_OBJECT = new DbObject() {

        {
            setObjectType(ObjectType.TABLE);
            setSchemaName("USER1");
            setObjectName("TABLE1");
        }
    };
    private static final List<ObjectPrivilegeType> OBJECT_PRIVILEGE_TYPES =
            Arrays.asList(ObjectPrivilegeType.SELECT, ObjectPrivilegeType.ALTER, ObjectPrivilegeType.UPDATE);

    private ObConnectTemplate template;
    private OracleUserAccessor accessor;

    @Before
    public void setUp() throws Exception {
        template = mock(ObConnectTemplate.class);
        when(template.getObVersion()).thenReturn("2.2.77");
        HintQueryer hintQueryer = new HintQueryer(template);
        when(template.weakRead()).thenReturn(hintQueryer);

        accessor = new OracleUserAccessor(template);
    }

    private ObUser buildObUser(String username) {
        ObUser obUser = new ObUser();
        obUser.setGmtCreate(Timestamp.from(Instant.now()));
        obUser.setUserName(username);
        obUser.setIsLocked(0L);
        return obUser;
    }

    private ObRole buildObRole(String roleName) {
        ObRole obRole = new ObRole();
        obRole.setName(roleName);
        obRole.setCreateTime(Timestamp.from(Instant.now()));
        obRole.setUpdateTime(Timestamp.from(Instant.now()));
        return obRole;
    }

    @Test
    public void createUser() {
        accessor.createUser(USERNAME, VALUE);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).update(sqlCaptor.capture(), eq(USERNAME));
        assertThat(sqlCaptor.getValue(), containsString("CREATE USER"));
        assertThat(sqlCaptor.getValue(), containsString(VALUE));
    }

    @Test
    public void dropUser() {
        accessor.dropUser(USERNAME);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).update(sqlCaptor.capture(), eq(USERNAME));
        assertThat(sqlCaptor.getValue(), containsString("DROP USER"));
    }

    @Test
    public void listUsers() {
        accessor = new OracleUserAccessor(template);

        List<ObUser> obUsers = Stream.of("USER1", "USER2", "USER3").map(this::buildObUser).collect(Collectors.toList());
        when(template.query(anyString(), any(RowMapper.class))).thenReturn(obUsers);

        accessor.listUsers();

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).query(sqlCaptor.capture(), any());
        assertThat(sqlCaptor.getValue(), containsString("SELECT"));
        assertThat(sqlCaptor.getValue(), containsString("DBA_USERS"));
    }

    @Test
    public void listUsernames() {
        accessor.listUsernames();

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).query(sqlCaptor.capture(), any());
        assertThat(sqlCaptor.getValue(), containsString("SELECT"));
        assertThat(sqlCaptor.getValue(), containsString("DBA_USERS"));
    }

    @Test
    public void getUser() {
        accessor = new OracleUserAccessor(template);

        when(template.queryForObject(anyString(), any(), any(RowMapper.class))).thenReturn(buildObUser("USER1"));

        accessor.getUser(USERNAME);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> argsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(template).queryForObject(sqlCaptor.capture(), argsCaptor.capture(), any(RowMapper.class));
        assertEquals(USERNAME, argsCaptor.getValue()[0]);
        assertThat(sqlCaptor.getValue(), containsString("SELECT"));
        assertThat(sqlCaptor.getValue(), containsString("DBA_USERS"));
    }

    @Test
    public void lockUser() {
        accessor.lockUser(USERNAME);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).update(sqlCaptor.capture(), eq(USERNAME));
        assertThat(sqlCaptor.getValue(), containsString("ACCOUNT LOCK"));
    }

    @Test
    public void unlockUser() {
        accessor.unlockUser(USERNAME);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).update(sqlCaptor.capture(), eq(USERNAME));
        assertThat(sqlCaptor.getValue(), containsString("ACCOUNT UNLOCK"));
    }

    @Test
    public void alterPassword() {
        accessor.alterPassword(USERNAME, NEW_VALUE);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).update(sqlCaptor.capture(), eq(USERNAME));
        assertThat(sqlCaptor.getValue(), containsString("ALTER USER"));
        assertThat(sqlCaptor.getValue(), containsString(NEW_VALUE));
    }

    @Test
    public void alterSuperPassword() {
        accessor.alterSuperPassword(NEW_VALUE);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).update(sqlCaptor.capture(), eq("SYS"));
        assertThat(sqlCaptor.getValue(), containsString("ALTER USER"));
        assertThat(sqlCaptor.getValue(), containsString(NEW_VALUE));
    }

    @Test
    public void listRoles() {
        accessor = new OracleUserAccessor(template);

        List<ObRole> obRoles = Stream.of("ROLE1", "ROLE2", "ROLE3").map(this::buildObRole).collect(Collectors.toList());
        when(template.query(anyString(), any(RowMapper.class))).thenReturn(obRoles);

        accessor.listRoles();

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).query(sqlCaptor.capture(), any(RowMapper.class));
        assertThat(sqlCaptor.getValue(), containsString("SELECT"));
        assertThat(sqlCaptor.getValue(), containsString("DBA_ROLES"));
    }

    @Test
    public void getRole() {
        accessor = new OracleUserAccessor(template);

        when(template.queryForObject(anyString(), any(), any(RowMapper.class))).thenReturn(buildObRole("ROLE1"));

        accessor.getRole(ROLE_NAME);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> argsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(template).queryForObject(sqlCaptor.capture(), argsCaptor.capture(), any(RowMapper.class));
        assertThat(sqlCaptor.getValue(), containsString("SELECT"));
        assertThat(sqlCaptor.getValue(), containsString("DBA_ROLES"));
        assertEquals(ROLE_NAME, argsCaptor.getValue()[0]);
    }

    @Test
    public void createRole() {
        accessor.createRole(ROLE_NAME);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).update(sqlCaptor.capture(), eq(ROLE_NAME));
        assertThat(sqlCaptor.getValue(), containsString("CREATE ROLE"));
    }

    @Test
    public void dropRole() {
        accessor.dropRole(ROLE_NAME);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).update(sqlCaptor.capture(), eq(ROLE_NAME));
        assertThat(sqlCaptor.getValue(), containsString("DROP ROLE"));
    }

    @Test
    public void grantGlobalPrivilege() {
        accessor.grantGlobalPrivilege(USERNAME, ORACLE_SYSTEM_PRIVILEGES);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).update(sqlCaptor.capture(), eq(USERNAME));
        String sql = sqlCaptor.getValue();
        assertThat(sql, containsString("GRANT"));
        for (GlobalPrivilege globalPrivilege : ORACLE_SYSTEM_PRIVILEGES) {
            OracleSystemPrivilege systemPrivilege = globalPrivilege.asOracle();
            assertThat(sql, containsString(systemPrivilege.getValue()));
        }
    }

    @Test
    public void revokeGlobalPrivilege() {
        accessor.revokeGlobalPrivilege(USERNAME, ORACLE_SYSTEM_PRIVILEGES);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).update(sqlCaptor.capture(), eq(USERNAME));
        String sql = sqlCaptor.getValue();
        assertThat(sql, containsString("REVOKE"));
        for (GlobalPrivilege globalPrivilege : ORACLE_SYSTEM_PRIVILEGES) {
            OracleSystemPrivilege systemPrivilege = globalPrivilege.asOracle();
            assertThat(sql, containsString(systemPrivilege.getValue()));
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void grantDbPrivilege() {
        accessor.grantDbPrivilege(USERNAME, DB_NAME, DB_PRIVILEGES);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void revokeDbPrivilege() {
        accessor.revokeDbPrivilege(USERNAME, DB_NAME, DB_PRIVILEGES);
    }

    @Test
    public void grantRole() {
        accessor.grantRole(USERNAME, ROLE_LIST);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).update(sqlCaptor.capture(), eq(USERNAME));
        String sql = sqlCaptor.getValue();
        assertThat(sql, containsString("GRANT"));
        for (String role : ROLE_LIST) {
            assertThat(sql, containsString(role));
        }
    }

    @Test
    public void revokeRole() {
        accessor.revokeRole(USERNAME, ROLE_LIST);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).update(sqlCaptor.capture(), eq(USERNAME));
        String sql = sqlCaptor.getValue();
        assertThat(sql, containsString("REVOKE"));
        for (String role : ROLE_LIST) {
            assertThat(sql, containsString(role));
        }
    }

    @Test
    public void listAllObjectPrivileges_omitUnknown() {
        UserObjectPrivilege userObjectPrivilege = new UserObjectPrivilege();
        userObjectPrivilege.setObjectType(ObjectType.TABLE);
        userObjectPrivilege.setObjectName("T1");
        userObjectPrivilege.setSchemaName("U1");
        userObjectPrivilege.setObjectPrivilege(ObjectPrivilegeType.fromValue("UNKNOWN"));
        when(template.query(anyString(), any())).thenReturn(Collections.singletonList(userObjectPrivilege));

        List<ObjectPrivilege> objectPrivileges = accessor.listAllObjectPrivileges();
        assertTrue(objectPrivileges.isEmpty());
    }

    @Test
    public void grantObjectPrivilege() {
        accessor.grantObjectPrivilege(USERNAME, DB_OBJECT, OBJECT_PRIVILEGE_TYPES);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).update(sqlCaptor.capture(), eq(USERNAME));
        String sql = sqlCaptor.getValue();
        assertThat(sql, containsString("GRANT"));
        assertThat(sql, containsString(DB_OBJECT.getSchemaName()));
        assertThat(sql, containsString(DB_OBJECT.getObjectName()));
        for (ObjectPrivilegeType privilege : OBJECT_PRIVILEGE_TYPES) {
            assertThat(sql, containsString(privilege.getValue()));
        }
    }

    @Test
    public void revokeObjectPrivilege() {
        accessor.revokeObjectPrivilege(USERNAME, DB_OBJECT, OBJECT_PRIVILEGE_TYPES);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).update(sqlCaptor.capture(), eq(USERNAME));
        String sql = sqlCaptor.getValue();
        assertThat(sql, containsString("REVOKE"));
        assertThat(sql, containsString(DB_OBJECT.getSchemaName()));
        assertThat(sql, containsString(DB_OBJECT.getObjectName()));
        for (ObjectPrivilegeType privilege : OBJECT_PRIVILEGE_TYPES) {
            assertThat(sql, containsString(privilege.getValue()));
        }
    }
}
