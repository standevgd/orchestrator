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
import java.util.List;

import static java.util.stream.Collectors.toList;

public class JdbcOrchestrator implements Orchestrator {
    public static final String SERVICE_PROVIDER_TYPE = "jdbc-orchestrator";
    protected List<INodeEntry> balancers = new ArrayList<>();
    protected List<INodeEntry> proxies = new ArrayList<>();
    private List<INodeEntry> group = new ArrayList<>();
    private List<INodeEntry> inProgress = new ArrayList<>();

    public JdbcOrchestrator(final StepExecutionContext context, final Collection<INodeEntry> nodes) {
        this.balancers = nodes.stream()
                .filter(this::isBalancer)
                .collect(toList());

        this.proxies = nodes.stream()
                .filter(this::isProxy)
                .collect(toList());
    }

    public INodeEntry nextNode() {
        if (!hasBalancer()) {
            if (balancers.isEmpty()) {
                return null;
            }

            final INodeEntry balancer = balancers.remove(0);
            group.add(balancer);
            inProgress.add(balancer);
            return balancer;
        }

        if (proxies.isEmpty() || hasEnoughProxies()) {
            return null;
        }

        final INodeEntry proxy = proxies.remove(0);
        group.add(proxy);
        inProgress.add(proxy);

        return proxy;
    }

    private boolean hasEnoughProxies() {
        return group.stream().filter(this::isProxy).count() >= Math.min(proxies.size(), 2);
    }

    private boolean hasBalancer() {
        return group.stream().anyMatch(this::isBalancer);
    }

    public void returnNode(final INodeEntry node, final boolean b, final NodeStepResult nodeStepResult) {
        inProgress.remove(node);
        if (inProgress.isEmpty() && hasBalancer() && hasEnoughProxies()) {
            group.clear();
        }
    }

    public boolean isComplete() {
        return balancers.isEmpty() && proxies.isEmpty();
    }

    private boolean isBalancer(final INodeEntry node) {
        return node.getNodename().contains("balancer");
    }

    private boolean isProxy(final INodeEntry node) {
        return !isBalancer(node);
    }
}
