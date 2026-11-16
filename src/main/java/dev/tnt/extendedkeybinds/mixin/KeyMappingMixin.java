package dev.tnt.extendedkeybinds.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import dev.tnt.extendedkeybinds.ExtendedKeybindsClient;
import dev.tnt.extendedkeybinds.api.ExtendedKeyMapping;
import dev.tnt.extendedkeybinds.api.KeyPressContext;
import dev.tnt.extendedkeybinds.input.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.ToggleKeyMapping;
import net.minecraft.util.Util;
import net.neoforged.neoforge.client.extensions.IKeyMappingExtension;
import org.jspecify.annotations.Nullable;
import org.lwjgl.sdl.SDLMouse;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin implements IKeyMappingExtension, ExtendedKeyMapping {

    @Shadow
    boolean isDown;
    @Shadow
    private int clickCount;

    @Unique
    private InputMode extendedkeybinds$inputMode = InputMode.PRESS;
    @Unique
    private InputMode extendedkeybinds$defaultInputMode = InputMode.PRESS;
    @Unique
    private @Nullable Set<InputMode> extendedkeybinds$allowedInputModes;
    @Unique
    private @Nullable TriggerStateMachine extendedkeybinds$state;
    @Unique
    private @Nullable List<Consumer<KeyPressContext>> extendedkeybinds$listeners;

    @Shadow
    public abstract String getName();

    @Shadow
    public abstract InputConstants.Key getDefaultKey();

    @Override
    public InputMode extendedkeybinds$getInputMode() {
        return this.extendedkeybinds$inputMode;
    }

    @Override
    public void extendedkeybinds$setInputMode(InputMode inputMode) {
        Objects.requireNonNull(inputMode);
        if (!this.extendedkeybinds$isInputModeAllowed(inputMode)) {
            ExtendedKeybindsClient.LOGGER.warn("Input mode {} is not allowed for key mapping {}, ignoring", inputMode, this.getName());
            return;
        }
        if (this.extendedkeybinds$inputMode == inputMode) {
            return;
        }
        this.extendedkeybinds$inputMode = inputMode;
        this.extendedkeybinds$ensureState();
        this.extendedkeybinds$resetInputState();
    }

    @Override
    public InputMode extendedkeybinds$getDefaultInputMode() {
        return this.extendedkeybinds$defaultInputMode;
    }

    @Override
    public void extendedkeybinds$setDefaultInputMode(InputMode inputMode) {
        Objects.requireNonNull(inputMode);
        if (!this.extendedkeybinds$isInputModeAllowed(inputMode)) {
            ExtendedKeybindsClient.LOGGER.warn("Default input mode {} is not allowed for key mapping {}, ignoring", inputMode, this.getName());
            return;
        }
        boolean followsDefault = this.extendedkeybinds$inputMode == this.extendedkeybinds$defaultInputMode;
        this.extendedkeybinds$defaultInputMode = inputMode;
        if (followsDefault) {
            this.extendedkeybinds$setInputMode(inputMode);
        }
    }

    @Override
    public Set<InputMode> extendedkeybinds$getAllowedInputModes() {
        return Collections.unmodifiableSet(this.extendedkeybinds$allowedModes());
    }

    @Override
    public void extendedkeybinds$setAllowedInputModes(Collection<InputMode> inputModes) {
        if (inputModes.isEmpty()) {
            ExtendedKeybindsClient.LOGGER.warn("Tried to set empty allowed input modes for key mapping {}, ignoring", this.getName());
            return;
        }
        Set<InputMode> allowed = EnumSet.copyOf(inputModes);
        this.extendedkeybinds$allowedInputModes = allowed;
        InputMode fallback = allowed.contains(InputMode.PRESS) ? InputMode.PRESS : allowed.iterator().next();
        if (!allowed.contains(this.extendedkeybinds$defaultInputMode)) {
            this.extendedkeybinds$defaultInputMode = fallback;
        }
        if (!allowed.contains(this.extendedkeybinds$inputMode)) {
            this.extendedkeybinds$setInputMode(this.extendedkeybinds$defaultInputMode);
        }
    }

    @Override
    public boolean extendedkeybinds$isInputModeAllowed(InputMode inputMode) {
        return this.extendedkeybinds$allowedModes().contains(inputMode);
    }

    @Override
    public void extendedkeybinds$resetInputState() {
        TriggerStateMachine state = this.extendedkeybinds$state;
        if (state != null) {
            state.reset();
            if (this.extendedkeybinds$inputMode != InputMode.PRESS) {
                this.isDown = false;
            }
        }
    }

    @Override
    public void extendedkeybinds$addTriggerListener(Consumer<KeyPressContext> listener) {
        Objects.requireNonNull(listener);
        if (this.extendedkeybinds$listeners == null) {
            this.extendedkeybinds$listeners = new CopyOnWriteArrayList<>();
        }
        this.extendedkeybinds$listeners.add(listener);
        this.extendedkeybinds$ensureState();
    }

    @Override
    public void extendedkeybinds$removeTriggerListener(Consumer<KeyPressContext> listener) {
        if (this.extendedkeybinds$listeners != null) {
            this.extendedkeybinds$listeners.remove(listener);
        }
    }

    @Override
    public void extendedkeybinds$preTick(long now) {
        TriggerStateMachine state = this.extendedkeybinds$state;
        if (state == null) {
            return;
        }
        InputSettings timings = KeymappingFileStorage.getInputSettings();
        if (state.isArmed() && !this.extendedkeybinds$isPhysicallyHeld()
                && state.onInput(false, this.extendedkeybinds$inputMode, timings, now, false)) {
            this.extendedkeybinds$fireListeners(state, now);
        }
        if (state.preTick(this.extendedkeybinds$inputMode, timings, now)) {
            this.extendedkeybinds$fireListeners(state, now);
        }
        this.extendedkeybinds$syncLogicalDown(state);
    }

    @Override
    public void extendedkeybinds$postTick() {
        TriggerStateMachine state = this.extendedkeybinds$state;
        if (state == null) {
            return;
        }
        state.postTick();
        this.extendedkeybinds$syncLogicalDown(state);
    }

    @Inject(method = "setDown", at = @At("HEAD"), cancellable = true)
    private void extendedkeybinds$onSetDown(boolean down, CallbackInfo ci) {
        TriggerStateMachine state = this.extendedkeybinds$state;
        if (state == null) {
            return;
        }
        long now = Util.getMillis();
        boolean phantom = TriggerManager.isSyncingKeyStates();
        if (state.onInput(down, this.extendedkeybinds$inputMode, KeymappingFileStorage.getInputSettings(), now, phantom)) {
            this.extendedkeybinds$fireListeners(state, now);
        }
        if (this.extendedkeybinds$inputMode != InputMode.PRESS) {
            this.isDown = state.logicalDown(this.extendedkeybinds$inputMode);
            ci.cancel();
        }
    }

    @Inject(method = "consumeClick", at = @At("HEAD"), cancellable = true)
    private void extendedkeybinds$onConsumeClick(CallbackInfoReturnable<Boolean> cir) {
        TriggerStateMachine state = this.extendedkeybinds$state;
        if (state != null && this.extendedkeybinds$inputMode != InputMode.PRESS) {
            this.clickCount = 0;
            cir.setReturnValue(state.consumeTrigger());
        }
    }

    @Inject(method = "release", at = @At("HEAD"))
    private void extendedkeybinds$onRelease(CallbackInfo ci) {
        this.extendedkeybinds$resetInputState();
    }

    @Inject(method = {"setKeyModifierAndCode", "setKey"}, at = @At("TAIL"))
    private void extendedkeybinds$onRebind(CallbackInfo ci) {
        this.extendedkeybinds$resetInputState();
    }

    @Inject(method = "setAll", at = @At("HEAD"))
    private static void extendedkeybinds$beforeSetAll(CallbackInfo ci) {
        TriggerManager.setSyncingKeyStates(true);
    }

    @Inject(method = "setAll", at = @At("RETURN"))
    private static void extendedkeybinds$afterSetAll(CallbackInfo ci) {
        TriggerManager.setSyncingKeyStates(false);
    }

    @Inject(method = "isDefault", at = @At("RETURN"), cancellable = true)
    private void extendedkeybinds$isDefault(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && this.extendedkeybinds$inputMode != this.extendedkeybinds$defaultInputMode) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "same", at = @At("RETURN"), cancellable = true)
    private void extendedkeybinds$same(KeyMapping that, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) {
            return;
        }
        if (this.getKey().equals(that.getKey()) && this.getKeyModifier() == that.getKeyModifier()
                && extendedkeybinds$areModesDistinguishable(this.extendedkeybinds$inputMode, ((ExtendedKeyMapping) that).extendedkeybinds$getInputMode())) {
            cir.setReturnValue(false);
        }
    }

    @Override
    public void setToDefault() {
        this.setKeyModifierAndCode(this.getDefaultKeyModifier(), this.getDefaultKey());
        this.extendedkeybinds$setInputMode(this.extendedkeybinds$defaultInputMode);
    }

    @Unique
    private Set<InputMode> extendedkeybinds$allowedModes() {
        if (this.extendedkeybinds$allowedInputModes == null) {
            this.extendedkeybinds$allowedInputModes = (Object) this instanceof ToggleKeyMapping
                    ? EnumSet.of(InputMode.PRESS)
                    : EnumSet.allOf(InputMode.class);
        }
        return this.extendedkeybinds$allowedInputModes;
    }

    @Unique
    private void extendedkeybinds$ensureState() {
        if (this.extendedkeybinds$state != null) {
            return;
        }
        boolean needsState = this.extendedkeybinds$inputMode != InputMode.PRESS
                || (this.extendedkeybinds$listeners != null && !this.extendedkeybinds$listeners.isEmpty());
        if (needsState) {
            TriggerStateMachine state = new TriggerStateMachine();
            if (this.isDown) {
                state.onInput(true, this.extendedkeybinds$inputMode, KeymappingFileStorage.getInputSettings(), Util.getMillis(), true);
            }
            this.extendedkeybinds$state = state;
            TriggerManager.track((KeyMapping) (Object) this);
        }
    }

    @Unique
    private void extendedkeybinds$syncLogicalDown(TriggerStateMachine state) {
        if (this.extendedkeybinds$inputMode != InputMode.PRESS) {
            this.isDown = state.logicalDown(this.extendedkeybinds$inputMode);
        }
    }

    @Unique
    private void extendedkeybinds$fireListeners(TriggerStateMachine state, long now) {
        List<Consumer<KeyPressContext>> listeners = this.extendedkeybinds$listeners;
        if (listeners == null || listeners.isEmpty()) {
            return;
        }
        InputMode mode = this.extendedkeybinds$inputMode;
        long heldMillis = switch (mode) {
            case PRESS, DOUBLE_CLICK -> 0L;
            case RELEASE, HOLD, CONTINUOUS -> state.getHeldMillis(now);
        };
        KeyPressContext context = new KeyPressContext((KeyMapping) (Object) this, mode, heldMillis, state.getHeldTicks());
        for (Consumer<KeyPressContext> listener : listeners) {
            try {
                listener.accept(context);
            } catch (RuntimeException e) {
                ExtendedKeybindsClient.LOGGER.error("Trigger listener of key mapping {} failed", this.getName(), e);
            }
        }
    }

    @Unique
    private boolean extendedkeybinds$isPhysicallyHeld() {
        InputConstants.Key key = this.getKey();
        return switch (key.getType()) {
            case KEYBOARD -> InputConstants.isKeyDown(key.getValue());
            case MOUSE -> extendedkeybinds$isMouseButtonDown(key.getValue());
        };
    }

    @Unique
    private static boolean extendedkeybinds$isMouseButtonDown(int button) {
        if (button < 1 || button > 32) {
            return true;
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int flags = SDLMouse.SDL_GetMouseState(stack.mallocFloat(1), stack.mallocFloat(1));
            return (flags & (1 << (button - 1))) != 0;
        }
    }

    @Unique
    private static boolean extendedkeybinds$areModesDistinguishable(InputMode a, InputMode b) {
        if (a == b) {
            return false;
        }
        return a != InputMode.PRESS && a != InputMode.CONTINUOUS && b != InputMode.PRESS && b != InputMode.CONTINUOUS;
    }
}
