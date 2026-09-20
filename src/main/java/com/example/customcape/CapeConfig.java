package com.example.customcape;

import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * CustomCape 配置：config/CustomCape/settings.txt
 * fpCape=true/false  第一人称披风可见开关（转头回望/俯冲时能看到披风飘动）
 */
public final class CapeConfig {
    public static boolean fpCape = true;

    private CapeConfig() {
    }

    public static Path file() {
        return FMLPaths.CONFIGDIR.get().resolve("CustomCape").resolve("settings.txt");
    }

    public static void load() {
        Map<String, String> kv = new HashMap<>();
        try {
            for (String line : Files.readAllLines(file(), StandardCharsets.UTF_8)) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) continue;
                int i = line.indexOf('=');
                kv.put(line.substring(0, i).trim().toLowerCase(),
                        line.substring(i + 1).trim().toLowerCase());
            }
        } catch (Exception ignored) {
        }
        fpCape = parseBool(kv.get("fpcape"), true);
    }

    public static void save() {
        try {
            Files.createDirectories(file().getParent());
            Files.writeString(file(), """
                    # CustomCape settings
                    # fpCape: 第一人称是否渲染披风 (true/false)
                    fpCape=%s
                    """.formatted(fpCape), StandardCharsets.UTF_8);
        } catch (Exception ignored) {
        }
    }

    private static boolean parseBool(String s, boolean def) {
        if (s == null) return def;
        return s.equals("true") || s.equals("1") || s.equals("on") || s.equals("yes");
    }
}
