package dev.tnt.extendedkeybinds.api;

import dev.tnt.extendedkeybinds.input.InputMode;

import java.util.Collection;
import java.util.Set;

public interface ExtendedKeyMapping {

    default InputMode extendedkeybinds$getInputMode() {
        throw new UnsupportedOperationException("Not implemented");
    }

    default void extendedkeybinds$setInputMode(InputMode inputMode) {
        throw new UnsupportedOperationException("Not implemented");
    }

    default Set<InputMode> extendedkeybinds$getAllowedInputModes() {
        throw new UnsupportedOperationException("Not implemented");
    }

    default void extendedkeybinds$setAllowedInputModes(Collection<InputMode> inputModes) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
