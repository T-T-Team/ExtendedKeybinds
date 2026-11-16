package dev.tnt.extendedkeybinds.input;

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

    private final String serializedName;

    InputMode(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
