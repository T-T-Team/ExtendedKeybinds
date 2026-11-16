package tnt.tarkovcraft.extendedkeybinds.input;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

// TODO move to Configuration library?
public record InputSettings(int holdDuration, int doubleClickInterval) {

    public static final int MIN_HOLD_DURATION = 50;
    public static final int MAX_HOLD_DURATION = 5000;
    public static final int MIN_DOUBLE_CLICK_INTERVAL = 100;
    public static final int MAX_DOUBLE_CLICK_INTERVAL = 1000;

    public static final InputSettings DEFAULT = new InputSettings(500, 300);

    public static final Codec<InputSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("hold_duration").forGetter(settings -> Optional.of(settings.holdDuration())),
            Codec.INT.optionalFieldOf("double_click_interval").forGetter(settings -> Optional.of(settings.doubleClickInterval()))
    ).apply(instance, (hold, doubleClick) -> new InputSettings(
            hold.orElse(DEFAULT.holdDuration()),
            doubleClick.orElse(DEFAULT.doubleClickInterval())
    )));

    public InputSettings {
        holdDuration = Math.clamp(holdDuration, MIN_HOLD_DURATION, MAX_HOLD_DURATION);
        doubleClickInterval = Math.clamp(doubleClickInterval, MIN_DOUBLE_CLICK_INTERVAL, MAX_DOUBLE_CLICK_INTERVAL);
    }
}
