package de.chrisicrafter.loadit.networking;

import de.chrisicrafter.loadit.LoadIt;
import de.chrisicrafter.loadit.networking.packet.DebugScreenDataS2CPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.*;

public class ModMessages {
    private static SimpleChannel INSTANCE;

    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = ChannelBuilder.named(new ResourceLocation(LoadIt.MOD_ID, "messages"))
                .networkProtocolVersion(1)
                .clientAcceptedVersions((status, version) -> true)
                .serverAcceptedVersions((status, version) -> true)
                .simpleChannel();
        INSTANCE = net;

        net.messageBuilder(DebugScreenDataS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(DebugScreenDataS2CPacket::new)
                .encoder(DebugScreenDataS2CPacket::toBytes)
                .consumerMainThread(DebugScreenDataS2CPacket::handle)
                .add();
    }

    public static <MSG> void sendToPlayer(MSG message, ResourceKey<Level> dimension) {
        INSTANCE.send(message, PacketDistributor.DIMENSION.with(dimension));
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(message, PacketDistributor.PLAYER.with(player));
    }
}
