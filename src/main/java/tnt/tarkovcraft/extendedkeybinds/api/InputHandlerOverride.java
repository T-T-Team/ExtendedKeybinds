package tnt.tarkovcraft.extendedkeybinds.api;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import tnt.tarkovcraft.extendedkeybinds.input.VanillaPassthroughHandler;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Predicate;

public record InputHandlerOverride(
        Predicate<InputConstants.Key> condition,
        KeyInputHandler.Factory factory,
        @Nullable InputMode lockedMode,
        @Nullable Component lockedTooltip
) {

    public InputHandlerOverride {
        Objects.requireNonNull(condition);
        Objects.requireNonNull(factory);
    }

    public static InputHandlerOverride vanillaMouse() {
        return new InputHandlerOverride(
                InputHandlerOverride::isMouseButton,
                VanillaPassthroughHandler::new,
                InputMode.PRESS,
                Component.translatable("extendedkeybinds.mode.mouse_locked")
        );
    }

    public static boolean isMouseButton(InputConstants.Key key) {
        return key.getType() == InputConstants.Type.MOUSE && !ScrollKeys.isScrollKey(key);
    }

    public boolean appliesTo(InputConstants.Key key) {
        return this.condition.test(key);
    }
}
