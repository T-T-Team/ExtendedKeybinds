package tnt.tarkovcraft.extendedkeybinds.input;

import net.minecraft.network.chat.Component;
import tnt.tarkovcraft.extendedkeybinds.api.InputHandlerOverride;
import tnt.tarkovcraft.extendedkeybinds.api.InputMode;
import tnt.tarkovcraft.extendedkeybinds.api.KeyPressContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

public interface ExtendedKeyMapping extends TickableKeyMapping {

    default InputMode extendedkeybinds$getInputMode() {
        throw new UnsupportedOperationException("Not implemented");
    }

    default InputMode extendedkeybinds$getEffectiveInputMode() {
        throw new UnsupportedOperationException("Not implemented");
    }

    default void extendedkeybinds$setInputMode(InputMode inputMode) {
        throw new UnsupportedOperationException("Not implemented");
    }

    default InputMode extendedkeybinds$getDefaultInputMode() {
        throw new UnsupportedOperationException("Not implemented");
    }

    default void extendedkeybinds$setDefaultInputMode(InputMode inputMode) {
        throw new UnsupportedOperationException("Not implemented");
    }

    default Set<InputMode> extendedkeybinds$getAllowedInputModes() {
        throw new UnsupportedOperationException("Not implemented");
    }

    default void extendedkeybinds$setAllowedInputModes(Collection<InputMode> inputModes) {
        throw new UnsupportedOperationException("Not implemented");
    }

    default boolean extendedkeybinds$isInputModeAllowed(InputMode inputMode) {
        throw new UnsupportedOperationException("Not implemented");
    }

    default void extendedkeybinds$resetInputState() {
        throw new UnsupportedOperationException("Not implemented");
    }

    default void extendedkeybinds$addTriggerListener(Consumer<KeyPressContext> listener) {
        throw new UnsupportedOperationException("Not implemented");
    }

    default void extendedkeybinds$removeTriggerListener(Consumer<KeyPressContext> listener) {
        throw new UnsupportedOperationException("Not implemented");
    }

    default @Nullable InputHandlerOverride extendedkeybinds$getInputHandlerOverride() {
        throw new UnsupportedOperationException("Not implemented");
    }

    default void extendedkeybinds$setInputHandlerOverride(@Nullable InputHandlerOverride override) {
        throw new UnsupportedOperationException("Not implemented");
    }

    default boolean extendedkeybinds$isInputModeLocked() {
        throw new UnsupportedOperationException("Not implemented");
    }

    default @Nullable Component extendedkeybinds$getInputModeLockReason() {
        throw new UnsupportedOperationException("Not implemented");
    }

    default boolean extendedkeybinds$resolvesGestures() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
