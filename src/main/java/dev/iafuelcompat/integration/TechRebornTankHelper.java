package dev.iafuelcompat.integration;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;

public class TechRebornTankHelper {
    public static boolean isTRTank(ItemStack stack) {
        String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return id.startsWith("techreborn:") && id.endsWith("_tank_unit");
    }

    public static String getFluid(ItemStack stack) {
        if (!stack.has(DataComponents.BLOCK_ENTITY_DATA)) return null;
        CustomData data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data == null) return null;
        
        CompoundTag tag = data.copyTag();
        if (!tag.contains("TankStorage", 10)) return null;
        
        CompoundTag tank = tag.getCompound("TankStorage");
        if (tank.contains("fluid", 8)) {
            return tank.getString("fluid"); // Simple string format
        } else if (tank.contains("fluid", 10)) {
            CompoundTag fluidTag = tank.getCompound("fluid");
            if (fluidTag.contains("fluid", 8)) {
                return fluidTag.getString("fluid"); // Compound format with "fluid" key
            }
        }
        return null;
    }

    public static long getAmount(ItemStack stack) {
        if (!stack.has(DataComponents.BLOCK_ENTITY_DATA)) return 0;
        CustomData data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data == null) return 0;
        
        CompoundTag tag = data.copyTag();
        if (!tag.contains("TankStorage", 10)) return 0;
        
        CompoundTag tank = tag.getCompound("TankStorage");
        if (tank.contains("amount", 99)) { // Any numeric type
            return tank.getLong("amount");
        }
        return 0;
    }

    public static void extract(ItemStack stack, long amountToExtract) {
        if (!stack.has(DataComponents.BLOCK_ENTITY_DATA)) return;
        CustomData data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data == null) return;
        
        CompoundTag tag = data.copyTag();
        if (!tag.contains("TankStorage", 10)) return;
        
        CompoundTag tank = tag.getCompound("TankStorage");
        if (tank.contains("amount", 99)) {
            long currentAmount = tank.getLong("amount");
            tank.putLong("amount", Math.max(0, currentAmount - amountToExtract));
            
            // If empty, clear fluid?
            if (tank.getLong("amount") <= 0) {
                tank.remove("fluid");
                tank.putLong("amount", 0);
            }
            
            stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(tag));
        }
    }
}
