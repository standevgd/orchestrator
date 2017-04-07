/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.rundeck.plugin.orchestrator;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.execution.workflow.StepExecutionContext;
import com.dtolabs.rundeck.core.execution.workflow.steps.node.NodeStepResult;
import com.dtolabs.rundeck.plugins.orchestrator.Orchestrator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class JdbcOrchestrator implements Orchestrator {
    public static final String SERVICE_PROVIDER_TYPE = "jdbc-orchestrator";
    private final List<List<INodeEntry>> groups;
    private final int totalGroups;
    private int currentGroupIndex = 0;
    private Iterator<INodeEntry> currentGroupIterator;
    private List<INodeEntry> nodesBeingProcessed;

    public JdbcOrchestrator(final StepExecutionContext context, final Collection<INodeEntry> nodes) {
        groups = groupNodes(nodes);
        totalGroups = groups.size();
        currentGroupIterator = groups.get(0).iterator();
        nodesBeingProcessed = new ArrayList<>();
    }

    public INodeEntry nextNode() {
        if (!currentGroupIterator.hasNext()) {
            if (nodesBeingProcessed.isEmpty() && !isComplete()) {
                currentGroupIterator = groups.get(++currentGroupIndex).iterator();
                return currentGroupIterator.next();
            } else {
                return null;
            }
        } else {
            final INodeEntry nextNode = currentGroupIterator.next();
            nodesBeingProcessed.add(nextNode);
            return nextNode;
        }
    }

    public void returnNode(final INodeEntry node, final boolean success, final NodeStepResult nodeStepResult) {
        nodesBeingProcessed.remove(node);
    }

    public boolean isComplete() {
        return currentGroupIndex +1 >= totalGroups && !currentGroupIterator.hasNext();
    }

    private static boolean isBalancer(final INodeEntry node) {
        return node.getNodename().contains("balancer");
    }

    private static boolean isProxy(final INodeEntry node) {
        return !isBalancer(node);
    }

    static List<List<INodeEntry>> groupNodes(final Collection<INodeEntry> nodes) {
        final List<INodeEntry> balancers = nodes.stream()
                .filter(JdbcOrchestrator::isBalancer)
                .collect(toList());

        final List<INodeEntry> proxies = nodes.stream()
                .filter(JdbcOrchestrator::isProxy)
                .collect(toList());

        final List<List<INodeEntry>> groups = new ArrayList<>();
        final List<Integer> backendGroupIndexes = computeBackendGroupIndexes(balancers.size(), proxies.size());

        for (int i = 0; i < balancers.size(); i++) {
            final List<INodeEntry> group = new ArrayList<>(Collections.singletonList(balancers.get(i)));
            group.addAll(proxies.subList(backendGroupIndexes.get(i), backendGroupIndexes.get(i+1)));
            groups.add(group);
        }

        return groups;
    }

    static List<Integer> computeBackendGroupIndexes(final int frontendCount, final int backendCount) {
        if (backendCount == 1 || frontendCount == 1 || frontendCount > backendCount) {
            throw new RuntimeException("Cannot perform a zero downtime release with the given number of nodes");
        }

        final int groupSize = backendCount / frontendCount;
        final int remainder = backendCount % frontendCount;
        final List<Integer> indexes = new ArrayList<Integer>() {{
            add(0);
        }};

        for (int i = 1; i <= frontendCount; i++) {
            if (remainder > i-1) {
                indexes.add(Math.min(indexes.get(i-1) + groupSize + 1, backendCount));
            } else {
                indexes.add(Math.min(indexes.get(i-1) + groupSize, backendCount));
            }
        }
        return indexes;
    }
}
