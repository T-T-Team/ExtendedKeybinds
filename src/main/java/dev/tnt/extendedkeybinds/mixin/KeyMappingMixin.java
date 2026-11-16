package dev.tnt.extendedkeybinds.mixin;

import dev.tnt.extendedkeybinds.api.ExtendedKeyMapping;
import dev.tnt.extendedkeybinds.input.InputMode;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.extensions.IKeyMappingExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin implements IKeyMappingExtension, ExtendedKeyMapping {

    @Unique
    private InputMode extendedkeybinds$inputMode = InputMode.PRESS;
    @Unique
    private Set<InputMode> extendedkeybinds$allowedInputModes = EnumSet.allOf(InputMode.class);

    @Override
    public InputMode extendedkeybinds$getInputMode() {
        return this.extendedkeybinds$inputMode;
    }

    @Override
    public void extendedkeybinds$setInputMode(InputMode inputMode) {
        this.extendedkeybinds$inputMode = Objects.requireNonNull(inputMode);
    }

    @Override
    public Set<InputMode> extendedkeybinds$getAllowedInputModes() {
        return this.extendedkeybinds$allowedInputModes;
    }

    @Override
    public void extendedkeybinds$setAllowedInputModes(Collection<InputMode> inputModes) {
        this.extendedkeybinds$allowedInputModes = EnumSet.copyOf(inputModes);
    }
}
