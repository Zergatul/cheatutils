package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.ScriptController;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.HttpException;

import java.util.Optional;

public class ScriptsApi extends ApiBase {

    @Override
    public String getRoute() {
        return "scripts";
    }

    @Override
    public String get() throws HttpException {
        String[] bindings = ConfigStore.instance.getConfig().keyBindingsConfig.bindings;
        return gson.toJson(ScriptController.instance.list().stream().map(s -> {
            int index = ArrayUtils.indexOf(bindings, s.name);
            return new Script(s.name, index);
        }).toArray());
    }

    @Override
    public String get(String id) throws HttpException {
        Optional<ScriptController.Script> optional = ScriptController.instance.list().stream().filter(s -> s.name.equals(id)).findFirst();
        if (!optional.isPresent()) {
            return gson.toJson((Object) null);
        } else {
            return gson.toJson(new Script(optional.get()));
        }
    }

    @Override
    public String put(String id, String body) throws HttpException {
        Script script = gson.fromJson(body, Script.class);
        try {
            ScriptController.instance.update(id, script.name, script.code);
        } catch (Throwable e) {
            throw new HttpException(e.getMessage());
        }
        ConfigStore.instance.requestWrite();
        return get(script.name);
    }

    @Override
    public String post(String body) throws HttpException {
        Script script = gson.fromJson(body, Script.class);
        try {
            ScriptController.instance.add(script.name, script.code);
        }
        catch (Throwable e) {
            throw new HttpException(e.getMessage());
        }
        ConfigStore.instance.requestWrite();
        return get(script.name);
    }

    @Override
    public String delete(String id) throws HttpException {
        ScriptController.instance.remove(id);
        ConfigStore.instance.requestWrite();
        return "true";
    }

    public class Script {
        public String name;
        public String code;
        public int key;

        public Script(String name, int key) {
            this.name = name;
            this.key = key;
        }

        public Script(ScriptController.Script script) {
            name = script.name;
            code = script.code;
        }
    }
}