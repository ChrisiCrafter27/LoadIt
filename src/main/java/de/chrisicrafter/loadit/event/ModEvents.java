package de.chrisicrafter.loadit.event;

import de.chrisicrafter.loadit.LoadIt;
import de.chrisicrafter.loadit.utils.BeaconData;
import de.chrisicrafter.loadit.utils.BeaconDataObject;
import de.chrisicrafter.loadit.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = LoadIt.MOD_ID)
public class ModEvents {
    private static int tickCount;

    @SubscribeEvent
    public static void onBeaconShiftRightClick(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        LevelAccessor level = event.getLevel();
        if(level instanceof ServerLevel world && world.getBlockState(event.getPos()).getBlock() == Blocks.BEACON && player.isCrouching() && event.getHand() == InteractionHand.MAIN_HAND && !event.getLevel().isClientSide() && world.getBlockEntity(event.getPos()) instanceof BeaconBlockEntity blockEntity) {
            CompoundTag nbt = blockEntity.getUpdateTag();
            int beaconLevel = nbt.getInt("Levels");
            player.sendSystemMessage(Component.literal("Sneak Right Click on Beacon with level " + beaconLevel));
            BlockPos pos = event.getPos();
            BeaconDataObject data = LoadIt.beaconData.get(pos);
            if(data.chunkLoader) {
                if(beaconLevel == data.level) {
                    Utils.changeForceLoad(world, pos, beaconLevel - 1, beaconLevel - 1, true);
                    Utils.unloadAnimation(world, pos);
                } else {
                    Utils.changeForceLoad(world, pos, 5, 5, true);
                    Utils.overloadAnimation(world, pos);
                }
                world.getServer().getPlayerList().getPlayer(player.getUUID()).sendSystemMessage(Component.literal(ChatFormatting.RED + "Disabled chunkloading at " + Utils.toString(pos) + " (Radius : " + beaconLevel + ")"));
            } else {
                Utils.changeForceLoad(world, event.getPos(), beaconLevel - 1, 0, false);
                Utils.loadAnimation(world, pos);
                world.getServer().getPlayerList().getPlayer(player.getUUID()).sendSystemMessage(Component.literal(ChatFormatting.GREEN + "Enabled chunkloading at " + Utils.toString(pos) + " (Radius : " + beaconLevel + ")"));
            }
            data.chunkLoader = !data.chunkLoader;
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBeaconPlaced(BlockEvent.EntityPlaceEvent event) {
        LevelAccessor level = event.getLevel();
        if(level instanceof ServerLevel world && !event.getLevel().isClientSide()) {
            if(LoadIt.beaconData == null) LoadIt.beaconData = world.getDataStorage().computeIfAbsent(BeaconData.factory(), "beacon_data");
            LoadIt.beaconData.data.add(new BeaconDataObject(event.getPos(), 0, false));
            LoadIt.beaconData.setDirty();
        }
    }

    @SubscribeEvent
    public static void onBeaconDestroyed(BlockEvent.NeighborNotifyEvent event) {
        LevelAccessor level = event.getLevel();
        if(level instanceof ServerLevel world && !event.getLevel().isClientSide() && !world.getBlockState(event.getPos()).is(Blocks.BEACON)) {
            if(LoadIt.beaconData == null) LoadIt.beaconData = world.getDataStorage().computeIfAbsent(BeaconData.factory(), "beacon_data");
            if(LoadIt.beaconData.getPositions().contains(event.getPos())) {
                LoadIt.beaconData.removePosition(event.getPos());
                LoadIt.beaconData.setDirty();
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if(!event.level.isClientSide() && event.level instanceof ServerLevel world) {
            tickCount++;
            if(tickCount >= 500) {
                if(LoadIt.beaconData == null) LoadIt.beaconData = world.getDataStorage().computeIfAbsent(BeaconData.factory(), "beacon_data");

                ArrayList<BlockPos> toUnload = new ArrayList<>();
                ArrayList<BlockPos> toRemove = new ArrayList<>();
                HashMap<BlockPos, Integer> toChangeLevel = new HashMap<>();
                boolean setDirty = false;
                for(BeaconDataObject data : LoadIt.beaconData.data) {
                    BlockPos blockPos = data.blockPos;
                    int savedLevel = data.level;
                    boolean isLoader = data.chunkLoader;
                    if(world.getBlockEntity(blockPos) != null) {
                        if(world.getBlockEntity(blockPos) instanceof BeaconBlockEntity blockEntity) {
                            CompoundTag nbt = blockEntity.getUpdateTag();
                            int actualLevel = nbt.getInt("Levels");
                            if(savedLevel != actualLevel) {
                                if(isLoader) {
                                    Utils.overloadAnimation(world, blockPos);
                                    Utils.changeForceLoad(world, blockPos, savedLevel, savedLevel, true);
                                    toUnload.add(blockPos);
                                }
                                toChangeLevel.put(blockPos, actualLevel);
                                setDirty = true;
                            }
                        } else {
                            toRemove.add(blockPos);
                            setDirty = true;
                        }
                    }
                }
                for(BlockPos pos : toUnload) {
                    LoadIt.beaconData.get(pos).chunkLoader = false;
                }
                for(BlockPos pos : toRemove) {
                    LoadIt.beaconData.removePosition(pos);
                }
                for(Map.Entry<BlockPos, Integer> map : toChangeLevel.entrySet()) {
                    LoadIt.beaconData.get(map.getKey()).level = map.getValue();
                }
                if(setDirty) LoadIt.beaconData.setDirty();

                tickCount = 0;
            }
        }
    }
}
