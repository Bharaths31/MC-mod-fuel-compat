package dev.iafuelcompat.container;

import dev.iafuelcompat.fuel.FuelRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BucketFuelAdapter implements FuelContainerAdapter {
    @Override
    public boolean matches(ItemStack stack) {
        return stack.getItem() instanceof BucketItem;
    }

    @Override
    public int getFuelTime(ItemStack stack) {
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        Integer time = FuelRegistry.getItemFuelTime(itemId);
        return time != null ? time : 0;
    }

    @Override
    public ItemStack getContainerReturn(ItemStack stack) {
        return new ItemStack(Items.BUCKET);
    }

    @Override
    public String getName() {
        return "BucketAdapter";
    }
}
