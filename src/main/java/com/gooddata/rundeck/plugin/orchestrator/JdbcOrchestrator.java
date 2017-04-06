/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.rundeck.plugin.orchestrator;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.execution.workflow.StepExecutionContext;
import com.dtolabs.rundeck.core.execution.workflow.steps.node.NodeStepResult;
import com.dtolabs.rundeck.plugins.orchestrator.Orchestrator;

import java.util.Collection;

public class JdbcOrchestrator implements Orchestrator {
    public static final String SERVICE_PROVIDER_TYPE = "jdbc-orchestrator";

    public JdbcOrchestrator(StepExecutionContext context, Collection<INodeEntry> nodes) {

    }

    public INodeEntry nextNode() {
        return null;
    }

    public void returnNode(final INodeEntry iNodeEntry, final boolean b, final NodeStepResult nodeStepResult) {

    }

    public boolean isComplete() {
        return false;
    }
}
