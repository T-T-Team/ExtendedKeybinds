package dev.tnt.extendedkeybinds.api;

import dev.tnt.extendedkeybinds.input.InputMode;
import net.minecraft.client.KeyMapping;

public record KeyPressContext(KeyMapping mapping, InputMode mode, long heldMillis, int heldTicks) {
}
