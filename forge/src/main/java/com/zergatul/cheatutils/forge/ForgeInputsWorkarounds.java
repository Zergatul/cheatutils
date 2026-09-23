package com.zergatul.cheatutils.forge;

import com.mojang.blaze3d.platform.InputConstants;
import com.zergatul.cheatutils.common.LoaderInputsWorkarounds;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.Optional;

public final class ForgeInputsWorkarounds implements LoaderInputsWorkarounds {

    public static final ForgeInputsWorkarounds INSTANCE = new ForgeInputsWorkarounds();

    private ForgeInputsWorkarounds() {}

    public String getKeyText(InputConstants.Key key) {
        Component displayName = key.getDisplayName();

        if (displayName instanceof MutableComponent mutable) {
            if (mutable.getContents() instanceof TranslatableContents translatable) {
                String i18Key = translatable.getKey();
                Language currentLanguage = Language.getInstance();
                return currentLanguage.getOrDefault(i18Key);
            }
        }

        StringBuilder sb = new StringBuilder();
        displayName.visit(cc -> {
            sb.append(cc);
            return Optional.empty();
        });
        return sb.toString();
    }
}