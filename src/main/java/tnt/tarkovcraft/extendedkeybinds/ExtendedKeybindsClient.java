package tnt.tarkovcraft.extendedkeybinds;

import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.format.ConfigFormats;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tnt.tarkovcraft.extendedkeybinds.api.ExtendedKeybindsApi;
import tnt.tarkovcraft.extendedkeybinds.api.InputMode;
import tnt.tarkovcraft.extendedkeybinds.api.event.ConfigureDefaultKeybindsEvent;
import tnt.tarkovcraft.extendedkeybinds.config.ExtendedKeybindsConfig;
import tnt.tarkovcraft.extendedkeybinds.input.ScrollInputHandler;
import tnt.tarkovcraft.extendedkeybinds.input.TriggerManager;

@Mod(value = ExtendedKeybindsApi.MODID, dist = Dist.CLIENT)
public class ExtendedKeybindsClient {

    public static final Logger LOGGER = LogManager.getLogger("ExtendedKeybinds");
    public static final ExtendedKeybindsConfig CONFIG = Configuration.registerConfig(ExtendedKeybindsConfig.class, ConfigFormats.JSON).getConfigInstance();

    public ExtendedKeybindsClient(IEventBus modEventBus) {
        modEventBus.addListener(this::registerKeyMappings);

        NeoForge.EVENT_BUS.addListener(this::preTick);
        NeoForge.EVENT_BUS.addListener(this::postTick);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::onMouseScrolled);
    }

    public static void configureDefaults(Options options, KeyMapping[] keyMappings) {
        ExtendedKeybindsApi.setAllowedInputModes(options.keyUp, InputMode.continuousOnly());
        ExtendedKeybindsApi.setAllowedInputModes(options.keyDown, InputMode.continuousOnly());
        ExtendedKeybindsApi.setAllowedInputModes(options.keyLeft, InputMode.continuousOnly());
        ExtendedKeybindsApi.setAllowedInputModes(options.keyRight, InputMode.continuousOnly());
        ExtendedKeybindsApi.setAllowedInputModes(options.keyJump, InputMode.continuousOnly());

        ExtendedKeybindsApi.setDefaultInputMode(options.keyPlayerList, InputMode.CONTINUOUS);
        ExtendedKeybindsApi.setDefaultInputMode(options.keySpectatorOutlines, InputMode.CONTINUOUS);
        ExtendedKeybindsApi.setDefaultInputMode(options.keySaveHotbarActivator, InputMode.CONTINUOUS);
        ExtendedKeybindsApi.setDefaultInputMode(options.keyLoadHotbarActivator, InputMode.CONTINUOUS);
        // event
        NeoForge.EVENT_BUS.post(new ConfigureDefaultKeybindsEvent(keyMappings, options));
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ScrollInputHandler.HOTBAR_NEXT);
        event.register(ScrollInputHandler.HOTBAR_PREVIOUS);
    }

    private void preTick(ClientTickEvent.Pre event) {
        TriggerManager.onPreTick(event);
    }

    private void postTick(ClientTickEvent.Post event) {
        TriggerManager.onPostTick(event);
    }

    private void onMouseScrolled(InputEvent.MouseScrollingEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player != null && player.isSpectator()) {
            SpectatorGui spectatorGui = minecraft.gui.getSpectatorGui();
            int wheel = (int) (event.getScrollDeltaY() == 0 ? -event.getScrollDeltaX() : event.getScrollDeltaY()); // TODO verify
            if (spectatorGui.isMenuActive()) {
                spectatorGui.onMouseScrolled(-wheel);
            } else {
                float speed = Mth.clamp(player.getAbilities().getFlyingSpeed() + wheel * 0.005F, 0.0F, 0.2F);
                player.getAbilities().setFlyingSpeed(speed);
            }
            event.setCanceled(true);
        }
    }
}
