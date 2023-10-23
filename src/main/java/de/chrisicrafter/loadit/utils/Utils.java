package de.chrisicrafter.loadit.utils;

import de.chrisicrafter.loadit.LoadIt;
import de.chrisicrafter.loadit.client.ClientDebugScreenData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
        } else LoadIt.LOGGER.debug("Message could not be sent, because server is null");
    }

    public static void forceLoad(ServerLevel world, BlockPos pos, int radiusLoad, int radiusUnload, boolean justUnload) {

        if(radiusUnload == 0) radiusUnload = radiusLoad;
        if(radiusLoad < 0 || radiusUnload < radiusLoad || radiusUnload > 8) return;

        int x1 = (pos.getX() / 16) - radiusUnload;
        int x2 = (pos.getX() / 16) + radiusUnload;
        int z1 = (pos.getZ() / 16) - radiusUnload;
        int z2 = (pos.getZ() / 16) + radiusUnload;
        int x3 = (pos.getX() / 16) - radiusLoad;
        int x4 = (pos.getX() / 16) + radiusLoad;
        int z3 = (pos.getZ() / 16) - radiusLoad;
        int z4 = (pos.getZ() / 16) + radiusLoad;

        for(int i = x1; i <= x2; i++) {
            for(int k = z1; k <= z2; k++) {
                boolean load = i >= x3 && i <= x4 && k >= z3 && k <= z4;
                if(justUnload) load = false;
                if(load) LoadIt.beaconData.chunksToLoad.add(new BlockPos(i, 0,  k)); else if(LoadIt.beaconData.loadedChunks.contains(new BlockPos(i, 0, k))) LoadIt.beaconData.chunksToUnload.add(new BlockPos(i, 0, k));
            }
        }

        updateLoadedChunks(world, false);
    }

    public static void updateLoadedChunks(ServerLevel world, boolean reloadLoaded) {
        if(reloadLoaded) {
            for (BlockPos pos : LoadIt.beaconData.loadedChunks) {
                world.setChunkForced(pos.getX(), pos.getZ(), true);
            }
        }
        for (BlockPos pos : LoadIt.beaconData.chunksToLoad) {
            world.setChunkForced(pos.getX(), pos.getZ(), true);
            LoadIt.beaconData.loadedChunks.add(pos);
        }
        LoadIt.beaconData.chunksToLoad.removeAll(LoadIt.beaconData.chunksToLoad);
        for (BlockPos pos : LoadIt.beaconData.chunksToUnload) {
            world.setChunkForced(pos.getX(), pos.getZ(), false);
            LoadIt.beaconData.loadedChunks.remove(pos);
        }
        LoadIt.beaconData.chunksToUnload.removeAll(LoadIt.beaconData.chunksToUnload);

        Utils.broadcastMessage("Load update:");
        Utils.broadcastMessage(LoadIt.beaconData.toString());
    }

    public static String toString(BlockPos pos) {
        return "[x=" + pos.getX() / 16 + "|z=" + pos.getZ() / 16 + "]";
    }

    @OnlyIn(Dist.CLIENT)
    public static String getDebugString(double x, double z) {
        BlockPos pos = new BlockPos(x < 0 ? (int) x - 16 : (int) x, 0, z < 0 ? (int) z - 16 : (int) z);
        if(isLoadedByLoadIt(pos.getX(), pos.getZ()) && isLoadedChunkLoader(pos.getX(), pos.getZ())) return ChatFormatting.YELLOW + "Active chunkloader in this chunk";
        else if(isLoadedByLoadIt(pos.getX(), pos.getZ())) return ChatFormatting.GREEN + "Chunk is forceloaded by LoadIt";
        else return  "Chunk is not forceloaded by LoadIt";
    }

    private static boolean isLoadedChunkLoader(int x, int z) {
        for(BlockPos loaderPos : ClientDebugScreenData.getBeaconData().getPositions()) {
            if(loaderPos.getX() / 16 == x / 16 && loaderPos.getZ() / 16 == z / 16) return true;
        }
        return false;
    }

    private static boolean isLoadedByLoadIt(int x, int z) {
        return ClientDebugScreenData.getBeaconData().loadedChunks.contains(new BlockPos(x / 16, 0, z / 16));
    }

    public static void overloadAnimation(ServerLevel world, BlockPos blockPos) {
        world.playSound(null, blockPos, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    public static void loadAnimation(ServerLevel world, BlockPos blockPos) {
        world.playSound(null, blockPos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    public static void unloadAnimation(ServerLevel world, BlockPos blockPos) {
        world.playSound(null, blockPos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
}
