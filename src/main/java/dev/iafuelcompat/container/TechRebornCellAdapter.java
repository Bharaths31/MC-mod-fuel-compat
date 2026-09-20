package dev.iafuelcompat.container;

import dev.iafuelcompat.fuel.FuelDefinition;
import dev.iafuelcompat.fuel.FuelRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import techreborn.items.DynamicCellItem;

public class TechRebornCellAdapter implements FuelContainerAdapter {
    @Override
    public boolean matches(ItemStack stack) {
        return stack.getItem() instanceof DynamicCellItem;
    }

    @Override
    public int getFuelTime(ItemStack stack) {
        if (!(stack.getItem() instanceof DynamicCellItem cellItem)) return 0;

        Fluid fluid = cellItem.getFluid(stack);
        if (fluid == Fluids.EMPTY) return 0;

        String fluidId = BuiltInRegistries.FLUID.getKey(fluid).toString();
        FuelDefinition def = FuelRegistry.getFluidFuel(fluidId);
        return def != null ? def.getEffectiveBurnTime() : 0;
    }

    @Override
    public ItemStack getContainerReturn(ItemStack stack) {
        return DynamicCellItem.getEmptyCell(1);
    }

    @Override
    public String getName() {
        return "TechRebornCellAdapter";
    }
}
