package dev.iafuelcompat;

import dev.iafuelcompat.config.FuelConfig;
import dev.iafuelcompat.container.BucketFuelAdapter;
import dev.iafuelcompat.debug.FuelDebugCommand;
import dev.iafuelcompat.fuel.FuelRegistry;
import dev.iafuelcompat.integration.OritechIntegration;
import dev.iafuelcompat.integration.TechRebornIntegration;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class IAFuelCompat implements ModInitializer {
    public static final String MOD_ID = "ia-fuel-compat";
    private static final Logger LOGGER = LoggerFactory.getLogger("IA-Fuels");

    @Override
    public void onInitialize() {
        LOGGER.info("[IA-Fuels] Initializing IA Fuel Compatibility v{}", getVersion());

        Path configDir = FabricLoader.getInstance().getConfigDir();
        FuelConfig config = FuelConfig.loadOrCreate(configDir);

        if (!config.enabled) {
            LOGGER.info("[IA-Fuels] Mod disabled by config");
            return;
        }

        if (config.containers.buckets) {
            FuelRegistry.registerAdapter(new BucketFuelAdapter());
        }

        if (FabricLoader.getInstance().isModLoaded("oritech")) {
            String ver = getModVersion("oritech");
            LOGGER.info("[IA-Fuels] Oritech {} detected", ver);
            OritechIntegration.register(config);
        } else {
            LOGGER.info("[IA-Fuels] Oritech not present, skipping");
        }

        if (FabricLoader.getInstance().isModLoaded("techreborn")) {
            String ver = getModVersion("techreborn");
            LOGGER.info("[IA-Fuels] Tech Reborn {} detected", ver);
            TechRebornIntegration.register(config);
        } else {
            LOGGER.info("[IA-Fuels] Tech Reborn not present, skipping");
        }

        CommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess, environment) ->
                FuelDebugCommand.register(dispatcher)
        );

        LOGGER.info("[IA-Fuels] Queued {} fuel candidates for lazy resolution. Registered {} adapters. Bridge READY.",
            FuelRegistry.getCandidateCount(),
            FuelRegistry.getAdapters().size());
    }

    private String getVersion() {
        return FabricLoader.getInstance()
            .getModContainer(MOD_ID)
            .map(c -> c.getMetadata().getVersion().getFriendlyString())
            .orElse("unknown");
    }

    private String getModVersion(String modId) {
        return FabricLoader.getInstance()
            .getModContainer(modId)
            .map(c -> c.getMetadata().getVersion().getFriendlyString())
            .orElse("unknown");
    }
}
