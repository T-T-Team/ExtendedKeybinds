package tnt.tarkovcraft.extendedkeybinds.api;

import net.minecraft.client.KeyMapping;

/**
 * @param mapping    the pressed key bind
 * @param mode       active keybind input mode
 * @param heldMillis for how long the key was held
 * @param heldTicks  for how long the key was held converted to game ticks
 * @author Toma
 * @since 1.0.0
 */
public record KeyPressContext(KeyMapping mapping, InputMode mode, long heldMillis, int heldTicks) {
}
