package dev.iafuelcompat.container;

import dev.iafuelcompat.fuel.FuelDefinition;
import dev.iafuelcompat.fuel.FuelRegistry;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TechRebornCellAdapter implements FuelContainerAdapter {
    @Override
    public boolean matches(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals("techreborn:cell");
    }

    @Override
    public int getFuelTime(ItemStack stack) {
        if (!matches(stack)) return 0;

        Storage<FluidVariant> storage = FluidStorage.ITEM.find(stack, ContainerItemContext.withConstant(stack));
        if (storage != null) {
            for (StorageView<FluidVariant> view : storage) {
                if (!view.isResourceBlank()) {
                    String fluidId = BuiltInRegistries.FLUID.getKey(view.getResource().getFluid()).toString();
                    FuelDefinition def = FuelRegistry.getFluidFuel(fluidId);
                    return def != null ? def.getEffectiveBurnTime() : 0;
                }
            }
        }
        return 0;
    }

    @Override
    public ItemStack getContainerReturn(ItemStack stack) {
        Item cell = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse("techreborn:cell")).orElse(Items.AIR);
        return new ItemStack(cell);
    }

    @Override
    public String getName() {
        return "TechRebornCellAdapter";
    }
}
