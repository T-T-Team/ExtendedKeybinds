package dev.tnt.extendedkeybinds.api;

import dev.tnt.extendedkeybinds.input.InputMode;
import dev.tnt.extendedkeybinds.input.InputSettings;
import dev.tnt.extendedkeybinds.input.KeymappingFileStorage;
import net.minecraft.client.KeyMapping;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;

public final class ExtendedKeybinds {

    private ExtendedKeybinds() {
    }

    public static ExtendedKeyMapping extend(KeyMapping mapping) {
        return (ExtendedKeyMapping) mapping;
    }

    public static Builder configure(KeyMapping mapping) {
        return new Builder(mapping);
    }

    public static InputMode getInputMode(KeyMapping mapping) {
        return extend(mapping).extendedkeybinds$getInputMode();
    }

    public static InputMode getDefaultInputMode(KeyMapping mapping) {
        return extend(mapping).extendedkeybinds$getDefaultInputMode();
    }

    public static Set<InputMode> getAllowedInputModes(KeyMapping mapping) {
        return extend(mapping).extendedkeybinds$getAllowedInputModes();
    }

    public static void onTrigger(KeyMapping mapping, Consumer<KeyPressContext> listener) {
        extend(mapping).extendedkeybinds$addTriggerListener(listener);
    }

    public static void removeTriggerListener(KeyMapping mapping, Consumer<KeyPressContext> listener) {
        extend(mapping).extendedkeybinds$removeTriggerListener(listener);
    }

    public static InputSettings getInputSettings() {
        return KeymappingFileStorage.getInputSettings();
    }

    public static final class Builder {

        private final KeyMapping mapping;

        private Builder(KeyMapping mapping) {
            this.mapping = mapping;
        }

        public Builder defaultMode(InputMode mode) {
            extend(this.mapping).extendedkeybinds$setDefaultInputMode(mode);
            return this;
        }

        public Builder allowedModes(InputMode... modes) {
            extend(this.mapping).extendedkeybinds$setAllowedInputModes(Arrays.asList(modes));
            return this;
        }

        public Builder onTrigger(Consumer<KeyPressContext> listener) {
            ExtendedKeybinds.onTrigger(this.mapping, listener);
            return this;
        }

        public KeyMapping mapping() {
            return this.mapping;
        }
    }
}
