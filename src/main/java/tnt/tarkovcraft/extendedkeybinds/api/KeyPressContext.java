package tnt.tarkovcraft.extendedkeybinds.api;

import tnt.tarkovcraft.extendedkeybinds.input.InputMode;
import net.minecraft.client.KeyMapping;

public record KeyPressContext(KeyMapping mapping, InputMode mode, long heldMillis, int heldTicks) {
}
