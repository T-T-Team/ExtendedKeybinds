package dev.tnt.extendedkeybinds.input;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public enum InputMode implements StringRepresentable {

    // vanilla-like press event, default for all keybinds
    PRESS("press"),
    // same as above but triggered once key is released
    RELEASE("release"),
    // requires you to hold the key for some time before keybinding is triggered
    HOLD("hold"),
    // requires you to double-click the key
    DOUBLE_CLICK("double_click"),
    // continuously triggered on each game tick when pressed
    CONTINUOUS("continuous");

    public static final Codec<InputMode> CODEC = StringRepresentable.fromEnum(InputMode::values);

    private final String serializedName;

    InputMode(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public String getTranslationKey() {
        return "extendedkeybinds.mode." + this.serializedName;
    }

    public Component getDisplayName() {
        return Component.translatable(this.getTranslationKey());
    }

    public Component getShortName() {
        return Component.translatable(this.getTranslationKey() + ".short");
    }

    public Component getDescription(InputSettings timings) {
        return Component.translatable(this.getTranslationKey() + ".description", timings.holdDuration(), timings.doubleClickInterval());
    }
}
