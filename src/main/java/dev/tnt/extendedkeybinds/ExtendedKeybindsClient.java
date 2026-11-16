package dev.tnt.extendedkeybinds;

import net.neoforged.api.distmarker.Dist;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(value = ExtendedKeybindsClient.MODID, dist = Dist.CLIENT)
public class ExtendedKeybindsClient {

    public static final String MODID = "extendedkeybinds";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ExtendedKeybindsClient(IEventBus modEventBus, ModContainer modContainer) {
    }
}
