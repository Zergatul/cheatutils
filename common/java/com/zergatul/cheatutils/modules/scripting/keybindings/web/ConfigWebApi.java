package com.zergatul.cheatutils.modules.scripting.keybindings.web;

import com.zergatul.cheatutils.configs.modules.KeyBindingEntry;
import com.zergatul.cheatutils.configs.modules.KeyBindingsConfig;
import com.zergatul.cheatutils.modules.scripting.keybindings.KeyBindings;
import com.zergatul.cheatutils.web.WebApiBase;
import org.jspecify.annotations.Nullable;

public class ConfigWebApi extends WebApiBase {

    @Override
    public String getRoute() {
        return "keybindings";
    }

    @Override
    public String get() {
        KeyBindingsConfig config = KeyBindings.INSTANCE.getSettings().get();
        return gson.toJson(config.entries().stream().map(s -> new ScriptInfo(s.name(), s.keyIndex())).toArray());
    }

    @Override
    public String get(String id) {
        KeyBindingsConfig config = KeyBindings.INSTANCE.getSettings().get();
        KeyBindingEntry entry = config.entries().stream()
                .filter(e -> e.name().equals(id))
                .findFirst()
                .orElse(null);
        if (entry != null) {
            return gson.toJson(new ScriptDetails(entry.name(), entry.code()));
        } else {
            return gson.toJson((Object) null);
        }
    }

    @Override
    public String post(String body) throws Exception {
        ScriptInfo entry = gson.fromJson(body, ScriptInfo.class);
        return KeyBindings.INSTANCE.add(entry.name(), entry.keyIndex()).match(
                _ -> "{ \"ok\": true }",
                error -> gson.toJson(new ErrorResponse(error.error())));
    }

    @Override
    public String put(String id, String body) throws Exception {
        ScriptInfo entry = gson.fromJson(body, ScriptInfo.class);
        return KeyBindings.INSTANCE.update(id, entry.name(), entry.keyIndex()).match(
                _ -> "{ \"ok\": true }",
                error -> gson.toJson(new ErrorResponse(error.error())));
    }

    @Override
    public String delete(String id) throws Exception {
        return KeyBindings.INSTANCE.delete(id).match(
                _ -> "{ \"ok\": true }",
                error -> gson.toJson(new ErrorResponse(error.error())));
    }

    public record ScriptInfo(String name, int keyIndex) {}
    public record ScriptDetails(String name, @Nullable String code) {}

    public record ErrorResponse(String error) {}
}