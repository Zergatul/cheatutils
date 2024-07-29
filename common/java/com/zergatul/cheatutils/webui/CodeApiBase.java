package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.scripting.compiler.CompilationResult;
import org.apache.http.HttpException;

public abstract class CodeApiBase extends ApiBase {

    @Override
    public String post(String code) throws HttpException {
        if (code == null || code.isEmpty()) {
            setCode(null);
            ConfigStore.instance.requestWrite();
            setProgram(() -> {});
            return "{ \"ok\": true }";
        }

        CompilationResult<Runnable> result;
        try {
            result = compile(code);
        } catch (Throwable e) {
            throw new HttpException(e.getMessage());
        }
        if (result.program() != null) {
            setCode(code);
            ConfigStore.instance.requestWrite();
            setProgram(result.program());
            return "{ \"ok\": true }";
        } else {
            return gson.toJson(result.diagnostics());
        }
    }

    protected abstract CompilationResult<Runnable> compile(String code);
    protected abstract void setCode(String code);
    protected abstract void setProgram(Runnable program);
}