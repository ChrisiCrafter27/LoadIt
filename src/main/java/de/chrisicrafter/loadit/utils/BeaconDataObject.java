package de.chrisicrafter.loadit.utils;

import net.minecraft.core.BlockPos;

public class BeaconDataObject {
    public final BlockPos blockPos;
    public int level;
    public boolean chunkLoader;

    public BeaconDataObject(BlockPos blockPos, int level, boolean chunkLoader) {
        this.blockPos = blockPos;
        this.level = level;
        this.chunkLoader = chunkLoader;
    }
}
