/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.rundeck.plugin.orchestrator;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.execution.workflow.steps.node.NodeStepResult;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

public class EqualSplitOrchestratorTest {

    @Mock
    private INodeEntry jbalancer1;
    @Mock
    private INodeEntry jbalancer2;
    @Mock
    private INodeEntry jbalancer3;

    @Mock
    private INodeEntry proxy1;
    @Mock
    private INodeEntry proxy2;
    @Mock
    private INodeEntry proxy3;
    @Mock
    private INodeEntry proxy4;
    @Mock
    private INodeEntry proxy5;
    @Mock
    private INodeEntry proxy6;
    @Mock
    private INodeEntry proxy7;
    @Mock
    private INodeEntry proxy8;
    @Mock
    private INodeEntry proxy9;
    @Mock
    private INodeEntry proxy10;

    @Mock
    private NodeStepResult nodeStepResult;

    private EqualSplitOrchestrator orchestrator;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(jbalancer1.getNodename()).thenReturn("jbalancer1");
        when(jbalancer2.getNodename()).thenReturn("jbalancer2");
        when(jbalancer3.getNodename()).thenReturn("jbalancer3");

        when(proxy1.getNodename()).thenReturn("proxy1");
        when(proxy2.getNodename()).thenReturn("proxy2");
        when(proxy3.getNodename()).thenReturn("proxy3");
        when(proxy4.getNodename()).thenReturn("proxy4");
        when(proxy5.getNodename()).thenReturn("proxy5");
        when(proxy6.getNodename()).thenReturn("proxy6");
        when(proxy7.getNodename()).thenReturn("proxy7");
        when(proxy8.getNodename()).thenReturn("proxy8");
        when(proxy9.getNodename()).thenReturn("proxy9");
        when(proxy10.getNodename()).thenReturn("proxy10");

        final List<INodeEntry> nodes = new ArrayList<>(asList(jbalancer1, jbalancer2, proxy1, proxy2, proxy3));

        orchestrator = new EqualSplitOrchestrator(null, nodes, "balancer", null);
    }

    @Test
    public void orchestrator_firstGroup() {
        final List<INodeEntry> nodes = getNextGroup();
        processAllNodes(nodes);

        assertThat(nodes.size(), is(3));
        assertThat(orchestrator.isComplete(), is(false));
        assertThat(orchestrator.nextNode(), is(jbalancer2));
    }

    @Test
    public void orchestrator_secondGroup() {
        processAllNodes(getNextGroup());

        final List<INodeEntry> nodes = getNextGroup();

        assertThat(nodes.size(), is(2));
        assertThat(orchestrator.nextNode(), is(nullValue()));
        assertThat(orchestrator.isComplete(), is(true));
    }

    @Test
    public void groupNodes() {
        final List<INodeEntry> nodes = new ArrayList<>(asList(jbalancer1, jbalancer2, proxy1, proxy2, proxy3, jbalancer3,
                proxy4, proxy5, proxy6, proxy7, proxy8, proxy9, proxy10));
        final List<List<INodeEntry>> nodeGroups = orchestrator.groupNodes(nodes);

        assertThat(nodeGroups.size(), is(3));
        final List<INodeEntry> firstGroup = nodeGroups.get(0);
        final List<INodeEntry> secondGroup = nodeGroups.get(1);
        final List<INodeEntry> thirdGroup = nodeGroups.get(2);

        assertThat(firstGroup.size(), is(5));
        assertThat(secondGroup.size(), is(4));
        assertThat(thirdGroup.size(), is(4));

        assertThat(firstGroup, equalTo(asList(jbalancer1, proxy1, proxy2, proxy3, proxy4)));
        assertThat(secondGroup, equalTo(asList(jbalancer2, proxy5, proxy6, proxy7)));
        assertThat(thirdGroup, equalTo(asList(jbalancer3, proxy8, proxy9, proxy10)));
    }

    @Test
    public void groupNodes_singleGroup() {
        final List<INodeEntry> nodes = new ArrayList<>(asList(jbalancer1, jbalancer2, proxy1, proxy2, proxy3, jbalancer3,
                proxy4, proxy5, proxy6, proxy7, proxy8, proxy9, proxy10));
        final EqualSplitOrchestrator orchestrator = new EqualSplitOrchestrator(null, nodes, "balancer",
                "jbalancer2");

        final List<List<INodeEntry>> nodeGroups = orchestrator.groupNodes(nodes);
        assertThat(nodeGroups.size(), is(1));
        assertThat(nodeGroups.get(0).size(), is(4));
        assertThat(nodeGroups.get(0), equalTo(asList(jbalancer2, proxy5, proxy6, proxy7)));
    }

    @Test
    public void groupNodes_singleGroupNotMatched() {
        final List<INodeEntry> nodes = new ArrayList<>(asList(jbalancer1, jbalancer2, proxy1, proxy2, proxy3, jbalancer3,
                proxy4, proxy5, proxy6, proxy7, proxy8, proxy9, proxy10));
        final EqualSplitOrchestrator orchestrator = new EqualSplitOrchestrator(null, nodes, "balancer",
                "non_existing_balancer");

        final List<List<INodeEntry>> nodeGroups = orchestrator.groupNodes(nodes);
        assertThat(nodeGroups.size(), is(1));
        assertThat(nodeGroups.get(0).isEmpty(), is(true));
    }

    @Test
    public void computeBackendGroupIndexes3Balancers9Proxies() {
        final List<Integer> indexes = EqualSplitOrchestrator.computeSecondaryNodeSplitting(3, 9);

        assertThat(indexes, equalTo(asList(0, 3, 6, 9)));
    }

    @Test
    public void computeBackendGroupIndexes3Balancers10Proxies() {
        final List<Integer> indexes = EqualSplitOrchestrator.computeSecondaryNodeSplitting(3, 10);

        assertThat(indexes, equalTo(asList(0, 4, 7, 10)));
    }

    @Test
    public void computeBackendGroupIndexes3Balancers11Proxies() {
        final List<Integer> indexes = EqualSplitOrchestrator.computeSecondaryNodeSplitting(3, 11);

        assertThat(indexes, equalTo(asList(0, 4, 8, 11)));
    }

    private List<INodeEntry> getNextGroup() {
        final List<INodeEntry> nodes = new ArrayList<>();

        INodeEntry node;
        while ((node = orchestrator.nextNode()) != null) {
            nodes.add(node);
        }
        return nodes;
    }

    private void processAllNodes(List<INodeEntry> nodes) {
        for (final INodeEntry node : nodes) {
            orchestrator.returnNode(node, true, nodeStepResult);
        }
    }
}
