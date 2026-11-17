package tnt.tarkovcraft.extendedkeybinds;

import dev.toma.configuration.Configuration;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tnt.tarkovcraft.extendedkeybinds.api.ExtendedKeybindsApi;
import tnt.tarkovcraft.extendedkeybinds.api.InputMode;
import tnt.tarkovcraft.extendedkeybinds.api.event.ConfigureDefaultKeybindsEvent;
import tnt.tarkovcraft.extendedkeybinds.config.ExtendedKeybindsConfig;
import tnt.tarkovcraft.extendedkeybinds.input.TriggerManager;

import java.util.Collection;

@Mod(value = ExtendedKeybindsApi.MODID, dist = Dist.CLIENT)
public class ExtendedKeybindsClient {

    public static final Logger LOGGER = LogManager.getLogger("ExtendedKeybinds");
    public static final ExtendedKeybindsConfig CONFIG = Configuration.registerSimpleJsonConfig(ExtendedKeybindsConfig.class);

    public ExtendedKeybindsClient() {
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Pre.class, TriggerManager::onPreTick);
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, TriggerManager::onPostTick);
    }

    public static void configureDefaults(Options options, KeyMapping[] keyMappings) {
        // toggle keys
        setCategoryInputList(keyMappings, KeyMapping.Category.MOVEMENT, InputMode.pressOnly());
        setCategoryInputList(keyMappings, KeyMapping.Category.DEBUG, InputMode.pressOnly());
        // event
        NeoForge.EVENT_BUS.post(new ConfigureDefaultKeybindsEvent(keyMappings, options));
    }

    private static void setCategoryInputList(KeyMapping[] mappings, KeyMapping.Category category, Collection<InputMode> inputs) {
        for (var mapping : mappings) {
            if (mapping.getCategory().equals(category)) {
                ExtendedKeybindsApi.setAllowedInputModes(mapping, inputs);
            }
        }
    }
}
