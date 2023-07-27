package com.oceanbase.ocp.obsdk.accessor.variable;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.core.RowMapper;

import com.oceanbase.ocp.obsdk.accessor.variable.model.SetVariableInput;
import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;
import com.oceanbase.ocp.obsdk.enums.VariableValueType;

@RunWith(MockitoJUnitRunner.class)
public class OracleVariableAccessorTest {

    private static ObConnectTemplate template;

    @Before
    public void setUp() throws Exception {
        template = mock(ObConnectTemplate.class);
    }

    @After
    public void tearDown() throws Exception {}

    @Test
    public void showVariables_sqlShouldCorrectWhenNormal() {
        OracleVariableAccessor accessor = new OracleVariableAccessor(template);
        accessor.showVariables();

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<RowMapper> mapperCaptor = ArgumentCaptor.forClass(RowMapper.class);
        verify(template).query(sqlCaptor.capture(), mapperCaptor.capture());

        Assert.assertEquals("SHOW GLOBAL VARIABLES", sqlCaptor.getValue());
    }

    @Test
    public void setVariable_sqlShouldCorrectWhenVariableTypeIsNull() {
        OracleVariableAccessor accessor = new OracleVariableAccessor(template);
        SetVariableInput input = SetVariableInput.builder().name("key1").value("value1").build();
        accessor.setVariable(input);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> argCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(template).update(sqlCaptor.capture(), argCaptor.capture());

        Assert.assertEquals("SET GLOBAL key1 = ?", sqlCaptor.getValue());
        Object arg = argCaptor.getValue();
        Assert.assertEquals("value1", arg);
    }

    @Test
    public void setVariable_sqlShouldCorrectWhenVariableTypeIsInt() {
        OracleVariableAccessor accessor = new OracleVariableAccessor(template);
        SetVariableInput input =
                SetVariableInput.builder().name("key1").value("23").type(VariableValueType.INT).build();
        accessor.setVariable(input);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> argCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(template).update(sqlCaptor.capture(), argCaptor.capture());

        Assert.assertEquals("SET GLOBAL key1 = ?", sqlCaptor.getValue());
        Object arg = argCaptor.getValue();
        Assert.assertEquals(23, (int) arg);
    }

    @Test
    public void setVariable_sqlShouldCorrectWhenVariableTypeIsNumeric() {
        OracleVariableAccessor accessor = new OracleVariableAccessor(template);
        SetVariableInput input =
                SetVariableInput.builder().name("key1").value("23.1").type(VariableValueType.NUMERIC).build();
        accessor.setVariable(input);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> argCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(template).update(sqlCaptor.capture(), argCaptor.capture());

        Assert.assertEquals("SET GLOBAL key1 = ?", sqlCaptor.getValue());
        Object arg = argCaptor.getValue();
        Assert.assertEquals(23.1f, (float) arg, 0.01);
    }

    @Test
    public void setVariable_sqlShouldCorrectWhenVariableTypeIsString() {
        OracleVariableAccessor accessor = new OracleVariableAccessor(template);
        SetVariableInput input =
                SetVariableInput.builder().name("key1").value("value1").type(VariableValueType.STRING).build();
        accessor.setVariable(input);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> argCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(template).update(sqlCaptor.capture(), argCaptor.capture());

        Assert.assertEquals("SET GLOBAL key1 = ?", sqlCaptor.getValue());
        Object arg = argCaptor.getValue();
        Assert.assertEquals("value1", arg);
    }

    @Test
    public void setVariable_sqlShouldCorrectWhenVariableTypeIsBool() {
        OracleVariableAccessor accessor = new OracleVariableAccessor(template);
        SetVariableInput input =
                SetVariableInput.builder().name("key1").value("false").type(VariableValueType.BOOL).build();
        accessor.setVariable(input);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> argCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(template).update(sqlCaptor.capture(), argCaptor.capture());

        Assert.assertEquals("SET GLOBAL key1 = ?", sqlCaptor.getValue());
        Object arg = argCaptor.getValue();
        Assert.assertEquals("false", arg);
    }

    @Test
    public void setVariable_sqlShouldCorrectWhenVariableTypeIsEnum() {
        OracleVariableAccessor accessor = new OracleVariableAccessor(template);
        SetVariableInput input =
                SetVariableInput.builder().name("key1").value("on").type(VariableValueType.ENUM).build();
        accessor.setVariable(input);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> argCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(template).update(sqlCaptor.capture(), argCaptor.capture());

        Assert.assertEquals("SET GLOBAL key1 = ?", sqlCaptor.getValue());
        Object arg = argCaptor.getValue();
        Assert.assertEquals("on", arg);
    }
}
