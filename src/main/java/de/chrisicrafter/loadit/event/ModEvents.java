package de.chrisicrafter.loadit.event;

import de.chrisicrafter.loadit.LoadIt;
import de.chrisicrafter.loadit.networking.ModMessages;
import de.chrisicrafter.loadit.networking.packet.DebugScreenDataS2CPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LoadIt.MOD_ID)
public class ModEvents {

    @SubscribeEvent
    public static void onPlayerJoinedLevel(EntityJoinLevelEvent event) {
        if(!event.getLevel().isClientSide() && event.getLevel() instanceof ServerLevel level && event.getEntity() instanceof ServerPlayer serverPlayer) {
            ModMessages.sendToPlayer(new DebugScreenDataS2CPacket(LoadIt.getBeaconData(level)), serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onBeaconShiftRightClick(PlayerInteractEvent.RightClickBlock event) {
        if(event.getLevel() instanceof ServerLevel level && event.getEntity() instanceof ServerPlayer player && level.getBlockState(event.getPos()).getBlock() == Blocks.BEACON && player.isCrouching() && event.getHand() == InteractionHand.MAIN_HAND && !event.getLevel().isClientSide()) {
            LoadIt.getBeaconData(level).beaconShiftUse(level, player, event.getPos());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if(!event.level.isClientSide() && event.level instanceof ServerLevel level) {
            LoadIt.getBeaconData(level).update(level);
        }
    }
}
