package com.erik.lumenrealis.config;

import com.erik.lumenrealis.LumenRealisMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static LumenConfig config = LumenConfig.defaults();

    private ConfigManager() {}

    public static LumenConfig get() { return config; }

    public static void set(LumenConfig newConfig) {
        config = newConfig;
        save();
    }

    public static void load() {
        Path path = configPath();
        if (Files.exists(path)) {
            try {
                String json = Files.readString(path);
                LumenConfig loaded = GSON.fromJson(json, LumenConfig.class);
                if (loaded != null) {
                    config = loaded;
                    LumenRealisMod.LOGGER.info("Config loaded: preset={}", config.preset());
                }
            } catch (Exception e) {
                LumenRealisMod.LOGGER.warn("Failed to load config, using defaults: {}", e.getMessage());
            }
        } else {
            save();
        }
    }

    public static void save() {
        try {
            Path path = configPath();
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(config));
        } catch (IOException e) {
            LumenRealisMod.LOGGER.warn("Failed to save config: {}", e.getMessage());
        }
    }

    public static void loadGlobal() {
        // No-op on server side. Real loading happens in load() from client init.
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("lumenrealis.json");
    }
}
