package tnt.tarkovcraft.extendedkeybinds.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ScrollWheelHandler;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.joml.Vector2ic;
import tnt.tarkovcraft.extendedkeybinds.api.ExtendedKeybindsApi;
import tnt.tarkovcraft.extendedkeybinds.api.ScrollKeys;
import tnt.tarkovcraft.extendedkeybinds.mixin.KeyBindsScreenAccessor;

import java.util.List;

public final class ScrollInputHandler {

    public static final KeyMapping HOTBAR_PREVIOUS = ExtendedKeybindsApi.configure(
            new KeyMapping("key.hotbar.previous",InputConstants.Type.MOUSE, ScrollKeys.WHEEL_UP_VALUE, KeyMapping.Category.GAMEPLAY)
    ).onTrigger(_ -> scroll(1)).mapping();

    public static final KeyMapping HOTBAR_NEXT = ExtendedKeybindsApi.configure(
            new KeyMapping("key.hotbar.next", InputConstants.Type.MOUSE, ScrollKeys.WHEEL_DOWN_VALUE, KeyMapping.Category.GAMEPLAY)
    ).onTrigger(_ -> scroll(-1)).mapping();

    private ScrollInputHandler() {
    }

    public static void dispatch(Vector2ic wheel) {
        int y = wheel.y();
        int x = wheel.x();
        if (y != 0) {
            pulse(y > 0 ? ScrollKeys.wheelUp() : ScrollKeys.wheelDown(), Math.abs(y));
        }
        if (x != 0) {
            InputConstants.Key horizontal = x > 0 ? ScrollKeys.wheelRight() : ScrollKeys.wheelLeft();
            if (isBound(horizontal)) {
                pulse(horizontal, Math.abs(x));
            } else if (y == 0) {
                // vanilla treats horizontal scroll as vertical when there is no vertical movement, right = down
                pulse(x > 0 ? ScrollKeys.wheelDown() : ScrollKeys.wheelUp(), Math.abs(x));
            }
        }
    }

    public static boolean captureInKeyBindsScreen(KeyBindsScreen screen, double scrollX, double scrollY) {
        KeyMapping selected = screen.selectedKey;
        if (selected == null || (scrollX == 0.0 && scrollY == 0.0)) {
            return false;
        }
        InputConstants.Key key;
        if (scrollY != 0.0) {
            key = scrollY > 0.0 ? ScrollKeys.wheelUp() : ScrollKeys.wheelDown();
        } else {
            key = scrollX > 0.0 ? ScrollKeys.wheelRight() : ScrollKeys.wheelLeft();
        }
        List<KeyModifier> modifiers = KeyModifier.getActiveModifiers();
        KeyModifier modifier = modifiers.isEmpty() ? KeyModifier.NONE : modifiers.getFirst();
        selected.setKeyModifierAndCode(modifier, key);
        KeyBindsScreenAccessor accessor = (KeyBindsScreenAccessor) screen;
        // modifier key press was recorded by the screen, clear it so its release does not rebind anything
        accessor.extendedkeybinds$setLastPressedKey(InputConstants.UNKNOWN);
        accessor.extendedkeybinds$setLastPressedModifier(InputConstants.UNKNOWN);
        accessor.extendedkeybinds$setLastKeyHeldDown(false);
        accessor.extendedkeybinds$setLastModifierHeldDown(false);
        screen.selectedKey = null;
        screen.lastKeySelection = Util.getMillis();
        accessor.extendedkeybinds$getKeyBindsList().resetMappingAndUpdateButtons();
        return true;
    }

    private static void scroll(int wheel) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
            return;
        Inventory inventory = player.getInventory();
        inventory.setSelectedSlot(ScrollWheelHandler.getNextScrollWheelSelection(wheel, inventory.getSelectedSlot(), Inventory.getSelectionSize()));
    }

    private static void pulse(InputConstants.Key key, int count) {
        for (int i = 0; i < count; i++) {
            KeyMapping.set(key, true);
            KeyMapping.click(key);
            KeyMapping.set(key, false);
        }
    }

    private static boolean isBound(InputConstants.Key key) {
        for (KeyMapping mapping : Minecraft.getInstance().options.keyMappings) {
            if (mapping.getKey().equals(key) && mapping.isConflictContextAndModifierActive()) {
                return true;
            }
        }
        return false;
    }
}
