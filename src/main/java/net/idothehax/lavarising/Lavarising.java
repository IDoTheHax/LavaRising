package net.idothehax.lavarising;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Lavarising implements ModInitializer {
    public static final String MOD_ID = "lavarising";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static boolean lavaRisingEnabled = false;
    private static int lavaLevel = -64;
    private static int ticksUntilNextRise = 600;

    @Override
    public void onInitialize() {
        LOGGER.info("Lava Rising mod initialized");

        // Register the command
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("lavarising")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(CommandManager.literal("toggle")
                            .executes(context -> {
                                lavaRisingEnabled = !lavaRisingEnabled;
                                context.getSource().sendFeedback(
                                        () -> Text.literal("Lava rising " + (lavaRisingEnabled ? "enabled" : "disabled")),
                                        true
                                );
                                return 1;
                            }))
                    .then(CommandManager.literal("reset")
                            .executes(context -> {
                                lavaRisingEnabled = false;
                                lavaLevel = -64;
                                ticksUntilNextRise = 600;
                                context.getSource().sendFeedback(
                                        () -> Text.literal("Lava rising reset"),
                                        true
                                );
                                return 1;
                            }))
            );
        });

        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
    }

    private void onServerTick(MinecraftServer server) {
        if (!lavaRisingEnabled) return;

        ticksUntilNextRise--;
        if (ticksUntilNextRise <= 0) {
            ticksUntilNextRise = 600;
            raiseLavaLevel(server);
        }
    }

    private void raiseLavaLevel(MinecraftServer server) {
        if (lavaLevel >= 319) {
            lavaRisingEnabled = false;
            return;
        }

        lavaLevel++;
        server.getOverworld().getPlayers().forEach(player -> {
            player.sendMessage(Text.literal("Warning: Lava level has risen to " + lavaLevel), false);
        });

        LavaPlacement.updateLava(server);
        LOGGER.info("Lava level raised to {}", lavaLevel);
    }

    public static boolean isLavaRisingEnabled() {
        return lavaRisingEnabled;
    }

    public static int getLavaLevel() {
        return lavaLevel;
    }
}