package de.chrisicrafter.loadit.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;

public class BeaconData extends SavedData {
    public final ArrayList<BeaconDataObject> data;

    public static SavedData.Factory<BeaconData> factory() {
        return new SavedData.Factory<>(BeaconData::new, BeaconData::load, DataFixTypes.LEVEL);
    }

    public BeaconData() {
        data = new ArrayList<>();
    }

    public BeaconData(ArrayList<BeaconDataObject> list) {
        data = list;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        //save to tag
        int size = data.size();
        tag.putInt("size", size);
        for(int i = 0; i < size; i++) {
            tag.put("beacon_pos_" + i, NbtUtils.writeBlockPos(data.get(i).blockPos));
            tag.putInt("beacon_level_" + i, data.get(i).level);
            tag.putBoolean("beacon_loader_" + i, data.get(i).chunkLoader);
        }
        return tag;
    }

    public static BeaconData load(CompoundTag tag) {
        ArrayList<BeaconDataObject> list = new ArrayList<>();
        //load from tag
        int size = tag.getInt("size");
        for(int i = 0; i < size; i++) {
            list.add(new BeaconDataObject(NbtUtils.readBlockPos((CompoundTag) tag.get("beacon_pos_" + i)), tag.getInt("beacon_level_" + i), tag.getBoolean("beacon_loader_" + i)));
        }
        return new BeaconData(list);
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

    public void removePosition(BlockPos blockPos) {
        data.removeIf(beaconData -> beaconData.blockPos.equals(blockPos));
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
