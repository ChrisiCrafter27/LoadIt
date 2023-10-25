package de.chrisicrafter.loadit.utils;

import de.chrisicrafter.loadit.client.ClientDebugScreenData;
import de.chrisicrafter.loadit.data.BeaconData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Utils {

    public static int beaconLevel(ServerLevel level, BlockPos pos) {
        BeaconBlockEntity blockEntity = (BeaconBlockEntity) level.getBlockEntity(pos);
        assert blockEntity != null;
        return blockEntity.getUpdateTag().getInt("Levels");
    }

    @OnlyIn(Dist.CLIENT)
    public static String getDebugString(double x, double z) {
        ChunkPos pos = toChunkPos(x, z);
        if(loaded(ClientDebugScreenData.getBeaconData(), pos) && chunkLoader(ClientDebugScreenData.getBeaconData(), pos)) return ChatFormatting.YELLOW + "Active chunk loader in this chunk";
        else if(loaded(ClientDebugScreenData.getBeaconData(), pos)) return ChatFormatting.GREEN + "Chunk is force loaded by LoadIt";
        else return ChatFormatting.WHITE + "Chunk is not force loaded by LoadIt";
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
