package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.ScriptsController;
import com.zergatul.scripting.DiagnosticMessage;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.HttpException;

import java.util.List;
import java.util.Optional;

public class KeyBindingScriptsApi extends ApiBase {

    @Override
    public String getRoute() {
        return "keybinding-scripts";
    }

    @Override
    public String get() throws HttpException {
        String[] bindings = ConfigStore.instance.getConfig().keyBindingsConfig.bindings;
        return gson.toJson(ScriptsController.instance.list().stream().map(s -> {
            int index = ArrayUtils.indexOf(bindings, s.name);
            return new Script(s.name, index);
        }).toArray());
    }

    @Override
    public String get(String id) throws HttpException {
        Optional<ScriptsController.Script> optional = ScriptsController.instance.list().stream().filter(s -> s.name.equals(id)).findFirst();
        if (optional.isEmpty()) {
            return gson.toJson((Object) null);
        } else {
            return gson.toJson(new Script(optional.get()));
        }
    }

    @Override
    public String put(String id, String body) throws HttpException {
        Script script = gson.fromJson(body, Script.class);
        try {
            List<DiagnosticMessage> messages = ScriptsController.instance.update(id, script.name, script.code);
            if (!messages.isEmpty()) {
                return gson.toJson(messages);
            }
        } catch (Throwable e) {
            throw new HttpException(e.getMessage());
        }
        ConfigStore.instance.requestWrite();
        return "{ \"ok\": true }";
    }

    @Override
    public String post(String body) throws HttpException {
        Script script = gson.fromJson(body, Script.class);
        try {
            List<DiagnosticMessage> messages = ScriptsController.instance.add(script.name, script.code, false);
            if (!messages.isEmpty()) {
                return gson.toJson(messages);
            }
        }
        catch (Throwable e) {
            throw new HttpException(e.getMessage());
        }
        ConfigStore.instance.requestWrite();
        return "{ \"ok\": true }";
    }

    @Override
    public String delete(String id) throws HttpException {
        ScriptsController.instance.remove(id);
        ConfigStore.instance.requestWrite();
        return "true";
    }

    public static class Script {
        public String name;
        public String code;
        public int key;

        public Script(String name, int key) {
            this.name = name;
            this.key = key;
        }

        public Script(ScriptsController.Script script) {
            name = script.name;
            code = script.code;
        }
    }
}