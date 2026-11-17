package tnt.tarkovcraft.extendedkeybinds;

import dev.toma.configuration.Configuration;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tnt.tarkovcraft.extendedkeybinds.api.ExtendedKeybindsApi;
import tnt.tarkovcraft.extendedkeybinds.config.ExtendedKeybindsConfig;
import tnt.tarkovcraft.extendedkeybinds.input.TriggerManager;

@Mod(value = ExtendedKeybindsApi.MODID, dist = Dist.CLIENT)
public class ExtendedKeybindsClient {

    public static final Logger LOGGER = LogManager.getLogger("ExtendedKeybinds");
    public static final ExtendedKeybindsConfig CONFIG = Configuration.registerSimpleJsonConfig(ExtendedKeybindsConfig.class);

    public ExtendedKeybindsClient() {
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Pre.class, TriggerManager::onPreTick);
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, TriggerManager::onPostTick);
    }
}
