/*
 * Applitools software.
 */
package com.applitools.eyes;

import com.applitools.utils.ArgumentGuard;
import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * Encapsulates data required to start session using the Session API.
 */

/*
 * We name the root value "startInfo" since this is a requirement of
 * the agent's web API.
 */
@SuppressWarnings("UnusedDeclaration")
@JsonRootName(value = "startInfo")
class SessionStartInfo {
    private String agentId;
    private String appIdOrName;
    private String verId;
    private String scenarioIdOrName;
    private BatchInfo batchInfo;
    private String envName;
    private AppEnvironment environment;
    private String branchName;
    private String parentBranchName;
    private ImageMatchSettings defaultMatchSettings;

    public SessionStartInfo(String agentId, String appIdOrName, String verId,
                            String scenarioIdOrName, BatchInfo batchInfo,
                            String envName, AppEnvironment environment,
                            ImageMatchSettings defaultMatchSettings, String branchName,
                            String parentBranchName) {
        ArgumentGuard.notNullOrEmpty(agentId, "agentId");
        ArgumentGuard.notNullOrEmpty(appIdOrName, "appIdOrName");
        ArgumentGuard.notNullOrEmpty(scenarioIdOrName, "scenarioIdOrName");
        ArgumentGuard.notNull(batchInfo, "batchInfo");
        ArgumentGuard.notNull(environment, "environment");
        ArgumentGuard.notNull(defaultMatchSettings, "defaultMatchSettings");
        this.agentId = agentId;
        this.appIdOrName = appIdOrName;
        this.verId = verId;
        this.scenarioIdOrName = scenarioIdOrName;
        this.batchInfo = batchInfo;
        this.envName = envName;
        this.environment = environment;
        this.defaultMatchSettings = defaultMatchSettings;
        this.branchName = branchName;
        this.parentBranchName = parentBranchName;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getAppIdOrName() {
        return appIdOrName;
    }

    public String getVerId() {
        return verId;
    }

    public String getScenarioIdOrName() {
        return scenarioIdOrName;
    }

    public BatchInfo getBatchInfo() {
        return batchInfo;
    }

    public String getEnvName() {
        return envName;
    }

    public AppEnvironment getEnvironment() {
        return environment;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getParentBranchName() {
        return parentBranchName;
    }

    public ImageMatchSettings getDefaultMatchSettings() {
        return defaultMatchSettings;
    }
}
