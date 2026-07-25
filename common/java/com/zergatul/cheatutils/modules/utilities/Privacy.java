package com.zergatul.cheatutils.modules.utilities;

import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.common.ModLoaderBridgeInstance;
import com.zergatul.cheatutils.configs.PrivacyConfig;
import com.zergatul.cheatutils.modules.Module;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.SignTextSlot;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class Privacy implements Module {

    public static final Privacy instance = new Privacy();

    private Privacy() {}

    public boolean shouldDisconnectOnTranslationExploit(PrivacyConfig config) {
        return config.disconnectOnTranslationExploit && !ModLoaderBridgeInstance.get().hasMod(Constants.EXPLOIT_PREVENTER_MOD_ID);
    }

    public boolean shouldDisconnectOnMaliciousResourcePack(PrivacyConfig config) {
        return config.disconnectOnMaliciousResourcePackBehavior && !ModLoaderBridgeInstance.get().hasMod(Constants.EXPLOIT_PREVENTER_MOD_ID);
    }

    public Optional<Component> checkExploitable(SignText text) {
        for (int i = 0; i < SignText.LINES; i++) {
            Optional<Component> result = checkExploitable(text.getMessages(false).get(i));
            if (result.isPresent()) {
                return Optional.of(createDisconnectMessage(result.get()));
            }
        }
        return Optional.empty();
    }

    public void forEachExploitable(
            SignBlockEntity sign,
            Consumer<TranslatableContents> translatableConsumer,
            Consumer<KeybindContents> keybindConsumer
    ) {
        Set<String> checked = new HashSet<>();
        forEachExploitable(sign.getText(SignTextSlot.BACK), checked, translatableConsumer, keybindConsumer);
        forEachExploitable(sign.getText(SignTextSlot.FRONT), checked, translatableConsumer, keybindConsumer);
    }

    public Component createDisconnectMessage(Component reason) {
        return Component.literal("")
                .append(Component.literal("Privacy Module Protection").withStyle(ChatFormatting.RED))
                .append("\n\n")
                .append(reason);
    }

    private Optional<Component> checkExploitable(Component component) {
        Optional<Component> result = checkExploitable(component.getContents());
        if (result.isPresent()) {
            return result;
        }
        for (Component child : component.getSiblings()) {
            Optional<Component> result2 = checkExploitable(child);
            if (result2.isPresent()) {
                return result2;
            }
        }
        return Optional.empty();
    }

    private Optional<Component> checkExploitable(ComponentContents contents) {
        if (contents instanceof TranslatableContents translatable) {
            for (Object arg : translatable.getArgs()) {
                if (arg instanceof Component component) {
                    Optional<Component> result = checkExploitable(component);
                    if (result.isPresent()) {
                        return result;
                    }
                }
            }
            return Optional.of(
                    Component.literal("")
                            .append(Component.literal("Server attempted to extract translatable key [").withStyle(ChatFormatting.WHITE))
                            .append(Component.literal(translatable.getKey()).withStyle(ChatFormatting.GREEN))
                            .append(Component.literal("]").withStyle(ChatFormatting.WHITE)));
        }
        if (contents instanceof KeybindContents keybind) {
            return Optional.of(
                    Component.literal("")
                            .append(Component.literal("Server attempted to extract keybind key [").withStyle(ChatFormatting.WHITE))
                            .append(Component.literal(keybind.getName()).withStyle(ChatFormatting.GREEN))
                            .append(Component.literal("]").withStyle(ChatFormatting.WHITE)));
        }
        return Optional.empty();
    }

    private void forEachExploitable(
            SignText text,
            Set<String> checked,
            Consumer<TranslatableContents> translatableConsumer,
            Consumer<KeybindContents> keybindConsumer
    ) {
        for (int i = 0; i < SignText.LINES; i++) {
            forEachExploitable(text.getMessages(false).get(i), checked, translatableConsumer, keybindConsumer);
        }
    }

    private void forEachExploitable(
            Component component,
            Set<String> checked,
            Consumer<TranslatableContents> translatableConsumer,
            Consumer<KeybindContents> keybindConsumer
    ) {
        forEachExploitable(component.getContents(), checked, translatableConsumer, keybindConsumer);
        for (Component child : component.getSiblings()) {
            forEachExploitable(child, checked, translatableConsumer, keybindConsumer);
        }
    }

    private void forEachExploitable(
            ComponentContents contents,
            Set<String> checked,
            Consumer<TranslatableContents> translatableConsumer,
            Consumer<KeybindContents> keybindConsumer
    ) {
        if (contents instanceof TranslatableContents translatable) {
            for (Object arg : translatable.getArgs()) {
                if (arg instanceof Component component) {
                    forEachExploitable(component, checked, translatableConsumer, keybindConsumer);
                }
            }

            String key = "T:" + translatable.getKey();
            if (!checked.contains(key)) {
                checked.add(key);
                translatableConsumer.accept(translatable);
            }
        }
        if (contents instanceof KeybindContents keybind) {
            String key = "K:" + keybind.getName();
            if (!checked.contains(key)) {
                checked.add(key);
                keybindConsumer.accept(keybind);
            }
        }
    }
}