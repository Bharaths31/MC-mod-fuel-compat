package dev.iafuelcompat.debug;

import com.mojang.brigadier.CommandDispatcher;
import dev.iafuelcompat.container.FuelContainerAdapter;
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

                    for (FuelContainerAdapter adapter : FuelRegistry.getAdapters()) {
                        source.sendSuccess(() -> Component.literal(
                            "  §bAdapter: " + adapter.getName()
                        ), false);
                    }

                    return 1;
                })
        );
    }
}
