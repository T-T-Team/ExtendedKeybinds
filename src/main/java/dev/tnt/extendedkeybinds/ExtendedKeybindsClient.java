package dev.tnt.extendedkeybinds;

import dev.tnt.extendedkeybinds.input.TriggerManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(value = ExtendedKeybindsClient.MODID, dist = Dist.CLIENT)
public class ExtendedKeybindsClient {

    public static final String MODID = "extendedkeybinds";
    public static final Logger LOGGER = LogManager.getLogger("ExtendedKeybinds");

    public ExtendedKeybindsClient() {
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Pre.class, TriggerManager::onPreTick);
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, TriggerManager::onPostTick);
    }
}
