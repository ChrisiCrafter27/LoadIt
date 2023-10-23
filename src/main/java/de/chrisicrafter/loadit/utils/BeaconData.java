package de.chrisicrafter.loadit.utils;

import de.chrisicrafter.loadit.LoadIt;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;

public class BeaconData extends SavedData {
    public final ArrayList<BeaconDataObject> data;
    public final ArrayList<BlockPos> loadedChunks;
    public final ArrayList<BlockPos> chunksToUnload;
    public final ArrayList<BlockPos> chunksToLoad;

    public static SavedData.Factory<BeaconData> factory() {
        return new SavedData.Factory<>(BeaconData::new, BeaconData::load, DataFixTypes.LEVEL);
    }

    public BeaconData() {
        data = new ArrayList<>();
        loadedChunks = new ArrayList<>();
        chunksToUnload = new ArrayList<>();
        chunksToLoad = new ArrayList<>();
    }

    public BeaconData(ArrayList<BeaconDataObject> list, ArrayList<BlockPos> loaded, ArrayList<BlockPos> toUnload, ArrayList<BlockPos> toLoad) {
        data = list;
        loadedChunks = loaded;
        chunksToUnload = toUnload;
        chunksToLoad = toLoad;
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

        size = data.size();
        tag.putInt("size_pos", size);
        for(int i = 0; i < size; i++) {
            tag.put("beacon_pos_" + i, NbtUtils.writeBlockPos(data.get(i).blockPos));
            tag.putInt("beacon_level_" + i, data.get(i).level);
            tag.putBoolean("beacon_loader_" + i, data.get(i).chunkLoader);
        }

        size = loadedChunks.size();
        tag.putInt("size_loaded", size);
        for(int i = 0; i < size; i++) {
            tag.put("chunk_loaded_" + i, NbtUtils.writeBlockPos(loadedChunks.get(i)));
        }

        size = chunksToUnload.size();
        tag.putInt("size_unload", size);
        for(int i = 0; i < size; i++) {
            tag.put("chunk_unload_" + i, NbtUtils.writeBlockPos(chunksToUnload.get(i)));
        }

        size = chunksToLoad.size();
        tag.putInt("size_load", size);
        for(int i = 0; i < size; i++) {
            tag.put("chunk_load_" + i, NbtUtils.writeBlockPos(chunksToLoad.get(i)));
        }

        return tag;
    }

    public static BeaconData load(CompoundTag tag) {
        //load from tag
        ArrayList<BeaconDataObject> data = new ArrayList<>();
        ArrayList<BlockPos> loaded = new ArrayList<>();
        ArrayList<BlockPos> toUnload = new ArrayList<>();
        ArrayList<BlockPos> toLoad = new ArrayList<>();
        int size;

        size = tag.getInt("size_pos");
        for(int i = 0; i < size; i++) {
            data.add(new BeaconDataObject(NbtUtils.readBlockPos((CompoundTag) tag.get("beacon_pos_" + i)), tag.getInt("beacon_level_" + i), tag.getBoolean("beacon_loader_" + i)));
        }

        size = tag.getInt("size_loaded");
        for(int i = 0; i < size; i++) {
            loaded.add(NbtUtils.readBlockPos((CompoundTag) tag.get("chunk_loaded_" + i)));
        }

        size = tag.getInt("size_unload");
        for(int i = 0; i < size; i++) {
            toUnload.add(NbtUtils.readBlockPos((CompoundTag) tag.get("chunk_unload_" + i)));
        }

        size = tag.getInt("size_load");
        for(int i = 0; i < size; i++) {
            toLoad.add(NbtUtils.readBlockPos((CompoundTag) tag.get("chunk_load_" + i)));
        }

        return new BeaconData(data, loaded, toUnload, toLoad);
    }

    public ArrayList<BlockPos> getPositions() {
        ArrayList<BlockPos> list = new ArrayList<>();
        for(BeaconDataObject beaconData: data) {
            list.add(beaconData.blockPos);
        }
        return list;
    }

    public BeaconDataObject get(BlockPos blockPos) {
        BeaconDataObject value = null;
        for(BeaconDataObject beaconData: data) {
            if(beaconData.blockPos.equals(blockPos)) {
                value = beaconData;
                break;
            }
        }
        return value;
    }

    public void removeAndUnload(ServerLevel world, BlockPos blockPos) {
        BeaconDataObject beaconData = get(blockPos);
        if(beaconData.chunkLoader) {
            Utils.forceLoad(world, blockPos, beaconData.level, beaconData.level, true);
            Utils.overloadAnimation(world, blockPos);
        }
        data.removeIf(beacon -> beacon.blockPos.equals(blockPos));
        Utils.updateLoadedChunks(world, false);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int size = data.size();
        builder.append("Count: ");
        builder.append(size);
        for(int i = 0; i < size; i++) {
            builder.append("\n");
            builder.append("beacon_pos_");
            builder.append(i);
            builder.append(" ");
            builder.append(data.get(i).blockPos.toString());
            builder.append(" [Level: ");
            builder.append(data.get(i).level);
            builder.append("]");
        }
        return builder.toString();
    }
}
