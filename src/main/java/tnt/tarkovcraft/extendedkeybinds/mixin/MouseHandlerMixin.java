package tnt.tarkovcraft.extendedkeybinds.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import org.joml.Vector2ic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import tnt.tarkovcraft.extendedkeybinds.input.ScrollInputHandler;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @WrapOperation(
            method = "onScroll",
            at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/client/ClientHooks;onMouseScroll(Lnet/minecraft/client/MouseHandler;DDLorg/joml/Vector2ic;)Z")
    )
    private boolean extendedkeybinds$dispatchScroll(MouseHandler handler, double scrollX, double scrollY, Vector2ic wheel, Operation<Boolean> original) {
        if (original.call(handler, scrollX, scrollY, wheel)) {
            return true;
        }
        ScrollInputHandler.dispatch(wheel);
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
