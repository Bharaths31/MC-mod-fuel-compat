package dev.iafuelcompat.container;

import net.minecraft.world.item.ItemStack;

public interface FuelContainerAdapter {
    boolean matches(ItemStack stack);
    int getFuelTime(ItemStack stack);
    ItemStack getContainerReturn(ItemStack stack);
    String getName();
}
