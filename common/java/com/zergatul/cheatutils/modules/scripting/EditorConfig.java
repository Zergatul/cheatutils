package com.zergatul.cheatutils.modules.scripting;

import com.zergatul.cheatutils.configs.SettingHandle;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.MonacoEditorConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class EditorConfig implements Module {

    public static final EditorConfig INSTANCE = new EditorConfig();

    private final SettingHandle<MonacoEditorConfig> settings = Settings.get(MonacoEditorConfig.class);

    private EditorConfig() {
        WebApiRegistry.INSTANCE.register("monaco-editor-settings", settings, MonacoEditorConfig.class);
    }

    public SettingHandle<MonacoEditorConfig> getSettings() {
        return settings;
    }
}