package dev.iafuelcompat.integration;

import dev.iafuelcompat.config.FuelConfig;
import dev.iafuelcompat.container.TechRebornCellAdapter;
import dev.iafuelcompat.fuel.FluidDiscovery;
import dev.iafuelcompat.fuel.FuelDefinition;
import dev.iafuelcompat.fuel.FuelRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

public final class TechRebornIntegration {
    private static final Logger LOGGER = LoggerFactory.getLogger("IA-Fuels");

    private static final Map<String, String> FUEL_FLUIDS = Map.of(
        "tr_diesel",       "techreborn:diesel",
        "tr_biofuel",      "techreborn:biofuel",
        "tr_nitro_diesel", "techreborn:nitro_diesel",
        "tr_nitrofuel",    "techreborn:nitrofuel"
    );

    public static void register(FuelConfig config) {
        LOGGER.info("[IA-Fuels] Tech Reborn detected, registering fuels...");

        for (var entry : FUEL_FLUIDS.entrySet()) {
            String fuelId = entry.getKey();
            String fluidId = entry.getValue();

            FuelConfig.FuelEntry fuelEntry = config.fuels.get(fuelId);
            if (fuelEntry == null || !fuelEntry.enabled) {
                LOGGER.info("[IA-Fuels]   {} → DISABLED by config", fuelId);
                continue;
            }

            Optional<Fluid> fluid = FluidDiscovery.resolveFluid(fluidId, "TechReborn/" + fuelId);
            if (fluid.isEmpty()) {
                LOGGER.warn("[IA-Fuels]   {} → fluid {} NOT FOUND", fuelId, fluidId);
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

            FuelRegistry.registerFluidFuel(fluidId, def);

            String bucketCandidate = fluidId + "_bucket";
            FluidDiscovery.resolveItem(bucketCandidate, "TechReborn/" + fuelId + "/bucket")
                .ifPresent(item -> {
                    String bucketId = BuiltInRegistries.ITEM.getKey(item).toString();
                    FuelRegistry.registerItemFuel(bucketId, def);
                    LOGGER.info("[IA-Fuels]   {} bucket → {} ({} ticks)",
                        fuelId, bucketId, def.getEffectiveBurnTime());
                });

            LOGGER.info("[IA-Fuels]   {} → fluid {} ({} ticks, cell support: ON)",
                fuelId, fluidId, def.getEffectiveBurnTime());
        }

        if (config.containers.techrebornCells) {
            FuelRegistry.registerAdapter(new TechRebornCellAdapter());
            LOGGER.info("[IA-Fuels]   TechReborn cell adapter: ENABLED");
        }
    }
}
