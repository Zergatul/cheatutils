package com.zergatul.cheatutils.scripting;

import com.zergatul.cheatutils.scripting.modules.*;

@SuppressWarnings("unused")
public class Root {

    public static final EspApi esp = new EspApi();

    // automation

    // esp
    public static final BlocksApi blocks = new BlocksApi();
    public static final FreeCamApi freeCam = new FreeCamApi();

    // others
    public static final UIApi ui = new UIApi();
    public static final DelayApi delay = new DelayApi();
}