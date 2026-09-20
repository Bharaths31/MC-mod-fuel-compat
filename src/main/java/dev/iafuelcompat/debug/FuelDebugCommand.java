package dev.iafuelcompat.debug;

import com.mojang.brigadier.CommandDispatcher;
import dev.iafuelcompat.fuel.FuelDefinition;
import dev.iafuelcompat.fuel.FuelRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class FuelDebugCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("ia-fuels")
                .requires(src -> src.hasPermission(2))
                .executes(ctx -> {
                    CommandSourceStack source = ctx.getSource();
                    source.sendSuccess(() ->
                        Component.literal("§6=== IA Fuel Compatibility ==="), false);

                    for (FuelDefinition fuel : FuelRegistry.getAllFuels()) {
                        source.sendSuccess(() -> Component.literal(String.format(
                            "  §a%s §7(%s) → §e%d ticks §7[%s]",
                            fuel.id(), fuel.provider(),
                            fuel.getEffectiveBurnTime(),
                            fuel.enabled() ? "ON" : "OFF"
                        )), false);
                    }

                    return 1;
                })
        );
        
        dispatcher.register(
            Commands.literal("ia-fuels-hand")
                .requires(src -> src.hasPermission(2))
                .executes(ctx -> {
                    CommandSourceStack source = ctx.getSource();
                    net.minecraft.server.level.ServerPlayer player = source.getPlayer();
                    if (player != null) {
                        net.minecraft.world.item.ItemStack stack = player.getMainHandItem();
                        if (!stack.isEmpty()) {
                            source.sendSuccess(() -> Component.literal("Item: " + stack.getItem()), false);
                            source.sendSuccess(() -> Component.literal("Components: " + stack.getComponents().toString()), false);
                            
                            // Check fluid storage
                            net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext itemCtx = net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext.withConstant(stack);
                            net.fabricmc.fabric.api.transfer.v1.storage.Storage<net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant> storage = net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage.ITEM.find(stack, itemCtx);
                            source.sendSuccess(() -> Component.literal("Storage: " + (storage != null ? "YES" : "NO")), false);
                        } else {
                            source.sendSuccess(() -> Component.literal("Hand is empty!"), false);
                        }
                    }
                    return 1;
                })
        );
    }
}
