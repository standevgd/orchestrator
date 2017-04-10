/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.rundeck.plugin.orchestrator;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.execution.workflow.StepExecutionContext;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty;
import com.dtolabs.rundeck.plugins.orchestrator.Orchestrator;
import com.dtolabs.rundeck.plugins.orchestrator.OrchestratorPlugin;

import java.util.Collection;

@Plugin(name = "equalGroupSplit", service = ServiceNameConstants.Orchestrator)
@PluginDescription(title="Equal group split", description="Split nodes into groups of 1 primary + n secondary nodes " +
        "and process each group separately")
public class EqualSplitOrchestratorPlugin implements OrchestratorPlugin {
    @PluginProperty(title="frontend identifier",
            description = "Nodes with names containing this word will be considered as primary (frontends)," +
                    " all the rest secondary (backends)",
            defaultValue = "balancer", required = true)
    String primaryNodeType;

    @Override
    public Orchestrator createOrchestrator(final StepExecutionContext context, final Collection<INodeEntry> nodes) {
        return new EqualSplitOrchestrator(context, nodes, primaryNodeType);
    }
}
