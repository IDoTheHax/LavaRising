package net.idothehax.lavarising;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Config {
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "lavarising.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Config instance;

    // Default values
    private int ticksBetweenRises = 600;
    private int blocksPerRise = 1;

    private Config() {
        // Private constructor for singleton
    }

    public static Config getInstance() {
        if (instance == null) {
            instance = loadConfig();
        }
        return instance;
    }

    public int getTicksBetweenRises() {
        return ticksBetweenRises;
    }

    public void setTicksBetweenRises(int ticks) {
        this.ticksBetweenRises = Math.max(1, ticks);
        saveConfig();
    }

    public int getBlocksPerRise() {
        return blocksPerRise;
    }

    public void setBlocksPerRise(int blocks) {
        this.blocksPerRise = Math.max(1, blocks);
        saveConfig();
    }

    private static Config loadConfig() {
        Config config = new Config();
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                config = GSON.fromJson(reader, Config.class);
            } catch (IOException e) {
                Lavarising.LOGGER.error("Failed to load config", e);
            }
        }
        config.saveConfig();
        return config;
    }

    private void saveConfig() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            Lavarising.LOGGER.error("Failed to save config", e);
        }
    }
}