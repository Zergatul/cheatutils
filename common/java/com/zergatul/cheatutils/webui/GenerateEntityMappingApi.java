package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.utils.EntityMappingGenerator;
import org.apache.http.HttpException;

public class GenerateEntityMappingApi extends ApiBase {

    @Override
    public String getRoute() {
        return "gen-entity-mapping";
    }

    @Override
    public String get() throws HttpException {
        return EntityMappingGenerator.generate();
    }
}