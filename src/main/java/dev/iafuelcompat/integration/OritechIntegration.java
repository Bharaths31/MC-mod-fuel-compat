package dev.iafuelcompat.integration;

import dev.iafuelcompat.config.FuelConfig;
import dev.iafuelcompat.fuel.FluidDiscovery;
import dev.iafuelcompat.fuel.FuelDefinition;
import dev.iafuelcompat.fuel.FuelRegistry;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

public final class OritechIntegration {
    private static final Logger LOGGER = LoggerFactory.getLogger("IA-Fuels");

    private static final Map<String, String> FUEL_CANDIDATES = Map.of(
        "oritech_fuel",    "oritech:still_fuel_bucket",
        "oritech_biofuel", "oritech:still_biofuel_bucket",
        "oritech_diesel",  "oritech:still_diesel_bucket"
    );

    public static void register(FuelConfig config) {
        LOGGER.info("[IA-Fuels] Oritech detected, registering fuels...");

        for (var entry : FUEL_CANDIDATES.entrySet()) {
            String fuelId = entry.getKey();
            String bucketItemId = entry.getValue();

            FuelConfig.FuelEntry fuelEntry = config.fuels.get(fuelId);
            if (fuelEntry == null || !fuelEntry.enabled) {
                LOGGER.info("[IA-Fuels]   {} → DISABLED by config", fuelId);
                continue;
            }

            Optional<Item> item = FluidDiscovery.resolveItem(bucketItemId, "Oritech/" + fuelId);
            if (item.isEmpty()) {
                LOGGER.warn("[IA-Fuels]   {} → item {} NOT FOUND in registry", fuelId, bucketItemId);
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

            FuelRegistry.registerItemFuel(bucketItemId, def);
            LOGGER.info("[IA-Fuels]   {} → {} ({} ticks)",
                fuelId, bucketItemId, def.getEffectiveBurnTime());
        }
    }
}
