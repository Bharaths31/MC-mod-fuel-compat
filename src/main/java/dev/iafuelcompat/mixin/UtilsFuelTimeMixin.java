package dev.iafuelcompat.mixin;

import immersive_aircraft.util.Utils;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Utils.class, remap = false)
public abstract class UtilsFuelTimeMixin {
    @Inject(
        method = "getFuelTime",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void iaFuelCompat$getFuelTime(
        ItemStack fuel,
        CallbackInfoReturnable<Integer> cir
    ) {
        int time = dev.iafuelcompat.fuel.FuelRegistry.getFuelTime(fuel);
        if (time > 0) {
            cir.setReturnValue(time);
        }
    }
}
