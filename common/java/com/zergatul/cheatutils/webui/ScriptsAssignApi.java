package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.KeyBindingsConfig;
import com.zergatul.cheatutils.modules.scripting.KeyBindings;
import org.apache.http.HttpException;

public class ScriptsAssignApi extends ApiBase {

    @Override
    public String getRoute() {
        return "keybinding-scripts-assign";
    }

    @Override
    public boolean requiresJsonContentType() {
        return true;
    }

    @Override
    public String put(String id, String body) throws HttpException {
        WebHelper.requireNonBlankField(id, "id");
        int index = WebHelper.parseJson(gson, body, int.class);
        if (index < -1 || index >= KeyBindingsConfig.KeysCount) {
            throw new ApiException("Key binding index must be between -1 and " + (KeyBindingsConfig.KeysCount - 1), HttpResponseCodes.BAD_REQUEST);
        }
        try {
            ClientThreadDispatcher.run(() -> {
                if (!KeyBindings.instance.exists(id)) {
                    throw new IllegalArgumentException("Cannot find script by name " + id + ".");
                }
                KeyBindings.instance.assign(index, id);
            });
        } catch (IllegalArgumentException e) {
            throw new ApiException(e.getMessage(), HttpResponseCodes.BAD_REQUEST, e);
        }
        ConfigStore.instance.requestWrite();
        return "true";
    }
}