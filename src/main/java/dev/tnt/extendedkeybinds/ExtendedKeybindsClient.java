package dev.tnt.extendedkeybinds;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(value = ExtendedKeybindsClient.MODID, dist = Dist.CLIENT)
public class ExtendedKeybindsClient {

    public static final String MODID = "extendedkeybinds";
    public static final Logger LOGGER = LogManager.getLogger("ExtendedKeybinds");

    public ExtendedKeybindsClient(IEventBus modEventBus, ModContainer modContainer) {
    }
}
