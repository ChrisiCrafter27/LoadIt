package de.chrisicrafter.loadit.event;

import de.chrisicrafter.loadit.LoadIt;
import de.chrisicrafter.loadit.networking.ModMessages;
import de.chrisicrafter.loadit.networking.packet.DebugScreenDataS2CPacket;
import de.chrisicrafter.loadit.utils.BeaconData;
import de.chrisicrafter.loadit.utils.Utils;
import net.minecraft.data.worldgen.DimensionTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.swing.plaf.IconUIResource;

@Mod.EventBusSubscriber(modid = LoadIt.MOD_ID)
public class ModEvents {

    @SubscribeEvent
    public static void onPlayerJoined(EntityJoinLevelEvent event) {
        if(!event.getLevel().isClientSide() && event.getLevel() instanceof ServerLevel world && event.getEntity() instanceof ServerPlayer serverPlayer) {
            ModMessages.sendToPlayer(new DebugScreenDataS2CPacket(LoadIt.getBeaconData(world)), serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onBeaconShiftRightClick(PlayerInteractEvent.RightClickBlock event) {
        if(event.getLevel() instanceof ServerLevel world && event.getEntity() instanceof ServerPlayer player && world.getBlockState(event.getPos()).getBlock() == Blocks.BEACON && player.isCrouching() && event.getHand() == InteractionHand.MAIN_HAND && !event.getLevel().isClientSide()) {
            LoadIt.getBeaconData(world).beaconShiftUse(world, player, event.getPos());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if(!event.level.isClientSide() && event.level instanceof ServerLevel world && event.level.dimensionTypeId().equals(BuiltinDimensionTypes.OVERWORLD)) {
            BeaconData.update(world);
        }
    }
}
