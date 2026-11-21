package tnt.tarkovcraft.extendedkeybinds.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import tnt.tarkovcraft.extendedkeybinds.input.ScrollInputHandler;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @WrapOperation(
            method = "onScroll",
            at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/client/ClientHooks;onMouseScroll(Lnet/minecraft/client/MouseHandler;DD)Z")
    )
    private boolean extendedkeybinds$dispatchScroll(MouseHandler mouseHelper, double scrollDeltaX, double scrollDeltaY, Operation<Boolean> original, @Local(name = "i") int i, @Local(name = "j") int j) {
        if (original.call(mouseHelper, scrollDeltaX, scrollDeltaY)) {
            return true;
        }
        ScrollInputHandler.dispatch(new Vector2i(j, i));
        return true;
    }

    @WrapOperation(
            method = "onScroll",
            at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/client/ClientHooks;onScreenMouseScrollPre(Lnet/minecraft/client/MouseHandler;Lnet/minecraft/client/gui/screens/Screen;DD)Z")
    )
    private boolean extendedkeybinds$captureScrollKey(MouseHandler mouseHelper, Screen guiScreen, double scrollDeltaX, double scrollDeltaY, Operation<Boolean> original) {
        if (guiScreen instanceof KeyBindsScreen keyBindsScreen && ScrollInputHandler.captureInKeyBindsScreen(keyBindsScreen, scrollDeltaX, scrollDeltaY)) {
            return true;
        }
        return original.call(mouseHelper, guiScreen, scrollDeltaX, scrollDeltaY);
    }
}
