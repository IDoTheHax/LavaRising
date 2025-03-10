package net.idothehax.lavarising;

import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

public class LavaPlacement {
    public static void updateLava(MinecraftServer server) {
        if (!Lavarising.isLavaRisingEnabled()) return;

        ServerWorld world = server.getOverworld();
        int lavaLevel = Lavarising.getLavaLevel();

        ServerChunkManager chunkManager = world.getChunkManager();

        int viewDistance = server.getPlayerManager().getViewDistance();

        server.getPlayerManager().getPlayerList().forEach(player -> {
            ChunkPos playerChunkPos = player.getChunkPos();
            int centerX = playerChunkPos.x;
            int centerZ = playerChunkPos.z;

            for (int dx = -viewDistance; dx <= viewDistance; dx++) {
                for (int dz = -viewDistance; dz <= viewDistance; dz++) {
                    int chunkX = centerX + dx;
                    int chunkZ = centerZ + dz;
                    ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);

                    Chunk chunk = chunkManager.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);

                    if (chunk != null) {
                        int minX = chunkPos.getStartX();
                        int minZ = chunkPos.getStartZ();
                        int maxX = chunkPos.getEndX();
                        int maxZ = chunkPos.getEndZ();

                        for (int x = minX; x <= maxX; x++) {
                            for (int z = minZ; z <= maxZ; z++) {
                                int topY = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);
                                BlockPos pos = new BlockPos(x, lavaLevel, z);

                                if (pos.getY() < topY) {
                                    if (world.getBlockState(pos).isAir() ||
                                            !world.getBlockState(pos).isSolidBlock(world, pos)) {
                                        world.setBlockState(pos, Blocks.LAVA.getDefaultState());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }
}