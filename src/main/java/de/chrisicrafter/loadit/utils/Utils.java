package de.chrisicrafter.loadit.utils;

import de.chrisicrafter.loadit.LoadIt;
import de.chrisicrafter.loadit.client.ClientDebugScreenData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.server.ServerLifecycleHooks;

public class Utils {
    public static void broadcastMessage(String message) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                player.sendSystemMessage(Component.literal(message));
            }
        }
    }

    public static int beaconLevel(ServerLevel world, BlockPos pos) {
        BeaconBlockEntity blockEntity = (BeaconBlockEntity) world.getBlockEntity(pos);
        return blockEntity.getUpdateTag().getInt("Levels");
    }

    @OnlyIn(Dist.CLIENT)
    public static String getDebugString(double x, double z) {
        ChunkPos pos = toChunkPos(x, z);
        if(loaded(ClientDebugScreenData.getBeaconData(), pos) && chunkLoader(ClientDebugScreenData.getBeaconData(), pos)) return ChatFormatting.YELLOW + "Active chunkloader in this chunk";
        else if(loaded(ClientDebugScreenData.getBeaconData(), pos)) return ChatFormatting.GREEN + "Chunk is forceloaded by LoadIt";
        else return ChatFormatting.WHITE + "Chunk is not forceloaded by LoadIt";
    }

    public static boolean loaded(BeaconData data, ChunkPos pos) {
        return data.loadedChunks.contains(pos);
    }

    public static boolean chunkLoader(BeaconData data, BlockPos pos) {
        return data.loaderPositions.contains(pos);
    }

    public static boolean chunkLoader(BeaconData data, ChunkPos pos) {
        for(BlockPos blockPos : data.loaderPositions) {
            ChunkPos chunkPos = toChunkPos(blockPos);
            if(chunkPos.equals(pos)) return true;
        }
        return false;
    }

    public static ChunkPos toChunkPos(double x, double z) {
        x = x < 0 ?  x - 16 : (int) x;
        z = z < 0 ? z - 16 : (int) z;
        return new ChunkPos((int) x / 16, (int) z / 16);
    }

    public static ChunkPos toChunkPos(BlockPos pos) {
        return toChunkPos(pos.getX(), pos.getZ());
    }

    public static String toString(ChunkPos pos) {
        return "[x=" + pos.x + "|z=" + pos.z + "]";
    }
}
