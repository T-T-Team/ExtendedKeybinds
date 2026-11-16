package dev.tnt.extendedkeybinds.input;

import java.io.File;
import java.util.Map;

public final class KeymappingFileStorage {

    private static final File CONFIG_FILE = new File("./extended_keybind_settings.json");

    public static void load() {

    }

    public static void save() {

    }

    public record Settings(InputSettings options, Map<String, KeybindSettings> keymap) {}
}
