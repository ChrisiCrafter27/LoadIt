package de.chrisicrafter.loadit.data;

import de.chrisicrafter.loadit.LoadIt;
import de.chrisicrafter.loadit.networking.ModMessages;
import de.chrisicrafter.loadit.networking.packet.DebugScreenDataS2CPacket;
import de.chrisicrafter.loadit.utils.Utils;
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
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

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
    public @NotNull CompoundTag save(CompoundTag tag) {
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

    public void beaconShiftUse(ServerLevel level, ServerPlayer player, BlockPos blockPos) {
        ChunkPos chunkPos = Utils.toChunkPos(blockPos);
        if(Utils.chunkLoader(LoadIt.getBeaconData(level), blockPos)) {
            loaderPositions.remove(blockPos);
            level.playSound(null, blockPos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0F, 1.0F);
            player.sendSystemMessage(Component.literal(ChatFormatting.RED + "Disabled chunk loading at chunk " + Utils.toString(chunkPos) + " in " + level.dimension().registry()), true);
        } else if(!Utils.chunkLoader(LoadIt.getBeaconData(level), chunkPos)) {
            loaderPositions.add(blockPos);
            level.playSound(null, blockPos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.0F);
            player.sendSystemMessage(Component.literal(ChatFormatting.GREEN + "Enabled chunk loading at chunk " + Utils.toString(chunkPos) + " in " + level.dimension().registry()), true);
        } else {
            level.playSound(null, blockPos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1.0F, 1.0F);
            player.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + "There is already an active chunk loader in this chunk"), true);
        }
    }

    public void update(ServerLevel level) {
        ArrayList<ChunkPos> chunksToLoad = new ArrayList<>();
        loaderPositions.removeIf(pos -> !level.getBlockState(pos).is(Blocks.BEACON));
        for(BlockPos blockPos : loaderPositions) {
            int beaconLevel = Utils.beaconLevel(level, blockPos);
            ChunkPos chunkPos = Utils.toChunkPos(blockPos);
            for(ChunkPos pos : getChunks(chunkPos, beaconLevel)) {
                if(!chunksToLoad.contains(pos)) chunksToLoad.add(pos);
            }
        }
        if(!loadedChunks.containsAll(chunksToLoad) || !chunksToLoad.containsAll(loadedChunks)) {
            for(ChunkPos pos : loadedChunks) {
                if(!chunksToLoad.contains(pos)) forceLoad(level, pos, false);
            }
            for(ChunkPos pos : chunksToLoad) {
                if(!loadedChunks.contains(pos)) forceLoad(level, pos, true);
            }
            loadedChunks.removeIf((ignored) -> true);
            loadedChunks.addAll(chunksToLoad);
            ModMessages.sendToPlayer(new DebugScreenDataS2CPacket(LoadIt.getBeaconData(level)), level.dimension());
            setDirty();
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

    private static void forceLoad(ServerLevel level, ChunkPos pos, boolean load) {
        level.setChunkForced(pos.x, pos.z, load);
    }
}
