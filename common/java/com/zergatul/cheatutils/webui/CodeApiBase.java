package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.scripting.compiler.CompilationResult;
import org.apache.http.HttpException;

public abstract class CodeApiBase<T> extends ApiBase {

    @Override
    public String post(String code) throws HttpException {
        if (code == null || code.isEmpty()) {
            setCode(null);
            ConfigStore.instance.requestWrite();
            setProgram(null);
            return "{ \"ok\": true }";
        }

        CompilationResult result;
        try {
            result = compile(code);
        } catch (Throwable e) {
            throw new HttpException(e.getMessage());
        }
        if (result.getProgram() != null) {
            setCode(code);
            ConfigStore.instance.requestWrite();
            setProgram(result.getProgram());
            return "{ \"ok\": true }";
        } else {
            return gson.toJson(result.getDiagnostics());
        }
    }

    protected abstract CompilationResult compile(String code);
    protected abstract void setCode(String code);
    protected abstract void setProgram(T program);
}