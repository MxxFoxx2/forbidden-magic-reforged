package dev.visfox.forbiddenreforged.common;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import java.util.List;

@EventBusSubscriber(modid = FmIds.MODID)
public final class FmCommands {
    private FmCommands() {
    }

    @SubscribeEvent
    static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal(FmIds.MODID)
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(
                    Commands.literal("aspects")
                        .executes(FmCommands::showAspects)
                )
        );
    }

    private static int showAspects(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        List<String> lines = HostAdapterRegistry.require().aspectDiagnostics();
        source.sendSuccess(
            () -> Component.literal("Aspect registry: entries=" + lines.size()),
            false
        );
        for (String line : lines) {
            source.sendSuccess(() -> Component.literal(line), false);
        }
        return 1;
    }
}
