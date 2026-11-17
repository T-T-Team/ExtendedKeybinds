package tnt.tarkovcraft.extendedkeybinds.api;

import com.mojang.blaze3d.platform.InputConstants;

/**
 * Mouse wheel inputs exposed as regular {@link InputConstants.Type#MOUSE} keys, so they can be assigned to any
 * {@link net.minecraft.client.KeyMapping}. Each wheel notch is dispatched as an instant press and release.
 * Key mappings bound to scroll keys always behave as {@link InputMode#PRESS}, regardless of their configured input mode.
 * <p>
 * To use scroll key as default key of your keybind, use one of the value constants, e.g.
 * {@code new KeyMapping("key.example", InputConstants.Type.MOUSE, ScrollKeys.WHEEL_UP_VALUE, category)}
 *
 * @author Toma
 * @since 1.0.0
 */
public final class ScrollKeys {

    public static final int WHEEL_UP_VALUE = 0x5C00;
    public static final int WHEEL_DOWN_VALUE = 0x5C01;
    public static final int WHEEL_LEFT_VALUE = 0x5C02;
    public static final int WHEEL_RIGHT_VALUE = 0x5C03;

    public static final String WHEEL_UP_NAME = "key.mouse.wheel.up";
    public static final String WHEEL_DOWN_NAME = "key.mouse.wheel.down";
    public static final String WHEEL_LEFT_NAME = "key.mouse.wheel.left";
    public static final String WHEEL_RIGHT_NAME = "key.mouse.wheel.right";

    public static InputConstants.Key wheelUp() {
        return InputConstants.Type.MOUSE.getOrCreate(WHEEL_UP_VALUE);
    }

    public static InputConstants.Key wheelDown() {
        return InputConstants.Type.MOUSE.getOrCreate(WHEEL_DOWN_VALUE);
    }

    public static InputConstants.Key wheelLeft() {
        return InputConstants.Type.MOUSE.getOrCreate(WHEEL_LEFT_VALUE);
    }

    public static InputConstants.Key wheelRight() {
        return InputConstants.Type.MOUSE.getOrCreate(WHEEL_RIGHT_VALUE);
    }

    /**
     * @param key queried key
     * @return whether the key represents mouse wheel scrolling
     */
    public static boolean isScrollKey(InputConstants.Key key) {
        return key.getType() == InputConstants.Type.MOUSE && isScrollValue(key.getValue());
    }

    public static boolean isScrollValue(int value) {
        return value >= WHEEL_UP_VALUE && value <= WHEEL_RIGHT_VALUE;
    }

    private ScrollKeys() {
    }
}
