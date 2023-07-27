package com.oceanbase.ocp.obsdk.accessor.user;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.core.RowMapper;

import com.oceanbase.ocp.obsdk.accessor.user.model.ObUser;
import com.oceanbase.ocp.obsdk.connector.HintQueryer;
import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;
import com.oceanbase.ocp.obsdk.enums.DbPrivType;
import com.oceanbase.ocp.obsdk.enums.GlobalPrivilege;
import com.oceanbase.ocp.obsdk.enums.MysqlGlobalPrivilege;

@RunWith(MockitoJUnitRunner.class)
public class MysqlUserAccessorTest {

    private static ObConnectTemplate template;

    @Before
    public void setUp() throws Exception {
        template = mock(ObConnectTemplate.class);
        when(template.getObVersion()).thenReturn("2.2.1");
        HintQueryer hintQueryer = new HintQueryer(template);
        when(template.weakRead()).thenReturn(hintQueryer);
    }

    @After
    public void tearDown() throws Exception {}

    @Test
    public void createUser_sqlShouldCorrectWhenNormal() {
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.createUser("user1", "pwd123");

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> paramCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).update(sqlCaptor.capture(), paramCaptor.capture());

        Assert.assertEquals("CREATE USER IF NOT EXISTS ? IDENTIFIED BY ?", sqlCaptor.getValue());
        Assert.assertEquals(2, paramCaptor.getAllValues().size());
        Assert.assertEquals("user1", paramCaptor.getAllValues().get(0));
        Assert.assertEquals("pwd123", paramCaptor.getAllValues().get(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createUser_shouldThrowExceptionWhenUsernameEmpty() {
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.createUser("", "pwd123");
    }

    @Test(expected = NullPointerException.class)
    public void createUser_shouldThrowExceptionWhenUsernameNull() {
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.createUser(null, "pwd123");
    }

    @Test
    public void dropUser_sqlShouldCorrectWhenNormal() {
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.dropUser("user1");

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> paramCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).update(sqlCaptor.capture(), paramCaptor.capture());

        Assert.assertEquals("DROP USER ?", sqlCaptor.getValue());
        Assert.assertEquals(1, paramCaptor.getAllValues().size());
        Assert.assertEquals("user1", paramCaptor.getAllValues().get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void dropUser_shouldThrowExceptionWhenUsernameEmpty() {
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.dropUser("");
    }

    @Test(expected = NullPointerException.class)
    public void dropUser_shouldThrowExceptionWhenUsernameNull() {
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.dropUser(null);
    }

    @Test
    public void listUsers() {
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.listUsers();

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<RowMapper> mapperCaptor = ArgumentCaptor.forClass(RowMapper.class);
        verify(template, times(3)).query(sqlCaptor.capture(), mapperCaptor.capture());

        Assert.assertTrue(sqlCaptor.getAllValues().get(0).contains("FROM `mysql`.`user`"));
        Assert.assertEquals("SHOW DATABASES", sqlCaptor.getAllValues().get(1));
        Assert.assertTrue(sqlCaptor.getAllValues().get(2).contains("FROM `mysql`.`db`"));
    }

    @Test
    public void listUsernames() {
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.listUsernames();

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).query(sqlCaptor.capture(), any());

        assertThat(sqlCaptor.getValue(), containsString("FROM `mysql`.`user`"));
    }

    @Test
    public void getUser() {
        when(template.queryForObject(anyString(), any(Object[].class), any(RowMapper.class)))
                .thenReturn(buildObUser("user1"));

        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.getUser("user1");

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> paramCaptor = ArgumentCaptor.forClass(Object[].class);
        ArgumentCaptor<RowMapper> mapperCaptor = ArgumentCaptor.forClass(RowMapper.class);
        verify(template).queryForObject(sqlCaptor.capture(), paramCaptor.capture(), mapperCaptor.capture());

        Assert.assertTrue(sqlCaptor.getValue().contains("FROM `mysql`.`user`"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getUser_shouldThrowExceptionWhenInputUsernameEmpty() {
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.getUser("");
    }

    @Test(expected = NullPointerException.class)
    public void getUser_shouldThrowExceptionWhenInputUsernameNull() {
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.getUser(null);
    }

    @Test
    public void lockUser_sqlShouldCorrectWhenObVersionAfter2_2_1() {
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.lockUser("user1");

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> paramCaptor = ArgumentCaptor.forClass(Object.class);
        verify(template).update(sqlCaptor.capture(), paramCaptor.capture());

        Assert.assertEquals("ALTER USER ? ACCOUNT LOCK", sqlCaptor.getValue());
        Assert.assertEquals(1, paramCaptor.getAllValues().size());
        Assert.assertEquals("user1", paramCaptor.getAllValues().get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void lockUser_shouldThrowExceptionWhenInputEmptyUsername() {
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.lockUser("");
    }

    @Test(expected = NullPointerException.class)
    public void lockUser_shouldThrowExceptionWhenInputNullUsername() {
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.lockUser(null);
    }

    @Test
    public void unlockUser_sqlShouldCorrectWhenNormal() {
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.unlockUser("user1");

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> paramCaptor = ArgumentCaptor.forClass(Object.class);
        verify(template).update(sqlCaptor.capture(), paramCaptor.capture());

        Assert.assertEquals("ALTER USER ? ACCOUNT UNLOCK", sqlCaptor.getValue());
        Assert.assertEquals(1, paramCaptor.getAllValues().size());
        Assert.assertEquals("user1", paramCaptor.getAllValues().get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void unlockUser_shouldThrowExceptionWhenInputEmptyUsername() {
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.unlockUser("");
    }

    @Test(expected = NullPointerException.class)
    public void unlockUser_shouldThrowExceptionWhenInputNullUsername() {
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.unlockUser(null);
    }

    @Test
    public void alterPassword_sqlShouldCorrectWhenNormal() {
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.alterPassword("user1", "pwd123");

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> paramCaptor = ArgumentCaptor.forClass(Object.class);
        verify(template).update(sqlCaptor.capture(), paramCaptor.capture());

        Assert.assertEquals("ALTER USER ? IDENTIFIED BY ?", sqlCaptor.getValue());
        Assert.assertEquals(2, paramCaptor.getAllValues().size());
        Assert.assertEquals("user1", paramCaptor.getAllValues().get(0));
        Assert.assertEquals("pwd123", paramCaptor.getAllValues().get(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void alterPassword_shouldThrowExceptionWhenInputEmptyUsername() {
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.alterPassword("", "pwd123");
    }

    @Test(expected = NullPointerException.class)
    public void alterPassword_shouldThrowExceptionWhenInputNullUsername() {
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.alterPassword(null, "pwd123");
    }

    @Test
    public void alterSuperPassword_sqlShouldCorrectWhenNormal() {
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.alterSuperPassword("pwd123");

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> paramCaptor = ArgumentCaptor.forClass(Object.class);
        verify(template).update(sqlCaptor.capture(), paramCaptor.capture());

        Assert.assertEquals("ALTER USER ? IDENTIFIED BY ?", sqlCaptor.getValue());
        Assert.assertEquals(2, paramCaptor.getAllValues().size());
        Assert.assertEquals("root", paramCaptor.getAllValues().get(0));
        Assert.assertEquals("pwd123", paramCaptor.getAllValues().get(1));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void listRoles_unsupported() {
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.listRoles();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getRole_unsupported() {
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.getRole("role1");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void createRole_unsupported() {
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.createRole("role1");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void dropRole_unsupported() {
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.dropRole("role1");
    }

    @Test
    public void grantGlobalPrivilege_sqlShouldCorrectWhenNormal() {
        List<GlobalPrivilege> privileges = Arrays.asList(MysqlGlobalPrivilege.CREATE, MysqlGlobalPrivilege.DELETE);
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.grantGlobalPrivilege("user1", privileges);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> paramCaptor = ArgumentCaptor.forClass(Object.class);
        verify(template).update(sqlCaptor.capture(), paramCaptor.capture());

        Assert.assertEquals("GRANT CREATE,DELETE ON *.* TO ?", sqlCaptor.getValue());
        Assert.assertEquals(1, paramCaptor.getAllValues().size());
        Assert.assertEquals("user1", paramCaptor.getAllValues().get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void grantGlobalPrivilege_shouldThrowExceptionWhenInputEmptyUsername() {
        List<GlobalPrivilege> privileges = Arrays.asList(MysqlGlobalPrivilege.CREATE, MysqlGlobalPrivilege.DELETE);
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.grantGlobalPrivilege("", privileges);
    }

    @Test(expected = NullPointerException.class)
    public void grantGlobalPrivilege_shouldThrowExceptionWhenInputNullUsername() {
        List<GlobalPrivilege> privileges = Arrays.asList(MysqlGlobalPrivilege.CREATE, MysqlGlobalPrivilege.DELETE);
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.grantGlobalPrivilege(null, privileges);
    }

    @Test
    public void revokeGlobalPrivilege_sqlShouldCorrectWhenNormal() {
        List<GlobalPrivilege> privileges = Arrays.asList(MysqlGlobalPrivilege.CREATE, MysqlGlobalPrivilege.DELETE);
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.revokeGlobalPrivilege("user1", privileges);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> paramCaptor = ArgumentCaptor.forClass(Object.class);
        verify(template).update(sqlCaptor.capture(), paramCaptor.capture());

        Assert.assertEquals("REVOKE CREATE,DELETE ON *.* FROM ?", sqlCaptor.getValue());
        Assert.assertEquals(1, paramCaptor.getAllValues().size());
        Assert.assertEquals("user1", paramCaptor.getAllValues().get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void revokeGlobalPrivilege_shouldThrowExceptionWhenInputEmptyUsername() {
        List<GlobalPrivilege> privileges = Arrays.asList(MysqlGlobalPrivilege.CREATE, MysqlGlobalPrivilege.DELETE);
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.revokeGlobalPrivilege("", privileges);
    }

    @Test(expected = NullPointerException.class)
    public void revokeGlobalPrivilege_shouldThrowExceptionWhenInputNullUsername() {
        List<GlobalPrivilege> privileges = Arrays.asList(MysqlGlobalPrivilege.CREATE, MysqlGlobalPrivilege.DELETE);
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.revokeGlobalPrivilege(null, privileges);
    }

    @Test
    public void grantDbPrivilege_sqlShouldCorrectWhenNormal() {
        List<DbPrivType> privileges = Arrays.asList(DbPrivType.INSERT, DbPrivType.DELETE);
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.grantDbPrivilege("user1", "db1", privileges);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> paramCaptor = ArgumentCaptor.forClass(Object.class);
        verify(template).update(sqlCaptor.capture(), paramCaptor.capture());

        Assert.assertEquals("GRANT INSERT,DELETE ON `db1`.* TO ?", sqlCaptor.getValue());
        Assert.assertEquals(1, paramCaptor.getAllValues().size());
        Assert.assertEquals("user1", paramCaptor.getAllValues().get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void grantDbPrivilege_shouldThrowExceptionWhenInputEmptyUsername() {
        List<DbPrivType> privileges = Arrays.asList(DbPrivType.INSERT, DbPrivType.DELETE);
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.grantDbPrivilege("", "db1", privileges);
    }

    @Test(expected = IllegalArgumentException.class)
    public void grantDbPrivilege_shouldThrowExceptionWhenInputEmptyDbName() {
        List<DbPrivType> privileges = Arrays.asList(DbPrivType.INSERT, DbPrivType.DELETE);
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.grantDbPrivilege("user1", "", privileges);
    }

    @Test
    public void revokeDbPrivilege_sqlShouldCorrectWhenNormal() {
        List<DbPrivType> privileges = Arrays.asList(DbPrivType.INSERT, DbPrivType.DELETE);
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.revokeDbPrivilege("user1", "db1", privileges);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> paramCaptor = ArgumentCaptor.forClass(Object.class);
        verify(template).update(sqlCaptor.capture(), paramCaptor.capture());

        Assert.assertEquals("REVOKE INSERT,DELETE ON `db1`.* FROM ?", sqlCaptor.getValue());
        Assert.assertEquals(1, paramCaptor.getAllValues().size());
        Assert.assertEquals("user1", paramCaptor.getAllValues().get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void revokeDbPrivilege_shouldThrowExceptionWhenInputEmptyUsername() {
        List<DbPrivType> privileges = Arrays.asList(DbPrivType.INSERT, DbPrivType.DELETE);
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.revokeDbPrivilege("", "db1", privileges);
    }

    @Test(expected = IllegalArgumentException.class)
    public void revokeDbPrivilege_shouldThrowExceptionWhenInputEmptyDbName() {
        List<DbPrivType> privileges = Arrays.asList(DbPrivType.INSERT, DbPrivType.DELETE);
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.revokeDbPrivilege("user1", "", privileges);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void grantRole_unsupported() {
        List<String> roles = Arrays.asList("role1", "role2", "role3");
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.grantRole("user1", roles);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void revokeRole_unsupported() {
        List<String> roles = Arrays.asList("role1", "role2", "role3");
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.revokeRole("user1", roles);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void listAllObjects_unsupported() {
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.listAllObjects();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void listAllObjectPrivileges_unsupported() {
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.listAllObjectPrivileges();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void grantObjectPrivilege_unsupported() {
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.grantObjectPrivilege("user1", null, Collections.emptyList());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void revokeObjectPrivilege_unsupported() {
        MysqlUserAccessor accessor = new MysqlUserAccessor(template);
        accessor.revokeObjectPrivilege("user1", null, Collections.emptyList());
    }


    private ObUser buildObUser(String username) {
        ObUser obUser = new ObUser();
        obUser.setGmtCreate(new Timestamp(System.currentTimeMillis()));
        obUser.setUserName(username);
        obUser.setIsLocked(0L);
        return obUser;
    }

}
