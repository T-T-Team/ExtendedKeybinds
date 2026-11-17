package tnt.tarkovcraft.extendedkeybinds.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyBindsScreen.class)
public interface KeyBindsScreenAccessor {

    @Accessor("keyBindsList")
    KeyBindsList extendedkeybinds$getKeyBindsList();

    @Accessor("lastPressedKey")
    void extendedkeybinds$setLastPressedKey(InputConstants.Key key);

    @Accessor("lastPressedModifier")
    void extendedkeybinds$setLastPressedModifier(InputConstants.Key key);

    @Accessor("isLastKeyHeldDown")
    void extendedkeybinds$setLastKeyHeldDown(boolean held);

    @Accessor("isLastModifierHeldDown")
    void extendedkeybinds$setLastModifierHeldDown(boolean held);
}
