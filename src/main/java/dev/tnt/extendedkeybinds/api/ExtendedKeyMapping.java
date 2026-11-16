package dev.tnt.extendedkeybinds.api;

import dev.tnt.extendedkeybinds.input.InputMode;
import dev.tnt.extendedkeybinds.input.InternalKeyMapping;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

public interface ExtendedKeyMapping extends InternalKeyMapping {

    default InputMode extendedkeybinds$getInputMode() {
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
}
