package dev.iafuelcompat.integration;

import dev.iafuelcompat.config.FuelConfig;
import dev.iafuelcompat.fuel.FuelDefinition;
import dev.iafuelcompat.fuel.FuelRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public final class ModernIndustrializationIntegration {
    private static final Logger LOGGER = LoggerFactory.getLogger("IA-Fuels");

    private static final Map<String, String> FUEL_FLUIDS = Map.of(
        "mi_turbo_diesel", "modern_industrialization:turbo_diesel"
    );

    public static void register(FuelConfig config) {
        LOGGER.info("[IA-Fuels] Queueing Modern Industrialization fluids for lazy resolution...");

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
                "MI " + fuelId.replace("mi_", ""),
                "modern_industrialization",
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
