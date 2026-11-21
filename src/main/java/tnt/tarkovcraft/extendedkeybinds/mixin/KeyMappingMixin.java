package tnt.tarkovcraft.extendedkeybinds.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ToggleKeyMapping;
import net.neoforged.neoforge.client.extensions.IKeyMappingExtension;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tnt.tarkovcraft.extendedkeybinds.ExtendedKeybindsClient;
import tnt.tarkovcraft.extendedkeybinds.api.InputMode;
import tnt.tarkovcraft.extendedkeybinds.api.KeyPressContext;
import tnt.tarkovcraft.extendedkeybinds.api.ScrollKeys;
import tnt.tarkovcraft.extendedkeybinds.input.ExtendedKeyMapping;
import tnt.tarkovcraft.extendedkeybinds.input.TriggerManager;
import tnt.tarkovcraft.extendedkeybinds.input.TriggerStateMachine;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin implements IKeyMappingExtension, ExtendedKeyMapping {

    @Shadow
    @Final
    private static Map<String, KeyMapping> ALL;

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

    @Shadow
    public abstract boolean isUnbound();

    @Override
    public InputMode extendedkeybinds$getInputMode() {
        return this.extendedkeybinds$inputMode;
    }

    @Override
    public InputMode extendedkeybinds$getEffectiveInputMode() {
        return ScrollKeys.isScrollKey(this.getKey())
                ? InputMode.PRESS
                : this.extendedkeybinds$inputMode;
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
        if (this.extendedkeybinds$isLegacy() && !EnumSet.copyOf(inputModes).equals(EnumSet.of(InputMode.PRESS))) {
            ExtendedKeybindsClient.LOGGER.warn("Toggle key mapping {} only supports {} input mode, ignoring", this.getName(), InputMode.PRESS);
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
        }
        if (!this.extendedkeybinds$isLegacy()) {
            this.isDown = false;
            this.clickCount = 0;
        }
    }

    @Override
    public void extendedkeybinds$addTriggerListener(Consumer<KeyPressContext> listener) {
        Objects.requireNonNull(listener);
        if (this.extendedkeybinds$listeners == null) {
            this.extendedkeybinds$listeners = new CopyOnWriteArrayList<>();
        }
        this.extendedkeybinds$listeners.add(listener);
        this.extendedkeybinds$getOrCreateState();
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
        // release may get lost (focus change, screen swallowing the event), synthesize it
        if (state.isArmed() && !this.extendedkeybinds$isPhysicallyHeld()) {
            state.onInput(false, this.extendedkeybinds$getEffectiveInputMode(), now, false);
        }
        state.preTick(this.extendedkeybinds$getEffectiveInputMode(), now);
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

    @Inject(method = "set(Lcom/mojang/blaze3d/platform/InputConstants$Key;Z)V", at = @At("HEAD"))
    private static void extendedkeybinds$beforeDispatch(InputConstants.Key key, boolean state, CallbackInfo ci) {
        TriggerManager.beginDispatch();
    }

    @Inject(method = "set(Lcom/mojang/blaze3d/platform/InputConstants$Key;Z)V", at = @At("RETURN"))
    private static void extendedkeybinds$afterDispatch(InputConstants.Key key, boolean state, CallbackInfo ci) {
        TriggerManager.endDispatch();
    }

    @Inject(method = "setDown", at = @At("HEAD"), cancellable = true)
    private void extendedkeybinds$onSetDown(boolean down, CallbackInfo ci) {
        boolean legacy = this.extendedkeybinds$isLegacy();
        TriggerStateMachine state = legacy ? this.extendedkeybinds$state : this.extendedkeybinds$getOrCreateState();
        if (state == null) {
            return;
        }
        state.onInput(down, this.extendedkeybinds$getEffectiveInputMode(), TriggerManager.now(), TriggerManager.isSyncingKeyStates());
        if (!legacy) {
            this.isDown = state.logicalDown(this.extendedkeybinds$getEffectiveInputMode());
            ci.cancel();
        }
    }

    @Inject(method = "consumeClick", at = @At("HEAD"), cancellable = true)
    private void extendedkeybinds$onConsumeClick(CallbackInfoReturnable<Boolean> cir) {
        if (this.extendedkeybinds$isLegacy()) {
            return;
        }
        this.clickCount = 0;
        cir.setReturnValue(this.extendedkeybinds$getOrCreateState().consumeTrigger());
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
                && extendedkeybinds$areModesDistinguishable(this.extendedkeybinds$getEffectiveInputMode(), ((ExtendedKeyMapping) that).extendedkeybinds$getEffectiveInputMode())) {
            cir.setReturnValue(false);
        }
    }

    @Override
    public void setToDefault() {
        this.setKeyModifierAndCode(this.getDefaultKeyModifier(), this.getDefaultKey());
        this.extendedkeybinds$setInputMode(this.extendedkeybinds$defaultInputMode);
    }

    @Unique
    private boolean extendedkeybinds$isLegacy() {
        return (Object) this instanceof ToggleKeyMapping;
    }

    @Unique
    private Set<InputMode> extendedkeybinds$allowedModes() {
        if (this.extendedkeybinds$allowedInputModes == null) {
            this.extendedkeybinds$allowedInputModes = this.extendedkeybinds$isLegacy()
                    ? EnumSet.of(InputMode.PRESS)
                    : EnumSet.allOf(InputMode.class);
        }
        return this.extendedkeybinds$allowedInputModes;
    }

    @Unique
    private TriggerStateMachine extendedkeybinds$getOrCreateState() {
        TriggerStateMachine state = this.extendedkeybinds$state;
        if (state == null) {
            state = new TriggerStateMachine(this::extendedkeybinds$competingModes, this::extendedkeybinds$fireListeners);
            this.extendedkeybinds$state = state;
            TriggerManager.track((KeyMapping) (Object) this);
        }
        return state;
    }

    @Unique
    private void extendedkeybinds$syncLogicalDown(TriggerStateMachine state) {
        if (!this.extendedkeybinds$isLegacy()) {
            this.isDown = state.logicalDown(this.extendedkeybinds$getEffectiveInputMode());
        }
    }

    @Unique
    private Set<InputMode> extendedkeybinds$competingModes() {
        Set<InputMode> modes = EnumSet.noneOf(InputMode.class);
        if (this.isUnbound()) {
            return modes;
        }
        KeyMapping self = (KeyMapping) (Object) this;
        InputConstants.Key key = this.getKey();
        InputMode mode = this.extendedkeybinds$getEffectiveInputMode();
        for (KeyMapping other : ALL.values()) {
            if (other == self || !key.equals(other.getKey()) || this.getKeyModifier() != other.getKeyModifier()
                    || !other.isConflictContextAndModifierActive()) {
                continue;
            }
            InputMode otherMode = ((ExtendedKeyMapping) other).extendedkeybinds$getEffectiveInputMode();
            if (extendedkeybinds$areModesDistinguishable(mode, otherMode)) {
                modes.add(otherMode);
            }
        }
        return modes;
    }

    @Unique
    private void extendedkeybinds$fireListeners(long heldMillis, int heldTicks) {
        List<Consumer<KeyPressContext>> listeners = this.extendedkeybinds$listeners;
        if (listeners == null || listeners.isEmpty()) {
            return;
        }
        KeyPressContext context = new KeyPressContext((KeyMapping) (Object) this, this.extendedkeybinds$getEffectiveInputMode(), heldMillis, heldTicks);
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
        if (ScrollKeys.isScrollKey(key)) {
            return false;
        }
        Window window = Minecraft.getInstance().getWindow();
        return switch (key.getType()) {
            case KEYSYM -> InputConstants.isKeyDown(window, key.getValue());
            case SCANCODE -> true;
            case MOUSE -> extendedkeybinds$isMouseButtonDown(window, key.getValue());
        };
    }

    @Unique
    private static boolean extendedkeybinds$isMouseButtonDown(Window window, int button) {
        if (button < GLFW.GLFW_MOUSE_BUTTON_1 || button > GLFW.GLFW_MOUSE_BUTTON_LAST) {
            return true;
        }
        return GLFW.glfwGetMouseButton(window.handle(), button) == GLFW.GLFW_PRESS;
    }

    @Unique
    private static boolean extendedkeybinds$areModesDistinguishable(InputMode a, InputMode b) {
        if (a == b || a == InputMode.CONTINUOUS || b == InputMode.CONTINUOUS) {
            return false;
        }
        boolean aTap = a == InputMode.PRESS || a == InputMode.RELEASE;
        boolean bTap = b == InputMode.PRESS || b == InputMode.RELEASE;
        return !(aTap && bTap);
    }
}
