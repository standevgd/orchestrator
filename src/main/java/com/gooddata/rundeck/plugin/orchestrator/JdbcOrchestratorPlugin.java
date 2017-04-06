/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.rundeck.plugin.orchestrator;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.execution.workflow.StepExecutionContext;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty;
import com.dtolabs.rundeck.plugins.orchestrator.Orchestrator;
import com.dtolabs.rundeck.plugins.orchestrator.OrchestratorPlugin;

import java.util.Collection;

@Plugin(name = JdbcOrchestrator.SERVICE_PROVIDER_TYPE, service = "Orchestrator")
public class JdbcOrchestratorPlugin implements OrchestratorPlugin {
    @PluginProperty(title="node type to group by",
            description = "Nodes with names containing this word will be considered as primary, all the rest secondary",
            defaultValue = "balancer", required = true)
    String primaryNodeType;

    public JdbcOrchestratorPlugin() {
    }

    public JdbcOrchestratorPlugin(final String primaryNodeType) {
        this.primaryNodeType = primaryNodeType;
    }

    @Override
    public Orchestrator createOrchestrator(final StepExecutionContext context, final Collection<INodeEntry> nodes) {
        return new JdbcOrchestrator(context, nodes);
    }
}
