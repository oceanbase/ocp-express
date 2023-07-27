package com.oceanbase.ocp.obsdk.operator.tenant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.core.RowMapper;

import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;
import com.oceanbase.ocp.obsdk.exception.OceanBaseException;
import com.oceanbase.ocp.obsdk.operator.tenant.model.CreateTenantInput;
import com.oceanbase.ocp.obsdk.operator.tenant.model.ObTenant;
import com.oceanbase.ocp.obsdk.operator.tenant.model.SysVariable;

@RunWith(MockitoJUnitRunner.class)
public class MysqlTenantOperatorTest {

    private static ObConnectTemplate template;

    @Before
    public void setUp() throws Exception {
        template = mock(ObConnectTemplate.class);
        when(template.queryForObject(anyString(), any(), any(RowMapper.class))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            Object[] params = (Object[]) args[1];
            String dbName = (String) params[0];
            return buildObTenant(dbName);
        });
    }

    @After
    public void tearDown() throws Exception {}

    @Test
    public void createTenant_sqlShouldCorrectWhenInputAllParam() {
        MysqlTenantOperator operator = new MysqlTenantOperator(template);
        CreateTenantInput input = CreateTenantInput.builder().name("tenant1")
                .resourcePoolList(Arrays.asList("pool1", "pool2")).primaryZone("zone1,zone2;zone3")
                .locality("F@zone1,L@zone2,R@zone3").mode("mysql").collation("utf8mb4_general_ci")
                .build();
        ObTenant obTenant = operator.createTenant(input);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> argsCaptor = ArgumentCaptor.forClass(Object.class);
        verify(template).update(sqlCaptor.capture(), argsCaptor.capture());

        Assert.assertEquals(
                "CREATE TENANT `tenant1` resource_pool_list=('pool1','pool2'), LOCALITY = ?, PRIMARY_ZONE = ?, COLLATE = ? SET ob_tcp_invited_nodes='%', ob_compatibility_mode = ?",
                sqlCaptor.getValue());
        Assert.assertEquals(4, argsCaptor.getAllValues().size());
        Assert.assertEquals("tenant1", obTenant.getTenantName());
    }

    @Test
    public void createTenant_sqlShouldCorrectWhenInputPartParam() {
        MysqlTenantOperator operator = new MysqlTenantOperator(template);
        CreateTenantInput input =
                CreateTenantInput.builder().name("tenant1").resourcePoolList(Arrays.asList("pool1", "pool2"))
                        .locality("F@zone1,L@zone2,R@zone3").collation("utf8mb4_general_ci").build();
        ObTenant obTenant = operator.createTenant(input);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> argsCaptor = ArgumentCaptor.forClass(Object.class);
        verify(template).update(sqlCaptor.capture(), argsCaptor.capture());

        Assert.assertEquals(
                "CREATE TENANT `tenant1` resource_pool_list=('pool1','pool2'), LOCALITY = ?, COLLATE = ? SET ob_tcp_invited_nodes='%'",
                sqlCaptor.getValue());
        Assert.assertEquals(2, argsCaptor.getAllValues().size());
        Assert.assertEquals("tenant1", obTenant.getTenantName());
    }

    @Test(expected = NullPointerException.class)
    public void createTenant_shouldThrowExceptionWhenInputNull() {
        MysqlTenantOperator operator = new MysqlTenantOperator(template);
        operator.createTenant(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createTenant_shouldThrowExceptionWhenTenantNameEmpty() {
        MysqlTenantOperator operator = new MysqlTenantOperator(template);
        CreateTenantInput input = CreateTenantInput.builder().name("").build();
        operator.createTenant(input);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createTenant_shouldThrowExceptionWhenResourcePoolListEmpty() {
        MysqlTenantOperator operator = new MysqlTenantOperator(template);
        CreateTenantInput input =
                CreateTenantInput.builder().name("").resourcePoolList(Collections.emptyList()).build();
        operator.createTenant(input);
    }

    @Test
    public void listTenant() {
        List<ObTenant> obTenants = Arrays.asList(buildObTenant(1001L, "tenant1"), buildObTenant(1002L, "tenant2"),
                buildObTenant(1003L, "tenant3"));

        when(template.query(anyString(), any(RowMapper.class))).thenReturn(obTenants);
        MysqlTenantOperator operator = new MysqlTenantOperator(template);
        List<ObTenant> tenants = operator.listTenant();
        Assert.assertEquals(obTenants.size(), tenants.size());
        for (int i = 0; i < obTenants.size(); i++) {
            Assert.assertEquals(obTenants.get(i).getTenantName(), tenants.get(i).getTenantName());
        }
    }

    @Test
    public void getTenant_appendException() {
        Long tenantId = 12112L;
        when(template.queryForObject(anyString(), any(), any(RowMapper.class))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            Object[] params = (Object[]) args[1];
            return buildObTenant((Long) params[0]);
        });
        when(template.query(anyString(), any(), any(RowMapper.class))).thenThrow(new OceanBaseException("error"));

        MysqlTenantOperator operator = new MysqlTenantOperator(template);
        ObTenant obTenant = operator.getTenant(tenantId);

        Assert.assertEquals(tenantId, obTenant.getTenantId());
    }

    @Test(expected = NullPointerException.class)
    public void getTenant_shouldThrowExceptionWhenInputNull() {
        MysqlTenantOperator operator = new MysqlTenantOperator(template);
        Long tenantId = null;
        operator.getTenant(tenantId);
    }

    @Test
    public void deleteTenant_sqlShouldCorrectWhenObVersionAfter2_2_0() {

        MysqlTenantOperator operator = new MysqlTenantOperator(template);
        operator.deleteTenant("tenant1");

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).execute(sqlCaptor.capture());

        Assert.assertEquals("DROP TENANT IF EXISTS `tenant1` FORCE", sqlCaptor.getValue());
    }

    @Test(expected = NullPointerException.class)
    public void deleteTenant_shouldThrowExceptionWhenInputNull() {
        MysqlTenantOperator operator = new MysqlTenantOperator(template);
        operator.deleteTenant(null);
    }

    @Test
    public void lockTenant_sqlShouldCorrect() {
        MysqlTenantOperator operator = new MysqlTenantOperator(template);
        operator.lockTenant("tenant1");

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).execute(sqlCaptor.capture());

        Assert.assertEquals("ALTER TENANT `tenant1` LOCK", sqlCaptor.getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void lockTenant_shouldThrowExceptionWhenInputEmptyTenantName() {
        MysqlTenantOperator operator = new MysqlTenantOperator(template);
        operator.lockTenant("");
    }

    @Test(expected = NullPointerException.class)
    public void lockTenant_shouldThrowExceptionWhenInputNullTenantName() {
        MysqlTenantOperator operator = new MysqlTenantOperator(template);
        operator.lockTenant(null);
    }

    @Test
    public void unlockTenant_sqlShouldCorrect() {
        MysqlTenantOperator operator = new MysqlTenantOperator(template);
        operator.unlockTenant("tenant1");

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).execute(sqlCaptor.capture());

        Assert.assertEquals("ALTER TENANT `tenant1` UNLOCK", sqlCaptor.getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void unlockTenant_shouldThrowExceptionWhenInputEmptyTenantName() {
        MysqlTenantOperator operator = new MysqlTenantOperator(template);
        operator.unlockTenant("");
    }

    @Test(expected = NullPointerException.class)
    public void unlockTenant_shouldThrowExceptionWhenInputNullTenantName() {
        MysqlTenantOperator operator = new MysqlTenantOperator(template);
        operator.unlockTenant(null);
    }

    @Test
    public void getWhitelist() {
        Long tenantId = 1001L;
        SysVariable variable = new SysVariable();
        variable.setTenantId(tenantId);
        variable.setName("ob_tcp_invited_nodes");
        variable.setValue("127.0.0.1");
        List<SysVariable> variables = Collections.singletonList(variable);
        when(template.query(anyString(), any(), any(RowMapper.class))).thenReturn(variables);

        MysqlTenantOperator operator = new MysqlTenantOperator(template);
        Optional<String> whitelist = operator.getWhitelist(tenantId);
        Assert.assertTrue(whitelist.isPresent());
        Assert.assertEquals("127.0.0.1", whitelist.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void modifyWhitelist_shouldThrowExceptionWhenInputEmptyTenantName() {
        MysqlTenantOperator operator = new MysqlTenantOperator(template);
        operator.modifyWhitelist("", "%");
    }

    @Test(expected = NullPointerException.class)
    public void modifyWhitelist_shouldThrowExceptionWhenInputNullWhiteList() {
        MysqlTenantOperator operator = new MysqlTenantOperator(template);
        operator.modifyWhitelist("tenant1", null);
    }

    @Test
    public void modifyPrimaryZone_sqlShouldCorrectWhenNormal() {
        MysqlTenantOperator operator = new MysqlTenantOperator(template);
        operator.modifyPrimaryZone("tenant1", "zone1,zone2");

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> argsCaptor = ArgumentCaptor.forClass(Object.class);
        verify(template).update(sqlCaptor.capture(), argsCaptor.capture());

        Assert.assertEquals("ALTER TENANT `tenant1` PRIMARY_ZONE = ?", sqlCaptor.getValue());
        Assert.assertEquals(1, argsCaptor.getAllValues().size());
        Assert.assertEquals("zone1,zone2", argsCaptor.getAllValues().get(0));
    }

    @Test
    public void modifyPrimaryZone_primaryZoneEmptyWhenVersionAfter2_0_0() {
        MysqlTenantOperator operator = new MysqlTenantOperator(template);
        operator.modifyPrimaryZone("tenant1", "");

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> argsCaptor = ArgumentCaptor.forClass(Object.class);
        verify(template).update(sqlCaptor.capture(), argsCaptor.capture());

        Assert.assertEquals("ALTER TENANT `tenant1` PRIMARY_ZONE = RANDOM", sqlCaptor.getValue());
        Assert.assertEquals(0, argsCaptor.getAllValues().size());
    }


    @Test(expected = IllegalArgumentException.class)
    public void modifyPrimaryZone_shouldThrowExceptionWhenInputTenantNameEmpty() {
        MysqlTenantOperator operator = new MysqlTenantOperator(template);
        operator.modifyPrimaryZone("", "zone1,zone2");
    }

    @Test(expected = NullPointerException.class)
    public void modifyPrimaryZone_shouldThrowExceptionWhenInputPrimaryZoneNull() {
        MysqlTenantOperator operator = new MysqlTenantOperator(template);
        operator.modifyPrimaryZone("tenant1", null);
    }

    @Test
    public void modifyLocality_sqlShouldCorrectWhenNormal() {
        MysqlTenantOperator operator = new MysqlTenantOperator(template);
        operator.modifyLocality("tenant1", "F@zone1,F@zone2");

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> argsCaptor = ArgumentCaptor.forClass(Object.class);
        verify(template).update(sqlCaptor.capture(), argsCaptor.capture());

        Assert.assertEquals("ALTER TENANT `tenant1` LOCALITY = ?", sqlCaptor.getValue());
        Assert.assertEquals(1, argsCaptor.getAllValues().size());
        Assert.assertEquals("F@zone1,F@zone2", argsCaptor.getAllValues().get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void modifyLocality_shouldThrowExceptionWhenInputTenantNameEmpty() {
        MysqlTenantOperator operator = new MysqlTenantOperator(template);
        operator.modifyLocality("", "F@zone1,F@zone2");
    }

    @Test(expected = NullPointerException.class)
    public void modifyLocality_shouldThrowExceptionWhenInputLocalityNull() {
        MysqlTenantOperator operator = new MysqlTenantOperator(template);
        operator.modifyLocality("tenant1", null);
    }

    @Test
    public void modifyResourcePoolList_sqlShouldCorrectWhenNormal() {
        List<String> pools = Arrays.asList("pool1", "pool2");
        MysqlTenantOperator operator = new MysqlTenantOperator(template);
        operator.modifyResourcePoolList("tenant1", pools);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).execute(sqlCaptor.capture());

        Assert.assertEquals("ALTER TENANT `tenant1` RESOURCE_POOL_LIST=('pool1','pool2')", sqlCaptor.getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void modifyResourcePoolList_shouldThrowExceptionWhenInputTenantNameEmpty() {
        List<String> pools = Arrays.asList("pool1", "pool2");
        MysqlTenantOperator operator = new MysqlTenantOperator(template);
        operator.modifyResourcePoolList("", pools);
    }

    @Test(expected = IllegalArgumentException.class)
    public void modifyResourcePoolList_shouldThrowExceptionWhenInputResourcePoolEmpty() {
        MysqlTenantOperator operator = new MysqlTenantOperator(template);
        operator.modifyResourcePoolList("tenant1", Collections.emptyList());
    }

    @Test
    public void getJobProgress() {}

    private ObTenant buildObTenant(String tenantName) {
        ObTenant tenant = new ObTenant();
        tenant.setTenantId(1001L);
        tenant.setTenantName(tenantName);
        return tenant;
    }

    private ObTenant buildObTenant(Long tenantId) {
        ObTenant tenant = new ObTenant();
        tenant.setTenantId(tenantId);
        return tenant;
    }

    private ObTenant buildObTenant(Long tenantId, String tenantName) {
        ObTenant tenant = new ObTenant();
        tenant.setTenantId(tenantId);
        tenant.setTenantName(tenantName);
        return tenant;
    }
}
