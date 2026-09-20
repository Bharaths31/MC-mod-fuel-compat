package dev.iafuelcompat.mixin;

import dev.iafuelcompat.container.FuelContainerAdapter;
import immersive_aircraft.cobalt.registration.CobaltFuelRegistry;
import immersive_aircraft.entity.EngineVehicle;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = EngineVehicle.class, remap = false)
public abstract class EngineVehicleRefuelMixin {
    @Redirect(
        method = "refuel(I)V",
        at = @At(
            value = "INVOKE",
            target = "Limmersive_aircraft/cobalt/registration/CobaltFuelRegistry;getCraftingRemainingItem(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"
        )
    )
    private ItemStack iaFuelCompat$getCraftingRemainingItem(
        CobaltFuelRegistry instance,
        ItemStack stack
    ) {
        FuelContainerAdapter adapter = dev.iafuelcompat.fuel.FuelRegistry.getAdapter(stack);
        if (adapter != null) {
            return adapter.getContainerReturn(stack);
        }
        return instance.getCraftingRemainingItem(stack);
    }
}
