package dev.iafuelcompat.integration;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class TechRebornTankStorage extends SnapshotParticipant<ItemStack> implements SingleSlotStorage<FluidVariant> {

    private final ContainerItemContext context;
    private ItemStack currentStack;

    public TechRebornTankStorage(ItemStack stack, ContainerItemContext context) {
        this.context = context;
        this.currentStack = stack.copy();
    }

    private CompoundTag getTankTag() {
        if (!currentStack.has(DataComponents.BLOCK_ENTITY_DATA)) return null;
        CustomData data = currentStack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data == null) return null;
        CompoundTag tag = data.copyTag();
        if (!tag.contains("TankStorage", Tag.TAG_COMPOUND)) return null;
        return tag.getCompound("TankStorage");
    }

    @Override
    public boolean isResourceBlank() {
        return getResource().isBlank();
    }

    @Override
    public FluidVariant getResource() {
        CompoundTag tank = getTankTag();
        if (tank == null) return FluidVariant.blank();
        
        if (tank.contains("fluid", Tag.TAG_STRING)) {
            String fluidStr = tank.getString("fluid");
            Fluid fluid = BuiltInRegistries.FLUID.getOptional(ResourceLocation.parse(fluidStr)).orElse(Fluids.EMPTY);
            return fluid == Fluids.EMPTY ? FluidVariant.blank() : FluidVariant.of(fluid);
        } else if (tank.contains("fluid", Tag.TAG_COMPOUND)) {
            CompoundTag fluidTag = tank.getCompound("fluid");
            if (fluidTag.contains("fluid", Tag.TAG_STRING)) {
                String fluidStr = fluidTag.getString("fluid");
                Fluid fluid = BuiltInRegistries.FLUID.getOptional(ResourceLocation.parse(fluidStr)).orElse(Fluids.EMPTY);
                return fluid == Fluids.EMPTY ? FluidVariant.blank() : FluidVariant.of(fluid);
            }
        }
        return FluidVariant.blank();
    }

    @Override
    public long getAmount() {
        CompoundTag tank = getTankTag();
        if (tank == null) return 0;
        if (tank.contains("amount", Tag.TAG_ANY_NUMERIC)) {
            return tank.getLong("amount");
        }
        return 0;
    }

    @Override
    public long getCapacity() {
        return 81000 * 10; // Default capacity fallback (10 buckets)
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        return 0; // We only support extraction for IAFuelCompat logic to keep it simple and safe.
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        FluidVariant currentVariant = getResource();
        if (!currentVariant.equals(resource)) return 0;

        long currentAmount = getAmount();
        long extracted = Math.min(maxAmount, currentAmount);
        
        if (extracted > 0) {
            updateSnapshots(transaction);
            
            long newAmount = currentAmount - extracted;
            CompoundTag rootTag = currentStack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).copyTag();
            CompoundTag tankTag = rootTag.getCompound("TankStorage");
            
            tankTag.putLong("amount", newAmount);
            if (newAmount <= 0) {
                tankTag.remove("fluid");
                tankTag.putLong("amount", 0);
            }
            rootTag.put("TankStorage", tankTag);
            
            ItemStack newStack = currentStack.copy();
            newStack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(rootTag));
            
            if (this.context.exchange(ItemVariant.of(newStack), 1, transaction) == 1) {
                this.currentStack = newStack;
                return extracted;
            } else {
                return 0;
            }
        }
        return 0;
    }

    @Override
    protected ItemStack createSnapshot() {
        return this.currentStack.copy();
    }

    @Override
    protected void readSnapshot(ItemStack snapshot) {
        this.currentStack = snapshot.copy();
    }
}
