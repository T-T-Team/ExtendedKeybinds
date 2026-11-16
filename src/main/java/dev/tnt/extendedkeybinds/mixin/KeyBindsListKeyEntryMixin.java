package dev.tnt.extendedkeybinds.mixin;

import dev.tnt.extendedkeybinds.api.ExtendedKeyMapping;
import dev.tnt.extendedkeybinds.input.InputMode;
import dev.tnt.extendedkeybinds.input.KeymappingFileStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mixin(KeyBindsList.KeyEntry.class)
public abstract class KeyBindsListKeyEntryMixin {

    @Unique
    private static final int extendedkeybinds$BUTTON_SIZE = 20;

    @Unique
    private static final int extendedkeybinds$BUTTON_GAP = 7;

    @Shadow
    @Final
    private KeyMapping key;
    @Shadow
    @Final
    private Component name;
    @Shadow
    @Final
    private Button changeButton;
    @Shadow
    @Final
    KeyBindsList this$0;

    @Unique
    private @Nullable Button extendedkeybinds$modeButton;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void extendedkeybinds$init(KeyBindsList list, KeyMapping key, Component name, CallbackInfo ci) {
        this.extendedkeybinds$modeButton = Button.builder(Component.empty(), _ -> this.extendedkeybinds$cycleMode())
                .bounds(0, 0, extendedkeybinds$BUTTON_SIZE, extendedkeybinds$BUTTON_SIZE)
                .createNarration(_ -> Component.translatable(
                        "extendedkeybinds.narrator.mode", this.name, this.extendedkeybinds$extended().extendedkeybinds$getInputMode().getDisplayName()))
                .build();
        this.extendedkeybinds$updateModeButton();
    }

    @Inject(method = "extractContent", at = @At("TAIL"))
    private void extendedkeybinds$render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a, CallbackInfo ci) {
        Button button = this.extendedkeybinds$modeButton;
        if (button != null && button.visible) {
            button.setPosition(this.changeButton.getX() - extendedkeybinds$BUTTON_GAP - extendedkeybinds$BUTTON_SIZE, this.changeButton.getY());
            button.extractRenderState(graphics, mouseX, mouseY, a);
        }
    }

    @Inject(method = "children", at = @At("RETURN"), cancellable = true)
    private void extendedkeybinds$children(CallbackInfoReturnable<List<? extends GuiEventListener>> cir) {
        Button button = this.extendedkeybinds$modeButton;
        if (button != null && button.visible) {
            List<GuiEventListener> children = new ArrayList<>(cir.getReturnValue());
            children.addFirst(button);
            cir.setReturnValue(children);
        }
    }

    @Inject(method = "narratables", at = @At("RETURN"), cancellable = true)
    private void extendedkeybinds$narratables(CallbackInfoReturnable<List<? extends NarratableEntry>> cir) {
        Button button = this.extendedkeybinds$modeButton;
        if (button != null && button.visible) {
            List<NarratableEntry> narratables = new ArrayList<>(cir.getReturnValue());
            narratables.addFirst(button);
            cir.setReturnValue(narratables);
        }
    }

    @Inject(method = "refreshEntry", at = @At("TAIL"))
    private void extendedkeybinds$refresh(CallbackInfo ci) {
        this.extendedkeybinds$updateModeButton();
    }

    @Unique
    private void extendedkeybinds$cycleMode() {
        ExtendedKeyMapping extended = this.extendedkeybinds$extended();
        List<InputMode> allowed = List.copyOf(extended.extendedkeybinds$getAllowedInputModes());
        int index = allowed.indexOf(extended.extendedkeybinds$getInputMode());
        extended.extendedkeybinds$setInputMode(allowed.get((index + 1) % allowed.size()));
        this.this$0.refreshEntries();
    }

    @Unique
    private void extendedkeybinds$updateModeButton() {
        Button button = this.extendedkeybinds$modeButton;
        if (button == null) {
            return;
        }
        ExtendedKeyMapping extended = this.extendedkeybinds$extended();
        Set<InputMode> allowed = extended.extendedkeybinds$getAllowedInputModes();
        InputMode mode = extended.extendedkeybinds$getInputMode();
        button.visible = !allowed.equals(Set.of(InputMode.PRESS));
        button.active = allowed.size() > 1;
        button.setMessage(mode.getShortName());
        button.setTooltip(Tooltip.create(Component.translatable("extendedkeybinds.mode.tooltip", mode.getDisplayName().copy().withStyle(ChatFormatting.YELLOW))
                .append("\n")
                .append(mode.getDescription(KeymappingFileStorage.getInputSettings()).copy().withStyle(ChatFormatting.GRAY))));
    }

    @Unique
    private ExtendedKeyMapping extendedkeybinds$extended() {
        return (ExtendedKeyMapping) this.key;
    }
}
