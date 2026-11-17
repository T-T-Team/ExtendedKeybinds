package tnt.tarkovcraft.extendedkeybinds.api;

import tnt.tarkovcraft.extendedkeybinds.ExtendedKeybindsClient;
import tnt.tarkovcraft.extendedkeybinds.input.ExtendedKeyMapping;
import tnt.tarkovcraft.extendedkeybinds.input.InputMode;
import net.minecraft.client.KeyMapping;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

public final class ExtendedKeybindsApi {

    /**
     * Mod ID in case you need it for something
     */
    public static final String MODID = "extendedkeybinds";

    /**
     * Allows you to configure any keymappings
     * @param mapping mapping to configure
     * @return Configurator used to adjust keybind properties
     * @see Configurator for more details
     */
    public static Configurator configure(KeyMapping mapping) {
        return new Configurator(mapping);
    }

    /**
     * @param mapping queried keymapping
     * @return active input mode
     */
    public static InputMode getInputMode(KeyMapping mapping) {
        return convert(mapping).extendedkeybinds$getInputMode();
    }

    /**
     * Sets active input mode for keymapping
     * @param mapping queried keymapping
     * @param inputMode new input mode. In case input mode is not allowed, only warning will be logged
     */
    public static void setInputMode(KeyMapping mapping, InputMode inputMode) {
        convert(mapping).extendedkeybinds$setInputMode(inputMode);
    }

    public static InputMode getDefaultInputMode(KeyMapping mapping) {
        return convert(mapping).extendedkeybinds$getDefaultInputMode();
    }

    public static void setDefaultInputMode(KeyMapping mapping, InputMode inputMode) {
        convert(mapping).extendedkeybinds$setDefaultInputMode(inputMode);
    }

    public static Set<InputMode> getAllowedInputModes(KeyMapping mapping) {
        return convert(mapping).extendedkeybinds$getAllowedInputModes();
    }

    public static void setAllowedInputModes(KeyMapping mapping, Collection<InputMode> set) {
        convert(mapping).extendedkeybinds$setAllowedInputModes(set);
    }

    public static void setTriggerListener(KeyMapping mapping, Consumer<KeyPressContext> listener) {
        convert(mapping).extendedkeybinds$addTriggerListener(listener);
    }

    public static void removeTriggerListener(KeyMapping mapping, Consumer<KeyPressContext> listener) {
        convert(mapping).extendedkeybinds$removeTriggerListener(listener);
    }

    public static int getRequiredHoldDuration() {
        return ExtendedKeybindsClient.CONFIG.holdDuration;
    }

    public static int getDoubleClickInterval() {
        return ExtendedKeybindsClient.CONFIG.doubleClickInterval;
    }

    private static ExtendedKeyMapping convert(KeyMapping mapping) {
        return (ExtendedKeyMapping) mapping;
    }

    private ExtendedKeybindsApi() {
    }

    public static final class Configurator {

        private final KeyMapping mapping;

        private Configurator(KeyMapping mapping) {
            this.mapping = mapping;
        }

        public Configurator defaultMode(InputMode mode) {
            convert(this.mapping).extendedkeybinds$setDefaultInputMode(mode);
            return this;
        }

        public Configurator allowedModes(InputMode... modes) {
            convert(this.mapping).extendedkeybinds$setAllowedInputModes(Arrays.asList(modes));
            return this;
        }

        public Configurator onTrigger(Consumer<KeyPressContext> listener) {
            ExtendedKeybindsApi.setTriggerListener(this.mapping, listener);
            return this;
        }

        public KeyMapping mapping() {
            return this.mapping;
        }
    }
}
