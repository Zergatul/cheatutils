package com.zergatul.cheatutils.scripting;

import com.zergatul.cheatutils.scripting.modules.FreeCamApi;
import com.zergatul.cheatutils.scripting.modules.UIApi;

public class Root {
    public static final FreeCamApi freeCam = new FreeCamApi();
    public static final UIApi ui = new UIApi();
}
