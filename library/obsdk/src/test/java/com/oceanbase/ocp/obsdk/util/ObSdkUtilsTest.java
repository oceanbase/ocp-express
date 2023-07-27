package com.oceanbase.ocp.obsdk.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.oceanbase.ocp.obsdk.enums.DbPrivType;
import com.oceanbase.ocp.obsdk.exception.OceanBaseException;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObServer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class ObSdkUtilsTest {

    private static String[][] versionBefore =
            {{"1.4.71", "1.4.72"}, {"1.3.72", "1.4.72"}, {"1.4.72", "2.0.0"}, {"2.1.10", "2.2.0"}};
    private static String[][] versionEqual = {{"1.4.72", "1.4.72"}, {"2.2.0", "2.2.0"}, {"4.0.0", "4.0.0.0"}};
    private static String[][] versionAfter =
            {{"1.4.72", "1.4.71"}, {"1.4.72", "1.3.72"}, {"2.0.0", "1.4.72"}, {"2.2.0", "2.1.10"}};

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {}

    @After
    public void tearDown() throws Exception {}

    @Test
    public void toCommaSeparatedString_shouldReturnEmptyStringWhenInputNull() {
        assertEquals("", ObSdkUtils.toCommaSeparatedString(null));
    }

    @Test
    public void toCommaSeparatedString_shouldReturnCommaSeparatedStringWhenInputStringList() {
        String[] array = {"hello", "world", "hangzhou"};
        List<String> list = new ArrayList<>(Arrays.asList(array));
        assertEquals("hello,world,hangzhou", ObSdkUtils.toCommaSeparatedString(list));
    }

    @Test
    public void toCommaSeparatedString_shouldReturnCommaSeparatedStringWhenInputEnumList() {
        DbPrivType[] array = {DbPrivType.ALTER, DbPrivType.CREATE, DbPrivType.CREATE_VIEW};
        List<DbPrivType> list = new ArrayList<>(Arrays.asList(array));
        assertEquals("ALTER,CREATE,CREATE VIEW", ObSdkUtils.toCommaSeparatedString(list));
    }

    @Test
    public void toCommaSeparatedString_shouldReturnCommaSeparatedStringWhenInputIntegerList() {
        Integer[] array = {2215, 2216, 2217};
        List<Integer> list = new ArrayList<>(Arrays.asList(array));
        assertEquals("2215,2216,2217", ObSdkUtils.toCommaSeparatedString(list));
    }

    @Test
    public void toCommaSeparatedString_shouldReturnCommaSeparatedStringWhenInputLongList() {
        Long[] array = {12112L, 12115L, 12118L};
        List<Long> list = new ArrayList<>(Arrays.asList(array));
        assertEquals("12112,12115,12118", ObSdkUtils.toCommaSeparatedString(list));
    }

    @Test
    public void toCommaSeparatedStringWithQuotationMark_shouldReturnCorrectWhenInputNull() {
        assertEquals("", ObSdkUtils.toCommaSeparatedStringWithQuotationMark(null));
    }

    @Test
    public void toCommaSeparatedStringWithQuotationMark_shouldReturnCorrectWhenInputStringList() {
        String[] array = {"hello", "world", "hangzhou"};
        List<String> list = new ArrayList<>(Arrays.asList(array));
        assertEquals("'hello','world','hangzhou'", ObSdkUtils.toCommaSeparatedStringWithQuotationMark(list));
    }

    @Test
    public void toCommaSeparatedStringWithQuotationMark_shouldReturnCorrectWhenInputEnumList() {
        DbPrivType[] array = {DbPrivType.ALTER, DbPrivType.CREATE, DbPrivType.CREATE_VIEW};
        List<DbPrivType> list = new ArrayList<>(Arrays.asList(array));
        assertEquals("'ALTER','CREATE','CREATE VIEW'", ObSdkUtils.toCommaSeparatedStringWithQuotationMark(list));
    }

    @Test
    public void toCommaSeparatedStringWithQuotationMark_shouldReturnCorrectWhenInputIntegerList() {
        Integer[] array = {2215, 2216, 2217};
        List<Integer> list = new ArrayList<>(Arrays.asList(array));
        assertEquals("'2215','2216','2217'", ObSdkUtils.toCommaSeparatedStringWithQuotationMark(list));
    }

    @Test
    public void toCommaSeparatedStringWithQuotationMark_shouldReturnCorrectWhenInputLongList() {
        Long[] array = {12112L, 12115L, 12118L};
        List<Long> list = new ArrayList<>(Arrays.asList(array));
        assertEquals("'12112','12115','12118'", ObSdkUtils.toCommaSeparatedStringWithQuotationMark(list));
    }

    @Test(expected = NullPointerException.class)
    public void versionBefore_shouldThrowNullPointerExceptionWhenCurrVersionIsNull() {
        ObSdkUtils.versionBefore(null, "2.2.1");
    }

    @Test(expected = NullPointerException.class)
    public void versionBefore_shouldThrowNullPointerExceptionWhenTarVersionIsNull() {
        ObSdkUtils.versionBefore("2.2.1", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void versionBefore_shouldThrowExceptionWhenCurrVersionIsNotValid() {
        ObSdkUtils.versionBefore("2.2", "2.2.1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void versionBefore_shouldThrowExceptionWhenTarVersionIsNotValid() {
        ObSdkUtils.versionBefore("1.4.72", "2.2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void versionBefore_shouldThrowExceptionWhenBothVersionIsNotValid() {
        ObSdkUtils.versionBefore("1.4", "2.2");
    }

    @Test
    public void versionBefore_shouldReturnTrueWhenCurrVersionBeforeTarVersion() {
        for (String[] versions : versionBefore) {
            assertTrue(ObSdkUtils.versionBefore(versions[0], versions[1]));
        }
    }

    @Test
    public void versionBefore_shouldReturnFalseWhenCurrVersionEqualsTarVersion() {
        for (String[] versions : versionEqual) {
            assertFalse(ObSdkUtils.versionBefore(versions[0], versions[1]));
        }
    }

    @Test
    public void versionBefore_shouldReturnFalseWhenCurrVersionAfterTarVersion() {
        for (String[] versions : versionAfter) {
            assertFalse(ObSdkUtils.versionBefore(versions[0], versions[1]));
        }
    }

    @Test
    public void versionAfter_shouldReturnFalseWhenCurrVersionBeforeTarVersion() {
        for (String[] versions : versionBefore) {
            assertFalse(ObSdkUtils.versionAfter(versions[0], versions[1]));
        }
    }

    @Test
    public void versionAfter_shouldReturnTrueWhenCurrVersionEqualsTarVersion() {
        for (String[] versions : versionEqual) {
            assertTrue(ObSdkUtils.versionAfter(versions[0], versions[1]));
        }
    }

    @Test
    public void versionAfter_shouldReturnFalseWhenCurrVersionAfterTarVersion() {
        for (String[] versions : versionAfter) {
            assertTrue(ObSdkUtils.versionAfter(versions[0], versions[1]));
        }
    }

    @Test
    public void versionShouldAfter_shouldSuccessWhenCurrVersionAfterTarVersion() {
        for (String[] versions : versionAfter) {
            ObSdkUtils.versionShouldAfter(versions[0], versions[1]);
        }
    }

    @Test
    public void versionShouldAfter_shouldSuccessWhenCurrVersionEqualsTarVersion() {
        for (String[] versions : versionEqual) {
            ObSdkUtils.versionShouldAfter(versions[0], versions[1]);
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void versionShouldAfter_shouldThrowExceptionWhenCurrVersionBeforeTarVersion() {
        for (String[] versions : versionBefore) {
            ObSdkUtils.versionShouldAfter(versions[0], versions[1]);
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void versionShouldBefore_shouldSuccessWhenCurrVersionAfterTarVersion() {
        for (String[] versions : versionAfter) {
            ObSdkUtils.versionShouldBefore(versions[0], versions[1]);
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void versionShouldBefore_shouldSuccessWhenCurrVersionEqualsTarVersion() {
        for (String[] versions : versionEqual) {
            ObSdkUtils.versionShouldBefore(versions[0], versions[1]);
        }
    }

    @Test
    public void versionShouldBefore_shouldSuccessWhenCurrVersionBeforeTarVersion() {
        for (String[] versions : versionBefore) {
            ObSdkUtils.versionShouldBefore(versions[0], versions[1]);
        }
    }

    @Test
    public void restrictValue_shouldReturnMaxValueWhenInputLargerThanMaxValue() {
        assertEquals(200L, ObSdkUtils.restrictValue(1L, 200L, 300L));
    }

    @Test
    public void restrictValue_shouldReturnMinValueWhenInputLessThanMinValue() {
        assertEquals(1L, ObSdkUtils.restrictValue(1L, 200L, 0L));
        assertEquals(1L, ObSdkUtils.restrictValue(1L, 200L, -10L));
        assertEquals(-1L, ObSdkUtils.restrictValue(-1L, 200L, -10L));
    }

    @Test
    public void restrictValue_shouldReturnInputValueWhenInputBetweenMinAndMax() {
        assertEquals(10L, ObSdkUtils.restrictValue(1L, 200L, 10L));
    }

    @Test
    public void splitValueUnit_empty_text() {
        String[] expected = {"", ""};

        String[] valueUnit = ObSdkUtils.splitValueUnit("");

        Assert.assertArrayEquals(expected, valueUnit);
    }

    @Test
    public void splitValueUnit_no_unit() {
        String[] expected = {"12", ""};

        String[] valueUnit = ObSdkUtils.splitValueUnit("12");

        Assert.assertArrayEquals(expected, valueUnit);
    }

    @Test
    public void splitValueUnit_without_blank() {
        String[] expected = {"12", "GB"};

        String[] valueUnit = ObSdkUtils.splitValueUnit("12GB");

        Assert.assertArrayEquals(expected, valueUnit);
    }

    @Test
    public void splitValueUnit_with_blank() {
        String[] expected = {"12", "GB"};

        String[] valueUnit = ObSdkUtils.splitValueUnit("12 GB");

        Assert.assertArrayEquals(expected, valueUnit);
    }

    @Test
    public void parseTenantGroups_null() {
        String tenantGroups = null;
        List<String> tenantGroupList = ObSdkUtils.parseTenantGroups(tenantGroups);
        Assert.assertEquals(0, tenantGroupList.size());
    }

    @Test
    public void parseTenantGroups_without_bracket() {
        String tenantGroups = "abc";
        thrown.expect(IllegalArgumentException.class);
        List<String> tenantGroupList = ObSdkUtils.parseTenantGroups(tenantGroups);
    }

    @Test
    public void parseTenantGroups_with_one_bracket() {
        String tenantGroups = new String("(abc)");
        thrown.expect(IllegalArgumentException.class);
        List<String> tenantGroupList = ObSdkUtils.parseTenantGroups(tenantGroups);
    }

    @Test
    public void parseTenantGroups_with_bracket_tenants() {
        String tenantGroups = "(abc,def)";
        List<String> tenantGroupList = ObSdkUtils.parseTenantGroups(tenantGroups);
        Assert.assertEquals(1, tenantGroupList.size());
    }


    @Test
    public void parseTenantGroups_with_two_bracket() {
        String tenantGroups = "((abc,def),(t1, t2))";
        List<String> tenantGroupList = ObSdkUtils.parseTenantGroups(tenantGroups);
        Assert.assertEquals(1, tenantGroupList.size());
    }

    @Test
    public void parseTenantGroups_with_two_bracket_two() {
        String tenantGroups = "((abc,def),(t1, t2)), (t3,t4)";
        List<String> tenantGroupList = ObSdkUtils.parseTenantGroups(tenantGroups);
        Assert.assertEquals(2, tenantGroupList.size());
    }

    @Test
    public void parseTenantGroups_lack_bracket() {
        String tenantGroups = "((abc,def),(t1, t2)";
        thrown.expect(IllegalArgumentException.class);
        List<String> tenantGroupList = ObSdkUtils.parseTenantGroups(tenantGroups);
    }

    @Test
    public void parseTenantGroups_lack_comma() {
        String tenantGroups = "((abc,def),(t1, t2))(t4,t5)";
        thrown.expect(IllegalArgumentException.class);
        List<String> tenantGroupList = ObSdkUtils.parseTenantGroups(tenantGroups);
    }

    @Test
    public void parseTenantGroup_null() {
        String tenantGroup = null;
        List<String> tenants = ObSdkUtils.parseTenantGroup(tenantGroup);
        Assert.assertEquals(0, tenants.size());
    }

    @Test
    public void parseTenantGroup_row() {
        String tenantGroup = "a,b,c";
        List<String> tenants = ObSdkUtils.parseTenantGroup(tenantGroup);
        Assert.assertEquals(3, tenants.size());
        List<String> result = Arrays.asList("a", "b", "c");
        Assert.assertEquals(tenants, result);
    }

    @Test
    public void parseTenantGroup_array() {
        String tenantGroup = "(a,b,c),(d,e,f)";
        List<String> tenants = ObSdkUtils.parseTenantGroup(tenantGroup);
        Assert.assertEquals(6, tenants.size());
        List<String> result = Arrays.asList("a", "b", "c", "d", "e", "f");
        Assert.assertEquals(tenants, result);
    }

    @Test
    public void parseTenantGroup_invalid_array() {
        String tenantGroup = "(a,b,c),(d,e,f";
        thrown.expect(IllegalArgumentException.class);
        ObSdkUtils.parseTenantGroup(tenantGroup);
    }

    @Test
    public void userObjectIdToSysObjectId_all_table_table() {
        long result = ObSdkUtils.userObjectIdToSysObjectId(1100611139403911L);
        Assert.assertEquals(1099511627911L, result);
    }

    @Test
    public void sysObjectIdToUserObjectId_oceanbase_database() {
        long result = ObSdkUtils.sysObjectIdToUserObjectId(1099511627777L, 1001L);
        Assert.assertEquals(1100611139403777L, result);
    }

    @Test
    public void isServerActive_true() {
        ObServer obServer = ObSdkTestUtils.buildNormalObServer("127.0.0.1", 2881);
        assertTrue(ObSdkUtils.isServerActive(obServer));
    }

    @Test
    public void isServerActive_false() {
        ObServer obServer = ObSdkTestUtils.buildInactiveObServer("127.0.0.1", 2881);
        assertFalse(ObSdkUtils.isServerActive(obServer));
    }

    @Test
    public void isServerStopped_true() {
        ObServer obServer = ObSdkTestUtils.buildStoppedObServer("127.0.0.1", 2881);
        assertTrue(ObSdkUtils.isServerStopped(obServer));
    }

    @Test
    public void isServerStopped_false() {
        ObServer obServer = ObSdkTestUtils.buildNormalObServer("127.0.0.1", 2881);
        assertFalse(ObSdkUtils.isServerStopped(obServer));
    }

    @Test
    public void checkConnection_test_success() throws SQLException {
        Connection connection = Mockito.mock(Connection.class);
        Statement statement = Mockito.mock(Statement.class);
        Mockito.when(connection.createStatement()).thenReturn(statement);
        Mockito.when(statement.executeQuery(Mockito.anyString())).thenReturn(null);
        boolean valid = ObSdkUtils.checkConnection(connection, "SELECT 1 FROM DUAL", 2);
        Assert.assertTrue(valid);
    }

    @Test
    public void checkConnection_test_failed() throws SQLException {
        Connection connection = Mockito.mock(Connection.class);
        Statement statement = Mockito.mock(Statement.class);
        Mockito.when(connection.createStatement()).thenReturn(statement);
        Mockito.when(statement.executeQuery(Mockito.anyString())).thenThrow(new RuntimeException());
        boolean valid = ObSdkUtils.checkConnection(connection, "SELECT 1 FROM DUAL", 2);
        Assert.assertFalse(valid);
    }

    @Test
    public void parseVersion_expect_ok() {
        String versionComment =
                "OceanBase 2.2.77 (r20210712224820-a153ceb17c858db62c8f35fa4acf61ec7b0a4877) (Built Jul 12 2021 23:21:12)";
        assertEquals("2.2.77", ObSdkUtils.parseVersionComment(versionComment));
    }

    @Test(expected = OceanBaseException.class)
    public void parseVersion_expect_exception() {
        ObSdkUtils.parseVersionComment("");
    }
}
