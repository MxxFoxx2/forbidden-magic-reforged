package dev.visfox.forbiddenreforged;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4AspectDiagnostics;

@EventBusSubscriber(modid = ForbiddenMagic.MODID)
public final class FmCommands {
    private FmCommands() {
    }

    @SubscribeEvent
    static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal(ForbiddenMagic.MODID)
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(
                    Commands.literal("aspects")
                        .executes(FmCommands::showAspects)
                )
        );
    }

    private static int showAspects(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        int fmAddonCount = Tc4AspectDiagnostics.addonLines().size();
        source.sendSuccess(
            () -> Component.literal("Aspect registry: total=" + FmAspects.count()
                + " builtin=" + FmAspects.BUILTIN_ASPECT_COUNT
                + " registeredAddon=" + (FmAspects.count() - FmAspects.BUILTIN_ASPECT_COUNT)
                + " fmAddon=" + fmAddonCount),
            false
        );
        for (String line : Tc4AspectDiagnostics.registryLines()) {
            source.sendSuccess(() -> Component.literal(line), false);
        }
        return 1;
    }
}
