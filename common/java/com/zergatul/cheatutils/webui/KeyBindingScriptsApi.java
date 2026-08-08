package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.scripting.KeyBindings;
import com.zergatul.scripting.DiagnosticMessage;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.HttpException;

import java.util.List;

public class KeyBindingScriptsApi extends ApiBase {

    @Override
    public String getRoute() {
        return "keybinding-scripts";
    }

    @Override
    public boolean requiresJsonContentType() {
        return true;
    }

    @Override
    public String get() {
        String[] bindings = ConfigStore.instance.getConfig().keyBindingsConfig.bindings;
        return gson.toJson(KeyBindings.instance.list().stream()
                .map(script -> new Script(script.name, ArrayUtils.indexOf(bindings, script.name)))
                .toArray());
    }

    @Override
    public String get(String id) throws ApiException {
        requireName(id, "id");
        KeyBindings.Script script = KeyBindings.instance.get(id);
        return script == null ? gson.toJson((Object) null) : gson.toJson(new Script(script));
    }

    @Override
    public String put(String id, String body) throws HttpException {
        requireName(id, "id");
        Script request = parse(body);
        List<DiagnosticMessage> messages;
        try {
            messages = ClientThreadDispatcher.call(
                    () -> KeyBindings.instance.update(id, request.name, request.code));
        } catch (IllegalArgumentException e) {
            throw new ApiException(e.getMessage(), HttpResponseCodes.BAD_REQUEST, e);
        }
        if (!messages.isEmpty()) {
            return gson.toJson(messages);
        }
        ConfigStore.instance.requestWrite();
        return "{ \"ok\": true }";
    }

    @Override
    public String post(String body) throws HttpException {
        Script request = parse(body);
        List<DiagnosticMessage> messages;
        try {
            messages = ClientThreadDispatcher.call(
                    () -> KeyBindings.instance.add(request.name, request.code, false));
        } catch (IllegalArgumentException e) {
            throw new ApiException(e.getMessage(), HttpResponseCodes.BAD_REQUEST, e);
        }
        if (!messages.isEmpty()) {
            return gson.toJson(messages);
        }
        ConfigStore.instance.requestWrite();
        return "{ \"ok\": true }";
    }

    @Override
    public String delete(String id) throws HttpException {
        requireName(id, "id");
        ClientThreadDispatcher.run(() -> KeyBindings.instance.remove(id));
        ConfigStore.instance.requestWrite();
        return "true";
    }

    private Script parse(String body) throws ApiException {
        Script script = WebHelper.parseJson(gson, body, Script.class);
        script.name = requireName(script.name, "name");
        script.code = WebHelper.requireNonBlankField(script.code, "code");
        return script;
    }

    private String requireName(String value, String field) throws ApiException {
        return WebHelper.requireNonBlankField(value, field);
    }

    public static class Script {
        public String name;
        public String code;
        public int key;

        public Script(String name, int key) {
            this.name = name;
            this.key = key;
        }

        public Script(KeyBindings.Script script) {
            name = script.name;
            code = script.code;
        }
    }
}