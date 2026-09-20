package dev.iafuelcompat.fuel;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for all fuel definitions.
 * Supports two paths:
 * <ul>
 *   <li><b>Item fuels</b> — looked up by item registry ID (e.g. bucket items)</li>
 *   <li><b>Fluid fuels</b> — looked up by fluid registry ID, used by the tank mixin
 *       to drain fluid from any Fabric FluidStorage container</li>
 * </ul>
 *
 * Fuel candidates are queued during {@code onInitialize()} and lazily resolved
 * against the registries on first access, after all mods have finished loading.
 */
public final class FuelRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger("IA-Fuels");

    // Resolved fuels (populated lazily)
    private static final Map<String, Integer> ITEM_FUELS = new ConcurrentHashMap<>();
    private static final Map<String, FuelDefinition> FLUID_FUELS = new ConcurrentHashMap<>();
    private static final Map<String, FuelDefinition> ALL_FUELS = new LinkedHashMap<>();

    // Candidates queued during onInitialize() — resolved on first access
    private static final Map<String, FuelDefinition> ITEM_CANDIDATES = new ConcurrentHashMap<>();
    private static final Map<String, FuelDefinition> FLUID_CANDIDATES = new ConcurrentHashMap<>();

    private static volatile boolean resolved = false;

    /**
     * Queue an item fuel candidate for lazy resolution.
     * The item ID will be validated against BuiltInRegistries on first access.
     */
    public static void queueItemFuel(String itemId, FuelDefinition def) {
        ITEM_CANDIDATES.put(itemId, def);
        LOGGER.debug("[IA-Fuels] Queued item fuel candidate: {} → {}", itemId, def.id());
    }

    /**
     * Queue a fluid fuel candidate for lazy resolution.
     * The fluid ID will be validated against BuiltInRegistries on first access.
     */
    public static void queueFluidFuel(String fluidId, FuelDefinition def) {
        FLUID_CANDIDATES.put(fluidId, def);
        LOGGER.debug("[IA-Fuels] Queued fluid fuel candidate: {} → {}", fluidId, def.id());
    }

    /**
     * Resolve all queued candidates against the registries.
     * Called lazily on first fuel lookup — by this point all mods have finished registering.
     */
    private static void resolveIfNeeded() {
        if (resolved) return;
        synchronized (FuelRegistry.class) {
            if (resolved) return;

            LOGGER.info("[IA-Fuels] Resolving fuel candidates ({} items, {} fluids)...",
                ITEM_CANDIDATES.size(), FLUID_CANDIDATES.size());

            for (var entry : ITEM_CANDIDATES.entrySet()) {
                String itemId = entry.getKey();
                FuelDefinition def = entry.getValue();
                try {
                    ResourceLocation loc = ResourceLocation.parse(itemId);
                    if (BuiltInRegistries.ITEM.getOptional(loc).isPresent()) {
                        ITEM_FUELS.put(itemId, def.getEffectiveBurnTime());
                        ALL_FUELS.put(def.id(), def);
                        LOGGER.info("[IA-Fuels]   ✓ {} → {} ({} ticks)",
                            def.id(), itemId, def.getEffectiveBurnTime());
                    } else {
                        LOGGER.warn("[IA-Fuels]   ✗ {} → item {} NOT FOUND in registry", def.id(), itemId);
                    }
                } catch (Exception e) {
                    LOGGER.error("[IA-Fuels]   ✗ Error resolving item {}: {}", itemId, e.getMessage());
                }
            }

            for (var entry : FLUID_CANDIDATES.entrySet()) {
                String fluidId = entry.getKey();
                FuelDefinition def = entry.getValue();
                try {
                    ResourceLocation loc = ResourceLocation.parse(fluidId);
                    if (BuiltInRegistries.FLUID.getOptional(loc).isPresent()) {
                        FLUID_FUELS.put(fluidId, def);
                        ALL_FUELS.put(def.id(), def);
                        LOGGER.info("[IA-Fuels]   ✓ {} → fluid {} ({} ticks)",
                            def.id(), fluidId, def.getEffectiveBurnTime());

                        // Also try to register the bucket variant automatically
                        String bucketId = fluidId + "_bucket";
                        ResourceLocation bucketLoc = ResourceLocation.parse(bucketId);
                        if (BuiltInRegistries.ITEM.getOptional(bucketLoc).isPresent()) {
                            ITEM_FUELS.put(bucketId, def.getEffectiveBurnTime());
                            LOGGER.info("[IA-Fuels]   ✓ {} bucket → {} ({} ticks)",
                                def.id(), bucketId, def.getEffectiveBurnTime());
                        }
                    } else {
                        LOGGER.warn("[IA-Fuels]   ✗ {} → fluid {} NOT FOUND in registry", def.id(), fluidId);
                    }
                } catch (Exception e) {
                    LOGGER.error("[IA-Fuels]   ✗ Error resolving fluid {}: {}", fluidId, e.getMessage());
                }
            }

            LOGGER.info("[IA-Fuels] Resolution complete: {} item fuels, {} fluid fuels registered.",
                ITEM_FUELS.size(), FLUID_FUELS.size());

            resolved = true;
        }
    }

    /**
     * Get fuel time for an ItemStack by its item registry ID.
     * Used by UtilsFuelTimeMixin to override IA's Utils.getFuelTime().
     */
    public static int getFuelTime(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        resolveIfNeeded();

        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        Integer time = ITEM_FUELS.get(itemId);
        if (time != null && time > 0) return time;

        // Dynamically check if the item contains a valid fluid fuel
        net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext ctx = net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext.withConstant(stack);
        net.fabricmc.fabric.api.transfer.v1.storage.Storage<net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant> storage = net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage.ITEM.find(stack, ctx);
        if (storage != null) {
            for (net.fabricmc.fabric.api.transfer.v1.storage.StorageView<net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant> view : storage) {
                if (!view.isResourceBlank()) {
                    String fluidId = BuiltInRegistries.FLUID.getKey(view.getResource().getFluid()).toString();
                    FuelDefinition fluidDef = FLUID_FUELS.get(fluidId);
                    if (fluidDef != null) {
                        return fluidDef.getEffectiveBurnTime();
                    }
                }
            }
        }

        // Fallback for Tech Reborn Tank Units which don't expose FluidStorage.ITEM
        if (dev.iafuelcompat.integration.TechRebornTankHelper.isTRTank(stack)) {
            String fluidId = dev.iafuelcompat.integration.TechRebornTankHelper.getFluid(stack);
            if (fluidId != null) {
                FuelDefinition fluidDef = FLUID_FUELS.get(fluidId);
                if (fluidDef != null && dev.iafuelcompat.integration.TechRebornTankHelper.getAmount(stack) > 0) {
                    return fluidDef.getEffectiveBurnTime();
                }
            }
        }

        return 0;
    }

    /**
     * Direct item fuel registration (bypasses lazy resolution).
     */
    public static void registerItemFuel(String itemId, FuelDefinition def) {
        ALL_FUELS.put(def.id(), def);
        ITEM_FUELS.put(itemId, def.getEffectiveBurnTime());
    }

    /**
     * Direct fluid fuel registration (bypasses lazy resolution).
     */
    public static void registerFluidFuel(String fluidId, FuelDefinition def) {
        ALL_FUELS.put(def.id(), def);
        FLUID_FUELS.put(fluidId, def);
    }

    /**
     * Lookup a FuelDefinition by fluid registry ID.
     * Used by EngineVehicleRefuelTankMixin to calculate burn time
     * when draining fluid from a tank item.
     */
    public static FuelDefinition getFluidFuel(String fluidId) {
        resolveIfNeeded();
        return FLUID_FUELS.get(fluidId);
    }

    public static Integer getItemFuelTime(String itemId) {
        resolveIfNeeded();
        return ITEM_FUELS.get(itemId);
    }

    public static Collection<FuelDefinition> getAllFuels() {
        resolveIfNeeded();
        return ALL_FUELS.values();
    }

    public static int getCandidateCount() {
        return ITEM_CANDIDATES.size() + FLUID_CANDIDATES.size();
    }
}
