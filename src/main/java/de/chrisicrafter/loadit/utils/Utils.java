package de.chrisicrafter.loadit.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
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

    public static boolean changeForceLoad(ServerLevel world, BlockPos pos, int radiusLoad, int radiusUnload, boolean justUnload) {
        if(radiusUnload == 0) radiusUnload = radiusLoad;
        if(radiusLoad < 0 || radiusUnload < radiusLoad || radiusUnload > 8) return false;
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
                world.setChunkForced(i, k, load);
            }
        }

        return true;
    }

    public static String toString(BlockPos pos) {
        return "[x=" + pos.getX() + "y=" + pos.getY() + "z=" + pos.getZ() + "]";
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
