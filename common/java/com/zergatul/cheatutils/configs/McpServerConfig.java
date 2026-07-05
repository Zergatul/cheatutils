package com.zergatul.cheatutils.configs;

public class McpServerConfig extends ModuleConfig {

    public boolean allowSavingScripts;
    public boolean allowEvaluatingExpressions;

    public McpServerConfig() {
        allowSavingScripts = false;
        allowEvaluatingExpressions = false;
    }
}