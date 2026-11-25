package tnt.tarkovcraft.extendedkeybinds.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tnt.tarkovcraft.extendedkeybinds.api.ExtendedKeybindsApi;
import tnt.tarkovcraft.extendedkeybinds.api.InputMode;
import tnt.tarkovcraft.extendedkeybinds.input.ExtendedKeyMapping;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mixin(KeyBindsList.KeyEntry.class)
public abstract class KeyBindsListKeyEntryMixin {

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
        this.extendedkeybinds$modeButton = ExtendedKeybindsApi.createInputModeWidget(this.key, this.name, 0, 0, this::extendedkeybinds$cycleMode);
        this.extendedkeybinds$updateModeButton();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void extendedkeybinds$render(GuiGraphics graphics, int i, int x, int y, int width, int height, int mouseX, int mouseY, boolean hovered, float a, CallbackInfo ci) {
        Button button = this.extendedkeybinds$modeButton;
        if (button != null && button.visible) {
            // gap between conflict and buttons is 7px
            button.setPosition(this.changeButton.getX() - 7 - ExtendedKeybindsApi.BUTTON_SIZE, this.changeButton.getY());
            button.render(graphics, mouseX, mouseY, a);
        }
    }

    @Inject(method = "children", at = @At("RETURN"), cancellable = true)
    private void extendedkeybinds$children(CallbackInfoReturnable<List<? extends GuiEventListener>> cir) {
        Button button = this.extendedkeybinds$modeButton;
        if (button != null && button.active) {
            List<GuiEventListener> children = new ArrayList<>(cir.getReturnValue());
            children.addFirst(button);
            cir.setReturnValue(children);
        }
    }

    @Inject(method = "narratables", at = @At("RETURN"), cancellable = true)
    private void extendedkeybinds$narratables(CallbackInfoReturnable<List<? extends NarratableEntry>> cir) {
        Button button = this.extendedkeybinds$modeButton;
        if (button != null && button.active) {
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
        ExtendedKeyMapping extended = (ExtendedKeyMapping) this.key;
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
        ExtendedKeyMapping extended = (ExtendedKeyMapping) this.key;
        Set<InputMode> allowed = extended.extendedkeybinds$getAllowedInputModes();
        InputMode mode = extended.extendedkeybinds$getEffectiveInputMode();
        Component lockReason = extended.extendedkeybinds$isInputModeLocked() ? extended.extendedkeybinds$getInputModeLockReason() : null;
        button.active = allowed.size() > 1 && lockReason == null;
        button.setMessage(mode.getShortName());
        Component description = lockReason != null
                ? lockReason.copy().withStyle(ChatFormatting.GRAY)
                : mode.getDescription().withStyle(ChatFormatting.GRAY);
        button.setTooltip(Tooltip.create(Component.translatable("extendedkeybinds.mode.tooltip", mode.getDisplayName().withStyle(ChatFormatting.YELLOW))
                .append("\n")
                .append(description)));
    }
}
