package tnt.tarkovcraft.extendedkeybinds.config;

import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;
import tnt.tarkovcraft.extendedkeybinds.api.ExtendedKeybindsApi;

@Config(id = ExtendedKeybindsApi.MODID, filename = "extendedkeybinds/config")
public final class ExtendedKeybindsConfig {

    @Configurable
    @Configurable.Range(min = 200, max = 5000)
    @Configurable.Comment("For how long (in ms) you need to hold keybind in Hold mode in order for it to be triggered")
    public int holdDuration = 500;

    @Configurable
    @Configurable.Range(min = 150, max = 1000)
    @Configurable.Comment("Interval (in ms) within which you must press the key again in order to register as a double click")
    public int doubleClickInterval = 300;
}
