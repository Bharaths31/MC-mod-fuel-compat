package dev.iafuelcompat.mixin;

import dev.iafuelcompat.fuel.FuelDefinition;
import dev.iafuelcompat.fuel.FuelRegistry;
import immersive_aircraft.entity.EngineVehicle;
import immersive_aircraft.entity.inventory.SparseSimpleInventory;
import immersive_aircraft.entity.inventory.VehicleInventoryDescription;
import immersive_aircraft.entity.inventory.slots.SlotDescription;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = EngineVehicle.class, remap = false)
public abstract class EngineVehicleRefuelTankMixin {
    @Shadow @Final protected int[] fuel;

    @Inject(method = "refuel(I)V", at = @At("HEAD"), cancellable = true)
    private void iaFuelCompat$refuelFromTank(int slotIndex, CallbackInfo ci) {
        EngineVehicle vehicle = (EngineVehicle) (Object) this;
        List<SlotDescription> boilerSlots = vehicle.getInventoryDescription().getSlots(VehicleInventoryDescription.BOILER);
        if (slotIndex >= boilerSlots.size()) return;

        int realSlotIndex = boilerSlots.get(slotIndex).index();
        SparseSimpleInventory inv = vehicle.getInventory();
        ItemStack stack = inv.getItem(realSlotIndex);
        if (stack.isEmpty()) return;

        // Ensure we only process items that have FluidStorage (tanks, cells, buckets)
        ContainerItemContext ctx = ContainerItemContext.ofSingleSlot(InventoryStorage.of(inv, null).getSlot(realSlotIndex));
        Storage<FluidVariant> storage = FluidStorage.ITEM.find(stack, ctx);
        
        if (storage == null) {
            // Fallback for Tech Reborn Tank Units which don't expose FluidStorage.ITEM
            if (dev.iafuelcompat.integration.TechRebornTankHelper.isTRTank(stack)) {
                String fluidId = dev.iafuelcompat.integration.TechRebornTankHelper.getFluid(stack);
                if (fluidId != null) {
                    FuelDefinition def = FuelRegistry.getFluidFuel(fluidId);
                    if (def != null && this.fuel[slotIndex] <= 1000) {
                        long amount = dev.iafuelcompat.integration.TechRebornTankHelper.getAmount(stack);
                        if (amount > 0) {
                            long toExtract = Math.min(amount, FluidConstants.BUCKET);
                            dev.iafuelcompat.integration.TechRebornTankHelper.extract(stack, toExtract);
                            int fuelTime = (int) ((double) toExtract / FluidConstants.BUCKET * def.getEffectiveBurnTime());
                            if (fuelTime > 0) {
                                this.fuel[slotIndex] += fuelTime;
                                ci.cancel(); // Skip vanilla item shrinking logic
                                return;
                            }
                        }
                    }
                }
            }
            return;
        }

        for (StorageView<FluidVariant> view : storage) {
            if (view.isResourceBlank()) continue;

            String fluidId = BuiltInRegistries.FLUID.getKey(view.getResource().getFluid()).toString();
            FuelDefinition def = FuelRegistry.getFluidFuel(fluidId);
            if (def == null) continue;

            // IA standard fuel limit is around 1000 for accepting more fuel.
            // If the buffer has room, we extract 1 bucket's worth of fluid.
            if (this.fuel[slotIndex] <= 1000) {
                try (Transaction t = Transaction.openOuter()) {
                    long extracted = storage.extract(view.getResource(), FluidConstants.BUCKET, t);
                    if (extracted > 0) {
                        // Calculate proportionate fuel time based on extracted droplets
                        int fuelTime = (int) ((double) extracted / FluidConstants.BUCKET * def.getEffectiveBurnTime());
                        if (fuelTime > 0) {
                            this.fuel[slotIndex] += fuelTime;
                            t.commit();
                            ci.cancel(); // Skip vanilla item shrinking logic
                            return;
                        }
                    }
                }
            }
        }
    }
}
