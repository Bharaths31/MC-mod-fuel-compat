package dev.iafuelcompat.fuel;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public final class FluidDiscovery {
    private static final Logger LOGGER = LoggerFactory.getLogger("IA-Fuels");

    public static boolean itemExists(String itemId) {
        return resolveItem(itemId, "check").isPresent();
    }

    public static boolean fluidExists(String fluidId) {
        return resolveFluid(fluidId, "check").isPresent();
    }

    public static Optional<Item> resolveItem(String itemId, String context) {
        try {
            ResourceLocation loc = ResourceLocation.parse(itemId);
            Optional<Item> item = BuiltInRegistries.ITEM.getOptional(loc);
            if (item.isEmpty() && !context.equals("check")) {
                LOGGER.warn("[IA-Fuels] WARNING: {} → item {} NOT FOUND in registry", context, itemId);
            }
            return item;
        } catch (Exception e) {
            LOGGER.error("[IA-Fuels] Error parsing item ID: {}", itemId, e);
            return Optional.empty();
        }
    }

    public static Optional<Fluid> resolveFluid(String fluidId, String context) {
        try {
            ResourceLocation loc = ResourceLocation.parse(fluidId);
            Optional<Fluid> fluid = BuiltInRegistries.FLUID.getOptional(loc);
            if (fluid.isEmpty() && !context.equals("check")) {
                LOGGER.warn("[IA-Fuels] WARNING: {} → fluid {} NOT FOUND in registry", context, fluidId);
            }
            return fluid;
        } catch (Exception e) {
            LOGGER.error("[IA-Fuels] Error parsing fluid ID: {}", fluidId, e);
            return Optional.empty();
        }
    }

    public static Optional<String> tryItemCandidates(List<String> candidates, String context) {
        for (String candidate : candidates) {
            if (itemExists(candidate)) {
                return Optional.of(candidate);
            }
        }
        if (!context.equals("check")) {
            LOGGER.warn("[IA-Fuels] WARNING: {} → None of the candidate items found in registry: {}", context, candidates);
        }
        return Optional.empty();
    }
}
