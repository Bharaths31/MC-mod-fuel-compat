package dev.iafuelcompat.fuel;

import dev.iafuelcompat.container.FuelContainerAdapter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class FuelRegistry {
    private static final Map<String, Integer> ITEM_FUELS = new ConcurrentHashMap<>();
    private static final Map<String, FuelDefinition> FLUID_FUELS = new ConcurrentHashMap<>();
    private static final List<FuelContainerAdapter> ADAPTERS = new CopyOnWriteArrayList<>();
    private static final Map<String, FuelDefinition> ALL_FUELS = new LinkedHashMap<>();

    public static int getFuelTime(ItemStack stack) {
        if (stack.isEmpty()) return 0;

        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        Integer time = ITEM_FUELS.get(itemId);
        if (time != null && time > 0) return time;

        for (FuelContainerAdapter adapter : ADAPTERS) {
            if (adapter.matches(stack)) {
                int t = adapter.getFuelTime(stack);
                if (t > 0) return t;
            }
        }
        return 0;
    }

    @Nullable
    public static FuelContainerAdapter getAdapter(ItemStack stack) {
        if (stack.isEmpty()) return null;
        for (FuelContainerAdapter adapter : ADAPTERS) {
            if (adapter.matches(stack)) return adapter;
        }
        return null;
    }

    public static void registerItemFuel(String itemId, FuelDefinition def) {
        ALL_FUELS.put(def.id(), def);
        ITEM_FUELS.put(itemId, def.getEffectiveBurnTime());
    }

    public static void registerFluidFuel(String fluidId, FuelDefinition def) {
        ALL_FUELS.put(def.id(), def);
        FLUID_FUELS.put(fluidId, def);
    }

    public static void registerAdapter(FuelContainerAdapter adapter) {
        ADAPTERS.add(adapter);
    }

    public static Collection<FuelDefinition> getAllFuels() {
        return ALL_FUELS.values();
    }

    public static List<FuelContainerAdapter> getAdapters() {
        return ADAPTERS;
    }
    
    public static FuelDefinition getFluidFuel(String fluidId) {
        return FLUID_FUELS.get(fluidId);
    }

    public static Integer getItemFuelTime(String itemId) {
        return ITEM_FUELS.get(itemId);
    }
}
