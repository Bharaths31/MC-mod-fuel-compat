package dev.iafuelcompat.integration;

import dev.iafuelcompat.config.FuelConfig;
import dev.iafuelcompat.fuel.FuelDefinition;
import dev.iafuelcompat.fuel.FuelRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public final class TechRebornIntegration {
    private static final Logger LOGGER = LoggerFactory.getLogger("IA-Fuels");

    private static final Map<String, String> FUEL_FLUIDS = Map.of(
        "tr_diesel",       "techreborn:diesel",
        "tr_biofuel",      "techreborn:biofuel",
        "tr_nitro_diesel", "techreborn:nitro_diesel",
        "tr_nitrofuel",    "techreborn:nitrofuel",
        "tr_turbo_diesel", "techreborn:turbo_diesel"
    );

    public static void register(FuelConfig config) {
        LOGGER.info("[IA-Fuels] Tech Reborn detected, queueing fluids for lazy resolution...");

        for (var entry : FUEL_FLUIDS.entrySet()) {
            String fuelId = entry.getKey();
            String fluidId = entry.getValue();

            FuelConfig.FuelEntry fuelEntry = config.fuels.get(fuelId);
            if (fuelEntry == null || !fuelEntry.enabled) {
                LOGGER.info("[IA-Fuels]   {} → DISABLED by config", fuelId);
                continue;
            }

            FuelDefinition def = new FuelDefinition(
                fuelId,
                "TR " + fuelId.replace("tr_", ""),
                "techreborn",
                fluidId,
                config.baseBurnTime,
                fuelEntry.multiplier,
                true
            );

            // Queue fluid fuel for lazy resolution
            FuelRegistry.queueFluidFuel(fluidId, def);
        }
    }
}
