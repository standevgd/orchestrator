/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.rundeck.plugin.orchestrator;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.execution.workflow.StepExecutionContext;
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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class JdbcOrchestratorTest {

    @Mock
    private StepExecutionContext context;

    @Mock
    private INodeEntry jbalancer1;
    @Mock
    private INodeEntry jbalancer2;

    @Mock
    private INodeEntry proxy1;
    @Mock
    private INodeEntry proxy2;
    @Mock
    private INodeEntry proxy3;

    @Mock
    private NodeStepResult nodeStepResult;

    private JdbcOrchestrator orchestrator;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(jbalancer1.getNodename()).thenReturn("jbalancer1");
        when(jbalancer2.getNodename()).thenReturn("jbalancer2");
        when(proxy1.getNodename()).thenReturn("proxy1");
        when(proxy2.getNodename()).thenReturn("proxy2");
        when(proxy3.getNodename()).thenReturn("proxy3");

        final List<INodeEntry> nodes = new ArrayList<>(asList(jbalancer1, jbalancer2, proxy1, proxy2, proxy3));

        orchestrator = new JdbcOrchestrator(context, nodes);
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
    public void computeBackendGroups() {
        final List<Integer> indexes = JdbcOrchestrator.computeBackendGroupIndexes(3, 10);

        assertThat(indexes, equalTo(asList(0, 4, 7, 10)));
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
