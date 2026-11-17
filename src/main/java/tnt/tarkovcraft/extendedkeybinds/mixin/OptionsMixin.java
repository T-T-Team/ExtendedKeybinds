package tnt.tarkovcraft.extendedkeybinds.mixin;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tnt.tarkovcraft.extendedkeybinds.input.ExtendedKeyMapping;
import tnt.tarkovcraft.extendedkeybinds.input.InputMode;
import tnt.tarkovcraft.extendedkeybinds.input.KeymappingFileStorage;

@Mixin(Options.class)
public abstract class OptionsMixin {

    @Shadow
    public KeyMapping[] keyMappings;
    @Shadow
    @Final
    public KeyMapping keyFullscreen;
    @Shadow
    @Final
    public KeyMapping keyScreenshot;
    @Shadow
    @Final
    public KeyMapping keyFriends;

    @Inject(method = "load", at = @At("HEAD"))
    private void extendedkeybinds$beforeLoad(CallbackInfo ci) {
        extendedkeybinds$lockToPress(this.keyFullscreen);
        extendedkeybinds$lockToPress(this.keyScreenshot);
        extendedkeybinds$lockToPress(this.keyFriends);
        for (KeyMapping mapping : this.keyMappings) {
            if (mapping.getCategory() == KeyMapping.Category.DEBUG) {
                extendedkeybinds$lockToPress(mapping);
            }
        }
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void extendedkeybinds$afterLoad(CallbackInfo ci) {
        KeymappingFileStorage.load(this.keyMappings);
    }

    @Inject(method = "save", at = @At("RETURN"))
    private void extendedkeybinds$afterSave(CallbackInfo ci) {
        KeymappingFileStorage.save(this.keyMappings);
    }

    @Unique
    private static void extendedkeybinds$lockToPress(KeyMapping mapping) {
        ((ExtendedKeyMapping) mapping).extendedkeybinds$setAllowedInputModes(InputMode.pressOnly());
    }
}
