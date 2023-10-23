package de.chrisicrafter.loadit.event;

import de.chrisicrafter.loadit.LoadIt;
import de.chrisicrafter.loadit.networking.ModMessages;
import de.chrisicrafter.loadit.networking.packet.DebugScreenDataS2CPacket;
import de.chrisicrafter.loadit.utils.BeaconData;
import de.chrisicrafter.loadit.utils.BeaconDataObject;
import de.chrisicrafter.loadit.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
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
    public static void onPlayerJoined(EntityJoinLevelEvent event) {
        if(!event.getLevel().isClientSide() && event.getEntity() instanceof ServerPlayer serverPlayer) {
            ModMessages.sendToPlayer(new DebugScreenDataS2CPacket(LoadIt.beaconData), serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onBeaconShiftRightClick(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        LevelAccessor level = event.getLevel();
        if(level instanceof ServerLevel world && world.getBlockState(event.getPos()).getBlock() == Blocks.BEACON && player.isCrouching() && event.getHand() == InteractionHand.MAIN_HAND && !event.getLevel().isClientSide() && world.getBlockEntity(event.getPos()) instanceof BeaconBlockEntity blockEntity) {
            CompoundTag nbt = blockEntity.getUpdateTag();
            int beaconLevel = nbt.getInt("Levels");
            BlockPos pos = event.getPos();
            BeaconDataObject data = LoadIt.beaconData.get(pos);
            if(data.chunkLoader) {
                if(beaconLevel == data.level) {
                    Utils.forceLoad(world, pos, beaconLevel, beaconLevel, true);
                    Utils.unloadAnimation(world, pos);
                } else {
                    Utils.forceLoad(world, pos, 5, 5, true);
                    Utils.overloadAnimation(world, pos);
                }
                world.getServer().getPlayerList().getPlayer(player.getUUID()).sendSystemMessage(Component.literal(ChatFormatting.RED + "Disabled chunkloading at chunk " + Utils.toString(pos) + " (Radius : " + beaconLevel + ")"));
                data.chunkLoader = false;
            } else if(!LoadIt.beaconData.loadedChunks.contains(new BlockPos(pos.getX() / 16, 0, pos.getZ() / 16))) {
                Utils.forceLoad(world, event.getPos(), beaconLevel, 0, false);
                Utils.loadAnimation(world, pos);
                world.getServer().getPlayerList().getPlayer(player.getUUID()).sendSystemMessage(Component.literal(ChatFormatting.GREEN + "Enabled chunkloading at chunk " + Utils.toString(pos) + " (radius: " + beaconLevel + ")"));
                data.chunkLoader = true;
            } else {
                world.getServer().getPlayerList().getPlayer(player.getUUID()).sendSystemMessage(Component.literal(ChatFormatting.YELLOW + "There is already an active chunkloader in this chunk"));
            }
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
                LoadIt.beaconData.removeAndUnload(world, event.getPos());
                LoadIt.beaconData.setDirty();
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if(!event.level.isClientSide() && event.level instanceof ServerLevel world) {
            tickCount++;
            if(tickCount >= 20) {
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
                                    Utils.forceLoad(world, blockPos, Math.max(savedLevel, actualLevel), Math.max(savedLevel, actualLevel), true);
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
                    LoadIt.beaconData.removeAndUnload(world, pos);
                }
                for(Map.Entry<BlockPos, Integer> map : toChangeLevel.entrySet()) {
                    LoadIt.beaconData.get(map.getKey()).level = map.getValue();
                }
                if(setDirty) LoadIt.beaconData.setDirty();

                if(LoadIt.sendData) ModMessages.sendToPlayer(new DebugScreenDataS2CPacket(LoadIt.beaconData));

                tickCount = 0;
            }
        }
    }
}
