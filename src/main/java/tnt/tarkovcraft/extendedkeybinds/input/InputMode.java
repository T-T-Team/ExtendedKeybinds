package tnt.tarkovcraft.extendedkeybinds.input;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringRepresentable;
import tnt.tarkovcraft.extendedkeybinds.ExtendedKeybindsClient;
import tnt.tarkovcraft.extendedkeybinds.config.ExtendedKeybindsConfig;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

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
    private static final Set<InputMode> PRESS_ONLY = EnumSet.of(PRESS);
    private static final Set<InputMode> NO_CONTINUOUS = EnumSet.range(PRESS, DOUBLE_CLICK);

    private final String serializedName;

    InputMode(String serializedName) {
        this.serializedName = serializedName;
    }

    public static Collection<InputMode> pressOnly() {
        return PRESS_ONLY;
    }

    public static Collection<InputMode> noContinuous() {
        return NO_CONTINUOUS;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public String getTranslationKey() {
        return "extendedkeybinds.mode." + this.serializedName;
    }

    public MutableComponent getDisplayName() {
        return Component.translatable(this.getTranslationKey());
    }

    public MutableComponent getShortName() {
        return Component.translatable(this.getTranslationKey() + ".short");
    }

    public MutableComponent getDescription() {
        ExtendedKeybindsConfig config = ExtendedKeybindsClient.CONFIG;
        return Component.translatable(this.getTranslationKey() + ".description", config.holdDuration, config.doubleClickInterval);
    }
}
