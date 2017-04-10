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

import static java.lang.Math.min;
import static java.util.stream.Collectors.toList;

/**
 * Splits the backends and frontends to even groups with one frontend and n backends per group
 */
public class EqualSplitOrchestrator implements Orchestrator {
    private final List<List<INodeEntry>> groups;
    private final String primaryNodeType;
    private final int totalGroups;
    private int currentGroupIndex = 0;
    private Iterator<INodeEntry> currentGroupIterator;
    private List<INodeEntry> nodesBeingProcessed;

    public EqualSplitOrchestrator(final StepExecutionContext context, final Collection<INodeEntry> nodes,
                                  final String primaryNodeType) {
        this.primaryNodeType = primaryNodeType;
        groups = groupNodes(nodes);
        totalGroups = groups.size();
        currentGroupIterator = groups.get(0).iterator();
        nodesBeingProcessed = new ArrayList<>();

        if (context != null && context.getExecutionListener() != null) {
            context.getExecutionListener().log(
                    3,
                    "EqualSplitOrchestrator primary node type: " +
                            primaryNodeType
            );
        }
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

    private boolean isPrimaryNode(final INodeEntry node) {
        return node.getNodename().contains(primaryNodeType);
    }

    private boolean isSecondaryNode(final INodeEntry node) {
        return !isPrimaryNode(node);
    }

    public List<List<INodeEntry>> groupNodes(final Collection<INodeEntry> nodes) {
        final List<INodeEntry> primaryNodes = nodes.stream()
                .filter(this::isPrimaryNode)
                .collect(toList());

        final List<INodeEntry> secondaryNodes = nodes.stream()
                .filter(this::isSecondaryNode)
                .collect(toList());

        final List<List<INodeEntry>> groups = new ArrayList<>();
        final List<Integer> secondaryGroupIndexes = computeSecondaryNodeSplitting(primaryNodes.size(),
                secondaryNodes.size());

        for (int i = 0; i < primaryNodes.size(); i++) {
            final List<INodeEntry> group = new ArrayList<>(Collections.singletonList(primaryNodes.get(i)));
            group.addAll(secondaryNodes.subList(secondaryGroupIndexes.get(i), secondaryGroupIndexes.get(i+1)));
            groups.add(group);
        }

        System.err.println("Resolved groups: " + groups);
        return groups;
    }

    static List<Integer> computeSecondaryNodeSplitting(final int primaryNodeCount, final int secondaryNodeCount) {
        if (secondaryNodeCount == 1 || primaryNodeCount == 1 || primaryNodeCount > secondaryNodeCount) {
            throw new RuntimeException("Cannot perform a zero downtime release with the given number of nodes");
        }

        final int groupSize = secondaryNodeCount / primaryNodeCount;
        final int remainder = secondaryNodeCount % primaryNodeCount;
        final List<Integer> indexes = new ArrayList<Integer>() {{
            add(0);
        }};

        for (int i = 1; i <= primaryNodeCount; i++) {
            if (remainder > i-1) {
                indexes.add(min(indexes.get(i-1) + groupSize + 1, secondaryNodeCount));
            } else {
                indexes.add(min(indexes.get(i-1) + groupSize, secondaryNodeCount));
            }
        }
        return indexes;
    }
}
