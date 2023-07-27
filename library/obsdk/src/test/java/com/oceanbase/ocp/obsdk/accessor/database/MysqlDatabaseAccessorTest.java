package com.oceanbase.ocp.obsdk.accessor.database;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import com.oceanbase.ocp.obsdk.accessor.database.model.AlterDatabaseInput;
import com.oceanbase.ocp.obsdk.accessor.database.model.CreateDatabaseInput;
import com.oceanbase.ocp.obsdk.accessor.database.model.ObDatabase;
import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class MysqlDatabaseAccessorTest {

    private static ObConnectTemplate template;

    @Before
    public void setUp() throws Exception {
        template = mock(ObConnectTemplate.class);
        when(template.queryForObject(anyString(), any(), any(RowMapper.class))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            Object[] params = (Object[]) args[1];
            String dbName = (String) params[0];
            return buildObDatabase(dbName);
        });
    }

    @After
    public void tearDown() throws Exception {}

    @Test
    public void createDatabase_sqlShouldCorrectWhenInputContainsOnlyDbname() {
        MysqlDatabaseAccessor accessor = new MysqlDatabaseAccessor(template);
        CreateDatabaseInput input = CreateDatabaseInput.builder().name("db1").build();
        ObDatabase database = accessor.createDatabase(input);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> argsCaptor = ArgumentCaptor.forClass(Object.class);
        verify(template).update(sqlCaptor.capture(), argsCaptor.capture());

        Assert.assertEquals("CREATE DATABASE IF NOT EXISTS `db1`", sqlCaptor.getValue());
        Assert.assertEquals(0, argsCaptor.getAllValues().size());
        Assert.assertEquals("db1", database.getName());
    }

    @Test
    public void createDatabase_sqlShouldCorrectWhenInputReadonlyFalse() {
        MysqlDatabaseAccessor accessor = new MysqlDatabaseAccessor(template);
        CreateDatabaseInput input = CreateDatabaseInput.builder().name("db1").readonly(false).build();
        ObDatabase database = accessor.createDatabase(input);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> argsCaptor = ArgumentCaptor.forClass(Object.class);
        verify(template).update(sqlCaptor.capture(), argsCaptor.capture());

        Assert.assertEquals("CREATE DATABASE IF NOT EXISTS `db1` READ WRITE", sqlCaptor.getValue());
        Assert.assertEquals(0, argsCaptor.getAllValues().size());
        Assert.assertEquals("db1", database.getName());
    }

    @Test
    public void createDatabase_sqlShouldCorrectWhenInputContainsAllParam() {
        MysqlDatabaseAccessor accessor = new MysqlDatabaseAccessor(template);
        CreateDatabaseInput input = CreateDatabaseInput.builder().name("db1").collation("utf8mb4_general_ci")
                .readonly(true).build();
        ObDatabase database = accessor.createDatabase(input);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> argsCaptor = ArgumentCaptor.forClass(Object.class);
        verify(template).update(sqlCaptor.capture(), argsCaptor.capture());

        Assert.assertEquals("CREATE DATABASE IF NOT EXISTS `db1` DEFAULT COLLATE = ? READ ONLY",
                sqlCaptor.getValue());
        Assert.assertEquals(1, argsCaptor.getAllValues().size());
        Assert.assertEquals("db1", database.getName());
    }

    @Test(expected = NullPointerException.class)
    public void createDatabase_shouldThrowExceptionWhenInputNull() {
        MysqlDatabaseAccessor accessor = new MysqlDatabaseAccessor(template);
        accessor.createDatabase(null);
    }

    @Test(expected = NullPointerException.class)
    public void createDatabase_shouldThrowExceptionWhenInputDatabaseNameNull() {
        MysqlDatabaseAccessor accessor = new MysqlDatabaseAccessor(template);
        CreateDatabaseInput input = CreateDatabaseInput.builder().build();
        accessor.createDatabase(input);
    }

    @Test
    public void alterDatabase_sqlShouldCorrectWhenInputContainsAllParam() {
        MysqlDatabaseAccessor accessor = new MysqlDatabaseAccessor(template);
        AlterDatabaseInput input = AlterDatabaseInput.builder().name("db1").collation("utf8mb4_general_ci")
                .readonly(true).build();
        ObDatabase database = accessor.alterDatabase(input);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> argsCaptor = ArgumentCaptor.forClass(Object.class);
        verify(template).update(sqlCaptor.capture(), argsCaptor.capture());

        Assert.assertEquals("ALTER DATABASE `db1` SET DEFAULT COLLATE = ? READ ONLY",
                sqlCaptor.getValue());
        Assert.assertEquals(1, argsCaptor.getAllValues().size());
        Assert.assertEquals("utf8mb4_general_ci", argsCaptor.getAllValues().get(0));
        Assert.assertEquals("db1", database.getName());
    }

    @Test
    public void alterDatabase_sqlShouldCorrectWhenInputReadonlyFalse() {
        MysqlDatabaseAccessor accessor = new MysqlDatabaseAccessor(template);
        AlterDatabaseInput input = AlterDatabaseInput.builder().name("db1").readonly(false).build();
        ObDatabase database = accessor.alterDatabase(input);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> argsCaptor = ArgumentCaptor.forClass(Object.class);
        verify(template).update(sqlCaptor.capture(), argsCaptor.capture());

        Assert.assertEquals("ALTER DATABASE `db1` SET READ WRITE", sqlCaptor.getValue());
        Assert.assertEquals(0, argsCaptor.getAllValues().size());
        Assert.assertEquals("db1", database.getName());
    }

    @Test(expected = NullPointerException.class)
    public void alterDatabase_shouldThrowExceptionWhenInputNull() {
        MysqlDatabaseAccessor accessor = new MysqlDatabaseAccessor(template);
        accessor.alterDatabase(null);
    }

    @Test(expected = NullPointerException.class)
    public void alterDatabase_shouldThrowExceptionWhenInputDatabaseNameNull() {
        MysqlDatabaseAccessor accessor = new MysqlDatabaseAccessor(template);
        AlterDatabaseInput input = AlterDatabaseInput.builder().build();
        accessor.alterDatabase(input);
    }

    @Test(expected = IllegalArgumentException.class)
    public void alterDatabase_shouldThrowExceptionWhenInputParamNull() {
        MysqlDatabaseAccessor accessor = new MysqlDatabaseAccessor(template);
        AlterDatabaseInput input = AlterDatabaseInput.builder().name("db1").build();
        accessor.alterDatabase(input);
    }

    @Test
    public void dropDatabase_sqlShouldSuccessWhenInputNormal() {
        MysqlDatabaseAccessor accessor = new MysqlDatabaseAccessor(template);
        accessor.dropDatabase("db1");

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).update(sqlCaptor.capture());

        Assert.assertEquals("DROP DATABASE IF EXISTS `db1`", sqlCaptor.getValue());
    }

    @Test
    public void listDatabases() {
        List<String> dbNames = Stream.of("db1", "db2", "db3").collect(Collectors.toList());
        List<ObDatabase> dbList =
                Stream.of("db1", "db2", "db3", "db4").map(this::buildObDatabase).collect(Collectors.toList());
        when(template.query(anyString(), any(SingleColumnRowMapper.class))).thenReturn(dbNames);
        when(template.query(anyString(), any(BeanPropertyRowMapper.class))).thenReturn(dbList);
        MysqlDatabaseAccessor accessor = new MysqlDatabaseAccessor(template);
        List<ObDatabase> databases = accessor.listDatabases();
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<RowMapper> mapperCaptor = ArgumentCaptor.forClass(RowMapper.class);
        verify(template, times(2)).query(sqlCaptor.capture(), mapperCaptor.capture());
        Assert.assertEquals(3, databases.size());
        Assert.assertEquals(2, sqlCaptor.getAllValues().size());
        Assert.assertEquals("SHOW DATABASES", sqlCaptor.getAllValues().get(0));
        assertThat(sqlCaptor.getAllValues().get(1), containsString("DBA_OB_DATABASES"));
    }

    private ObDatabase buildObDatabase(String dbName) {
        ObDatabase database = new ObDatabase();
        database.setGmtCreate(new Timestamp(System.currentTimeMillis()));
        database.setName(dbName);
        database.setCollationType(46L);
        database.setReadOnly(1L);
        return database;
    }
}
