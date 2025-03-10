package net.idothehax.lavarising;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mojang.brigadier.arguments.IntegerArgumentType;

public class Lavarising implements ModInitializer {
    public static final String MOD_ID = "lavarising";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static boolean lavaRisingEnabled = false;
    private static int lavaLevel = -64;
    private static int ticksUntilNextRise;
    private final Config config = Config.getInstance();

    @Override
    public void onInitialize() {
        LOGGER.info("Lava Rising mod initialized");

        ticksUntilNextRise = config.getTicksBetweenRises();

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
                                ticksUntilNextRise = config.getTicksBetweenRises();
                                context.getSource().sendFeedback(
                                        () -> Text.literal("Lava rising reset"),
                                        true
                                );
                                return 1;
                            }))
                    .then(CommandManager.literal("speed")
                            .then(CommandManager.argument("ticks", IntegerArgumentType.integer(1))
                                    .executes(context -> {
                                        int ticks = IntegerArgumentType.getInteger(context, "ticks");
                                        config.setTicksBetweenRises(ticks);
                                        ticksUntilNextRise = ticks;
                                        context.getSource().sendFeedback(
                                                () -> Text.literal("Lava rise speed set to every " + ticks + " ticks (" + (ticks / 20.0) + " seconds)"),
                                                true
                                        );
                                        return 1;
                                    })))
                    .then(CommandManager.literal("height")
                            .then(CommandManager.argument("blocks", IntegerArgumentType.integer(1))
                                    .executes(context -> {
                                        int blocks = IntegerArgumentType.getInteger(context, "blocks");
                                        config.setBlocksPerRise(blocks);
                                        context.getSource().sendFeedback(
                                                () -> Text.literal("Lava rise height set to " + blocks + " blocks per rise"),
                                                true
                                        );
                                        return 1;
                                    })))
            );
        });

        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
    }

    private void onServerTick(MinecraftServer server) {
        if (!lavaRisingEnabled) return;

        ticksUntilNextRise--;
        if (ticksUntilNextRise <= 0) {
            ticksUntilNextRise = config.getTicksBetweenRises();
            raiseLavaLevel(server);
        }
    }

    private void raiseLavaLevel(MinecraftServer server) {
        int blocksToRise = config.getBlocksPerRise();
        if (lavaLevel + blocksToRise >= 319) {
            lavaRisingEnabled = false;
            return;
        }

        lavaLevel += blocksToRise;
        server.getOverworld().getPlayers().forEach(player -> {
            player.sendMessage(Text.literal("Warning: Lava level has risen to " + lavaLevel + " (+" + blocksToRise + " blocks)"), false);
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