package com.zergatul.cheatutils.scripting.types;

import com.zergatul.scripting.Getter;
import com.zergatul.scripting.type.CustomType;
import net.minecraft.world.item.component.ItemAttributeModifiers;

@CustomType(name = "AttributeModifier")
public class AttributeModifier {

    private final ItemAttributeModifiers.Entry entry;

    AttributeModifier(ItemAttributeModifiers.Entry entry) {
        this.entry = entry;
    }

    @Getter(name = "attribute")
    public String getAttribute() {
        return entry.attribute().getRegisteredName();
    }

    @Getter(name = "id")
    public String getId() {
        return entry.modifier().id().toString();
    }

    @Getter(name = "operation")
    public String getOperation() {
        return entry.modifier().operation().name();
    }

    @Getter(name = "slot")
    public String getSlot() {
        return entry.slot().name();
    }

    @Getter(name = "value")
    public double getValue() {
        return entry.modifier().amount();
    }
}