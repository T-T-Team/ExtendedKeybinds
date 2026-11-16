package dev.tnt.extendedkeybinds.input;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record KeybindSettings(InputMode inputMode) {

    public static final Codec<KeybindSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            InputMode.CODEC.fieldOf("input_mode").forGetter(KeybindSettings::inputMode)
    ).apply(instance, KeybindSettings::new));
}
