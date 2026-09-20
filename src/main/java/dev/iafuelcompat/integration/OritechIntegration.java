package dev.iafuelcompat.integration;

import dev.iafuelcompat.config.FuelConfig;
import dev.iafuelcompat.fuel.FuelDefinition;
import dev.iafuelcompat.fuel.FuelRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public final class OritechIntegration {
    private static final Logger LOGGER = LoggerFactory.getLogger("IA-Fuels");

    private static final Map<String, String> FUEL_CANDIDATES = Map.of(
        "oritech_fuel",    "oritech:still_fuel_bucket",
        "oritech_biofuel", "oritech:still_biofuel_bucket",
        "oritech_diesel",  "oritech:still_diesel_bucket"
    );

    public static void register(FuelConfig config) {
        LOGGER.info("[IA-Fuels] Oritech detected, queueing fuels for lazy resolution...");

        for (var entry : FUEL_CANDIDATES.entrySet()) {
            String fuelId = entry.getKey();
            String bucketItemId = entry.getValue();

            FuelConfig.FuelEntry fuelEntry = config.fuels.get(fuelId);
            if (fuelEntry == null || !fuelEntry.enabled) {
                LOGGER.info("[IA-Fuels]   {} → DISABLED by config", fuelId);
                continue;
            }

            FuelDefinition def = new FuelDefinition(
                fuelId,
                "Oritech " + fuelId.replace("oritech_", ""),
                "oritech",
                bucketItemId.replace("_bucket", ""),
                config.baseBurnTime,
                fuelEntry.multiplier,
                true
            );

            FuelRegistry.queueItemFuel(bucketItemId, def);
        }
    }
}
