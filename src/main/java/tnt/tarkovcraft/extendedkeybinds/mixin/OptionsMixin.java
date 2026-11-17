package tnt.tarkovcraft.extendedkeybinds.mixin;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tnt.tarkovcraft.extendedkeybinds.ExtendedKeybindsClient;
import tnt.tarkovcraft.extendedkeybinds.input.KeymappingFileStorage;

@Mixin(Options.class)
public abstract class OptionsMixin {

    @Shadow
    public KeyMapping[] keyMappings;

    @Inject(method = "load", at = @At("HEAD"))
    private void extendedkeybinds$beforeLoad(CallbackInfo ci) {
        Options options = (Options) (Object) this;
        ExtendedKeybindsClient.configureDefaults(options, this.keyMappings);
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void extendedkeybinds$afterLoad(CallbackInfo ci) {
        KeymappingFileStorage.load(this.keyMappings);
    }

    @Inject(method = "save", at = @At("RETURN"))
    private void extendedkeybinds$afterSave(CallbackInfo ci) {
        KeymappingFileStorage.save(this.keyMappings);
    }
}
