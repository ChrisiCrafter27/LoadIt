package de.chrisicrafter.loadit.networking.packet;

import de.chrisicrafter.loadit.client.ClientDebugScreenData;
import de.chrisicrafter.loadit.data.BeaconData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class DebugScreenDataS2CPacket {
    private final CompoundTag tag;

    public DebugScreenDataS2CPacket(BeaconData data) {
        tag = data.save(new CompoundTag());
    }

    public DebugScreenDataS2CPacket(FriendlyByteBuf buf) {
        this.tag = buf.readNbt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeNbt(tag);
    }

    public void handle(CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> ClientDebugScreenData.set(BeaconData.load(tag)));
    }
}
