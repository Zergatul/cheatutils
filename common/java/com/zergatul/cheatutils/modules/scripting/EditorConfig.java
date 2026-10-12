package com.zergatul.cheatutils.modules.scripting;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.MonacoEditorConfig;
import com.zergatul.cheatutils.configs.SettingHandle;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class EditorConfig implements Module {

    public static final EditorConfig INSTANCE = new EditorConfig();

    private final SettingHandle<MonacoEditorConfig> settings = new SettingHandle<>(
            () -> ConfigStore.instance.getConfig().monacoEditor,
            config -> ConfigStore.instance.getConfig().monacoEditor = config);

    private EditorConfig() {
        WebApiRegistry.INSTANCE.register("monaco-editor-settings", settings, MonacoEditorConfig.class);
    }

    public SettingHandle<MonacoEditorConfig> getSettings() {
        return settings;
    }
}