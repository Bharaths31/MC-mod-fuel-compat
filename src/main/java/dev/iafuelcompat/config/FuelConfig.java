package dev.iafuelcompat.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class FuelConfig {
    public boolean enabled = true;
    public boolean debugLogging = false;
    public int baseBurnTime = 1200;

    public Map<String, FuelEntry> fuels = new LinkedHashMap<>();
    public ContainerConfig containers = new ContainerConfig();

    public static class FuelEntry {
        public boolean enabled;
        public float multiplier;

        public FuelEntry(boolean enabled, float multiplier) {
            this.enabled = enabled;
            this.multiplier = multiplier;
        }
    }

    public static class ContainerConfig {
        public boolean buckets = true;
        public boolean techrebornCells = true;
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static FuelConfig loadOrCreate(Path configDir) {
        Path file = configDir.resolve("ia-fuel-compat.json");
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file)) {
                return GSON.fromJson(reader, FuelConfig.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        FuelConfig config = createDefault();
        config.save(configDir);
        return config;
    }

    public void save(Path configDir) {
        Path file = configDir.resolve("ia-fuel-compat.json");
        try {
            Files.createDirectories(configDir);
            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(this, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static FuelConfig createDefault() {
        FuelConfig config = new FuelConfig();
        config.fuels.put("oritech_fuel", new FuelEntry(true, 1.35f));
        config.fuels.put("oritech_biofuel", new FuelEntry(true, 0.75f));
        config.fuels.put("oritech_diesel", new FuelEntry(true, 1.0f));
        config.fuels.put("tr_diesel", new FuelEntry(true, 1.0f));
        config.fuels.put("tr_biofuel", new FuelEntry(true, 0.75f));
        config.fuels.put("tr_nitro_diesel", new FuelEntry(false, 1.25f));
        config.fuels.put("tr_nitrofuel", new FuelEntry(false, 1.5f));
        config.fuels.put("tr_turbo_diesel", new FuelEntry(true, 1.75f));
        config.fuels.put("oritech_turbo_diesel", new FuelEntry(true, 1.75f));
        config.fuels.put("mi_turbo_diesel", new FuelEntry(true, 1.75f));
        return config;
    }
}
