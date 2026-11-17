package tnt.tarkovcraft.extendedkeybinds.api.event;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.neoforged.bus.api.Event;
import tnt.tarkovcraft.extendedkeybinds.api.ExtendedKeybindsApi;
import tnt.tarkovcraft.extendedkeybinds.api.InputMode;

import java.util.function.Consumer;

/**
 * Allows you to change keybind input modes. Triggered when keybinds are being loaded.
 * Triggered on {@link net.neoforged.neoforge.common.NeoForge#EVENT_BUS}
 */
public final class ConfigureDefaultKeybindsEvent extends Event {

    private final KeyMapping[] keyMappings;
    private final Options options;

    public ConfigureDefaultKeybindsEvent(KeyMapping[] keyMappings, Options options) {
        this.keyMappings = keyMappings;
        this.options = options;
    }

    public Options getOptions() {
        return options;
    }

    public void pressOnly(KeyMapping mapping) {
        ExtendedKeybindsApi.setAllowedInputModes(mapping, InputMode.pressOnly());
    }

    public void continuousOnly(KeyMapping mapping) {
        ExtendedKeybindsApi.setAllowedInputModes(mapping, InputMode.continuousOnly());
    }

    public void forEachInCategory(KeyMapping.Category category, Consumer<KeyMapping> consumer) {
        for (KeyMapping mapping : this.keyMappings) {
            if (mapping.getCategory().equals(category)) {
                consumer.accept(mapping);
            }
        }
    }
}
