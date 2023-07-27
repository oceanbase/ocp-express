package com.oceanbase.ocp.obsdk.operator.cluster;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.jdbc.core.RowMapper;

import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObCollation;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObZone;
import com.oceanbase.ocp.obsdk.operator.cluster.model.RootServiceEvent;

@RunWith(PowerMockRunner.class)
public class MysqlClusterOperatorTest {

    @Mock
    private ObConnectTemplate connectTemplate;

    private MysqlClusterOperator operator;

    @Before
    public void setup() {
        operator = new MysqlClusterOperator(connectTemplate);
    }

    @Test
    public void listZones_test() {
        when(connectTemplate.getObVersion()).thenReturn("2.2.77").thenReturn("4.0.0.0");
        when(connectTemplate.query(anyString(), any())).thenReturn(new ArrayList<>());
        List<ObZone> obZones = operator.listZones();
        obZones = operator.listZones();
        assertNotNull(obZones);
    }

    @Test
    public void showCollection_success() {
        when(connectTemplate.getObVersion()).thenReturn("2.2.77").thenReturn("4.0.0.0");
        when(connectTemplate.query(anyString(), any())).thenReturn(new ArrayList<>());
        List<ObCollation> obCollations = operator.showCollation();
        obCollations = operator.showCollation();
        assertNotNull(obCollations);
    }

    @Test
    public void listUnitEventDesc_success() {
        when(connectTemplate.getObVersion()).thenReturn("2.2.77").thenReturn("4.0.0.0");
        when(connectTemplate.namedQuery(anyString(), ArgumentMatchers.any(),
                ArgumentMatchers.<RowMapper<RootServiceEvent>>any()))
                        .thenReturn(Collections.singletonList(new RootServiceEvent()));
        List<RootServiceEvent> rootServiceEvents =
                operator.listUnitEventDesc(Arrays.asList(1L, 2L), Timestamp.from(Instant.EPOCH),
                        Timestamp.from(Instant.EPOCH));
        rootServiceEvents = operator.listUnitEventDesc(Arrays.asList(1L, 2L), Timestamp.from(Instant.EPOCH),
                Timestamp.from(Instant.EPOCH));
        assertNotNull(rootServiceEvents);
    }
}
