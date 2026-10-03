package com.zergatul.cheatutils.modules.scripting;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.MonacoEditorConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.SimpleModuleConfigWebApi;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class EditorConfig implements Module {

    public static final EditorConfig INSTANCE = new EditorConfig();

    private EditorConfig() {
        WebApiRegistry.INSTANCE.register(new WebApi());
    }

    private static final class WebApi extends SimpleModuleConfigWebApi<MonacoEditorConfig> {

        public WebApi() {
            super("monaco-editor-settings", MonacoEditorConfig.class);
        }

        @Override
        protected MonacoEditorConfig getConfig() {
            return ConfigStore.instance.getConfig().monacoEditor;
        }

        @Override
        protected void setConfig(MonacoEditorConfig config) {
            ConfigStore.instance.getConfig().monacoEditor = config;
        }
    }
}