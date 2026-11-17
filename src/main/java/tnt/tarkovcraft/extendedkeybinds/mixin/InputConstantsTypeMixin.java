package tnt.tarkovcraft.extendedkeybinds.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tnt.tarkovcraft.extendedkeybinds.api.ScrollKeys;

@Mixin(InputConstants.Type.class)
public abstract class InputConstantsTypeMixin {

    @Shadow
    private static void addKey(InputConstants.Type type, String name, int value) {
        throw new AssertionError();
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void extendedkeybinds$registerScrollKeys(CallbackInfo ci) {
        addKey(InputConstants.Type.MOUSE, ScrollKeys.WHEEL_UP_NAME, ScrollKeys.WHEEL_UP_VALUE);
        addKey(InputConstants.Type.MOUSE, ScrollKeys.WHEEL_DOWN_NAME, ScrollKeys.WHEEL_DOWN_VALUE);
        addKey(InputConstants.Type.MOUSE, ScrollKeys.WHEEL_LEFT_NAME, ScrollKeys.WHEEL_LEFT_VALUE);
        addKey(InputConstants.Type.MOUSE, ScrollKeys.WHEEL_RIGHT_NAME, ScrollKeys.WHEEL_RIGHT_VALUE);
    }
}
