package com.zergatul.cheatutils.extensions;

import com.zergatul.cheatutils.modules.esp.EntityEsp;

public interface EntityRenderStateExtension {
    EntityEsp.EntityRenderParameters getParameters_CU();
    void setParameters_CU(EntityEsp.EntityRenderParameters parameters);
}