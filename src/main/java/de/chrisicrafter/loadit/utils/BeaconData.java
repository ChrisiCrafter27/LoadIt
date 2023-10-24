package de.chrisicrafter.loadit.utils;

import de.chrisicrafter.loadit.LoadIt;
import de.chrisicrafter.loadit.networking.ModMessages;
import de.chrisicrafter.loadit.networking.packet.DebugScreenDataS2CPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;

public class BeaconData extends SavedData {
    public final ArrayList<BlockPos> loaderPositions;
    public final ArrayList<ChunkPos> loadedChunks;

    public static SavedData.Factory<BeaconData> factory() {
        return new SavedData.Factory<>(BeaconData::new, BeaconData::load, DataFixTypes.LEVEL);
    }

    public BeaconData() {
        loaderPositions = new ArrayList<>();
        loadedChunks = new ArrayList<>();
    }

    public BeaconData(ArrayList<BlockPos> loaderPositions, ArrayList<ChunkPos> loadedChunks) {
        this.loaderPositions = loaderPositions;
        this.loadedChunks = loadedChunks;
    }

    @Override
    public void setDirty() {
        super.setDirty();
        LoadIt.sendData = true;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        //save to tag
        int size;

        size = loaderPositions.size();
        tag.putInt("size_pos", size);
        for(int i = 0; i < size; i++) {
            tag.put("loader_pos_" + i, NbtUtils.writeBlockPos(loaderPositions.get(i)));
        }

        size = loadedChunks.size();
        tag.putInt("size_loaded", size);
        for(int i = 0; i < size; i++) {
            tag.putLong("chunk_loaded_" + i, loadedChunks.get(i).toLong());
        }

        return tag;
    }

    public static BeaconData load(CompoundTag tag) {
        //load from tag
        ArrayList<BlockPos> loaderPositions = new ArrayList<>();
        ArrayList<ChunkPos> loadedChunks = new ArrayList<>();
        int size;

        size = tag.getInt("size_pos");
        for(int i = 0; i < size; i++) {
            loaderPositions.add(NbtUtils.readBlockPos(tag.getCompound("loader_pos_" + i)));
        }

        size = tag.getInt("size_loaded");
        for(int i = 0; i < size; i++) {
            loadedChunks.add(new ChunkPos(tag.getLong("chunk_loaded_" + i)));
        }

        return new BeaconData(loaderPositions, loadedChunks);
    }

    public void beaconShiftUse(ServerLevel world, ServerPlayer player, BlockPos blockPos) {
        ChunkPos chunkPos = Utils.toChunkPos(blockPos);
        if(Utils.chunkLoader(LoadIt.getBeaconData(world), blockPos)) {
            LoadIt.getBeaconData(world).loaderPositions.remove(blockPos);
            world.playSound(null, blockPos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0F, 1.0F);
            world.getServer().getPlayerList().getPlayer(player.getUUID()).sendSystemMessage(Component.literal(ChatFormatting.RED + "Disabled chunkloading at chunk " + Utils.toString(chunkPos) + " (Radius : " + Utils.beaconLevel(world, blockPos) + ")"));
        } else if(!Utils.chunkLoader(LoadIt.getBeaconData(world), chunkPos)) {
            LoadIt.getBeaconData(world).loaderPositions.add(blockPos);
            world.playSound(null, blockPos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.0F);
            world.getServer().getPlayerList().getPlayer(player.getUUID()).sendSystemMessage(Component.literal(ChatFormatting.GREEN + "Enabled chunkloading at chunk " + Utils.toString(chunkPos) + " (radius: " + Utils.beaconLevel(world, blockPos) + ")"));
        } else {
            world.playSound(null, blockPos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1.0F, 1.0F);
            world.getServer().getPlayerList().getPlayer(player.getUUID()).sendSystemMessage(Component.literal(ChatFormatting.YELLOW + "There is already an active chunkloader in this chunk"));
        }
    }

    public static void update(ServerLevel world) {
        ArrayList<ChunkPos> chunksToLoad = new ArrayList<>();
        LoadIt.getBeaconData(world).loaderPositions.removeIf(pos -> !world.getBlockState(pos).is(Blocks.BEACON));
        for(BlockPos blockPos : LoadIt.getBeaconData(world).loaderPositions) {
            int beaconLevel = Utils.beaconLevel(world, blockPos);
            ChunkPos chunkPos = Utils.toChunkPos(blockPos);
            for(ChunkPos pos : getChunks(chunkPos, beaconLevel)) {
                if(!chunksToLoad.contains(pos)) chunksToLoad.add(pos);
            }
        }
        if(!LoadIt.getBeaconData(world).loadedChunks.containsAll(chunksToLoad) || !chunksToLoad.containsAll(LoadIt.getBeaconData(world).loadedChunks)) {
            LoadIt.LOGGER.info("Update Difference");
            LoadIt.LOGGER.info("Loaded Chunks: " + LoadIt.getBeaconData(world).loadedChunks.size());
            LoadIt.LOGGER.info("Chunks to load: " + chunksToLoad.size());
            for(ChunkPos pos : LoadIt.getBeaconData(world).loadedChunks) {
                if(!chunksToLoad.contains(pos)) forceLoad(world, pos, false);
                LoadIt.LOGGER.info("Unload Chunk");
            }
            for(ChunkPos pos : chunksToLoad) {
                if(!LoadIt.getBeaconData(world).loadedChunks.contains(pos)) forceLoad(world, pos, true);
                LoadIt.LOGGER.info("Load Chunk");
            }
            LoadIt.getBeaconData(world).loadedChunks.removeIf((ignored) -> true);
            LoadIt.getBeaconData(world).loadedChunks.addAll(chunksToLoad);
            LoadIt.LOGGER.info("Loaded Chunks: " + LoadIt.getBeaconData(world).loadedChunks.size());
            ModMessages.sendToPlayer(new DebugScreenDataS2CPacket(LoadIt.getBeaconData(world)));
            LoadIt.getBeaconData(world).setDirty();
            LoadIt.LOGGER.info("Update End");
            LoadIt.LOGGER.info(" ");
        }
    }

    public static ArrayList<ChunkPos> getChunks(ChunkPos pos, int radius) {
        ArrayList<ChunkPos> list = new ArrayList<>();
        int x1 = pos.x - radius;
        int x2 = pos.x + radius;
        int z1 = pos.z - radius;
        int z2 = pos.z + radius;
        for(int i = x1; i <= x2; i++) {
            for(int k = z1; k <= z2; k++) {
                list.add(new ChunkPos(i, k));
            }
        }
        return list;
    }

    private static void forceLoad(ServerLevel world, ChunkPos pos, boolean load) {
        world.setChunkForced(pos.x, pos.z, load);
    }
}
