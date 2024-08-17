package com.zergatul.cheatutils.scripting;

@FunctionalInterface
public interface KillAuraFunction {
    boolean shouldAttack(int id);
}