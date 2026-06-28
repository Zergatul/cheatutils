package com.zergatul.cheatutils.configs;

public class McpServerConfig extends ModuleConfig {

    public boolean allowSavingScripts;
    public boolean allowEvaluatingExpressions;
    public boolean allowExecutingCode;
    public boolean allowScreenshots;

    public McpServerConfig() {
        allowSavingScripts = false;
        allowEvaluatingExpressions = false;
        allowExecutingCode = false;
        allowScreenshots = false;
    }
}