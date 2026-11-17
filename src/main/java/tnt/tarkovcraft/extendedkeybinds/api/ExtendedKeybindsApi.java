package tnt.tarkovcraft.extendedkeybinds.api;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import tnt.tarkovcraft.extendedkeybinds.ExtendedKeybindsClient;
import tnt.tarkovcraft.extendedkeybinds.input.ExtendedKeyMapping;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Essentially single point of interaction of other mods with this library. Provides all necessary methods for configuring
 * your keybinds.
 * When registering multiple actions you can utilize the {@link ExtendedKeybindsApi.Configurator} helper to set multiple
 * properties at once. Otherwise, this API offers methods such as {@link ExtendedKeybindsApi#setInputMode(KeyMapping, InputMode)}
 * to set active input mode or {@link ExtendedKeybindsApi#setDefaultInputMode(KeyMapping, InputMode)} to change default input mode method.
 * You can also utilize trigger callbacks to easily hook up custom logic to your keybind using {@link ExtendedKeybindsApi#setTriggerListener(KeyMapping, Consumer)}.
 * There are also various events you can find in {@link tnt.tarkovcraft.extendedkeybinds.api.event} package
 *
 * @author Toma
 * @since 1.0.0
 */
public final class ExtendedKeybindsApi {

    /**
     * Mod ID in case you need it for something
     */
    public static final String MODID = "extendedkeybinds";
    public static final int BUTTON_SIZE = 20;

    /**
     * Allows you to configure any keymappings
     *
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
     * @param mapping queried keymapping
     * @return input mode which is actually applied to inputs. Differs from {@link #getInputMode(KeyMapping)} when the
     * keymapping is bound to mouse wheel ({@link ScrollKeys}), which always behaves as {@link InputMode#PRESS}
     */
    public static InputMode getEffectiveInputMode(KeyMapping mapping) {
        return convert(mapping).extendedkeybinds$getEffectiveInputMode();
    }

    /**
     * Sets active input mode for keymapping
     *
     * @param mapping   queried keymapping
     * @param inputMode new input mode. In case input mode is not allowed, only warning will be logged
     */
    public static void setInputMode(KeyMapping mapping, InputMode inputMode) {
        convert(mapping).extendedkeybinds$setInputMode(inputMode);
    }

    /**
     * @param mapping queried keymapping
     * @return default input mode configured for given keybind
     */
    public static InputMode getDefaultInputMode(KeyMapping mapping) {
        return convert(mapping).extendedkeybinds$getDefaultInputMode();
    }

    /**
     * Sets default input mode for keymapping. If the keymapping also has configured active mode equal to default mode,
     * its selected input mode is changed too
     *
     * @param mapping   queried keymapping
     * @param inputMode new default input mode
     */
    public static void setDefaultInputMode(KeyMapping mapping, InputMode inputMode) {
        convert(mapping).extendedkeybinds$setDefaultInputMode(inputMode);
    }

    /**
     * @param mapping queried keymapping
     * @return allowed input modes for this keymapping
     */
    public static Set<InputMode> getAllowedInputModes(KeyMapping mapping) {
        return convert(mapping).extendedkeybinds$getAllowedInputModes();
    }

    /**
     * Allows you to configure allowed input modes for given keybind
     *
     * @param mapping queried keymapping
     * @param set     set of allowed input modes, see also {@link InputMode#pressOnly()} and {@link InputMode#noContinuous()}
     */
    public static void setAllowedInputModes(KeyMapping mapping, Collection<InputMode> set) {
        convert(mapping).extendedkeybinds$setAllowedInputModes(set);
    }

    /**
     * Adds new trigger listener for valid key presses. Each keymapping can have multiple listeners at once
     *
     * @param mapping  queried keymapping
     * @param listener the key press listener
     */
    public static void setTriggerListener(KeyMapping mapping, Consumer<KeyPressContext> listener) {
        convert(mapping).extendedkeybinds$addTriggerListener(listener);
    }

    /**
     * Removes trigger listener from keybind
     *
     * @param mapping  queried keymapping
     * @param listener the key press listener to remove
     */
    public static void removeTriggerListener(KeyMapping mapping, Consumer<KeyPressContext> listener) {
        convert(mapping).extendedkeybinds$removeTriggerListener(listener);
    }

    /**
     * @return Configured required hold duration for {@link InputMode#HOLD} keybinds
     */
    public static int getRequiredHoldDuration() {
        return ExtendedKeybindsClient.CONFIG.holdDuration;
    }

    /**
     * @return Configured max interval between key presses in order to be registered for {@link InputMode#DOUBLE_CLICK} keybinds
     */
    public static int getDoubleClickInterval() {
        return ExtendedKeybindsClient.CONFIG.doubleClickInterval;
    }

    /**
     * Creates default widget for cycling input modes in UI
     * @param keyMapping Keymapping for the widget
     * @param mappingName Display name
     * @param x x position in UI
     * @param y y position in UI
     * @param cycleHandler function responsible for changing the selected input mode for keymapping
     * @return new configured button
     */
    public static Button createInputModeWidget(KeyMapping keyMapping, Component mappingName, int x, int y, ModeCycleHandler cycleHandler) {
        InputMode inputMode = getInputMode(keyMapping);
        Component displayName = inputMode.getDisplayName();
        return Button.builder(CommonComponents.EMPTY, _ -> cycleHandler.cycleButtonMode())
                .bounds(x, y, BUTTON_SIZE, BUTTON_SIZE)
                .createNarration(_ -> Component.translatable("extendedkeybinds.narrator.mode", mappingName, displayName))
                .build();
    }

    @ApiStatus.Internal
    private static ExtendedKeyMapping convert(KeyMapping mapping) {
        return (ExtendedKeyMapping) mapping;
    }

    @ApiStatus.Internal
    private ExtendedKeybindsApi() {
    }

    @FunctionalInterface
    public interface ModeCycleHandler {
        void cycleButtonMode();
    }

    /**
     * Configurator helper for specific keybinds. Utilizing builder pattern for chained configuration
     */
    public static final class Configurator {

        private final KeyMapping mapping;

        @ApiStatus.Internal
        private Configurator(KeyMapping mapping) {
            this.mapping = mapping;
        }

        /**
         * Sets default mode for keymapping, same as {@link ExtendedKeybindsApi#setDefaultInputMode(KeyMapping, InputMode)}
         *
         * @param mode default input mode
         */
        public Configurator defaultMode(InputMode mode) {
            convert(this.mapping).extendedkeybinds$setDefaultInputMode(mode);
            return this;
        }

        /**
         * Sets allowed input modes for keymapping, same as {@link ExtendedKeybindsApi#setAllowedInputModes(KeyMapping, Collection)}
         *
         * @param modes allowed input modes
         */
        public Configurator allowedModes(InputMode... modes) {
            convert(this.mapping).extendedkeybinds$setAllowedInputModes(Arrays.asList(modes));
            return this;
        }

        /**
         * Adds trigger listener for keymapping, same as {@link ExtendedKeybindsApi#setTriggerListener(KeyMapping, Consumer)}
         *
         * @param listener trigger listener
         */
        public Configurator onTrigger(Consumer<KeyPressContext> listener) {
            ExtendedKeybindsApi.setTriggerListener(this.mapping, listener);
            return this;
        }

        /**
         * @return the original keymapping, useful when registering and configuring new keymappings at once
         */
        public KeyMapping mapping() {
            return this.mapping;
        }
    }
}
