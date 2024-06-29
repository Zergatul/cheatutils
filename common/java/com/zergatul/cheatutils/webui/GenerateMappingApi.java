package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.utils.mappings.ContainerMenuMappingGenerator;
import com.zergatul.cheatutils.utils.mappings.EntityMappingGenerator;
import org.apache.http.HttpException;

public class GenerateMappingApi extends ApiBase {

    @Override
    public String getRoute() {
        return "gen-mapping";
    }

    @Override
    public String get(String id) throws HttpException {
        if (id.equals("entity")) {
            return EntityMappingGenerator.generate();
        }
        if (id.equals("menu")) {
            return ContainerMenuMappingGenerator.generate();
        }
        return "Invalid ID";
    }
}