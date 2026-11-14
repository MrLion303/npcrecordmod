package com.negativestudios.npcrecordmod.network;

import com.negativestudios.npcrecordmod.recording.RecordingManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EmoteRecordPacket {
    private final boolean stop;
    private final long worldTick;
    private final String animationJson;

    public EmoteRecordPacket(boolean stop, long worldTick, String animationJson) {
        this.stop = stop;
        this.worldTick = worldTick;
        this.animationJson = animationJson == null ? "" : animationJson;
    }

    public static void encode(EmoteRecordPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.stop);
        buffer.writeLong(packet.worldTick);
        buffer.writeUtf(packet.animationJson, 262_144);
    }

    public static EmoteRecordPacket decode(FriendlyByteBuf buffer) {
        return new EmoteRecordPacket(buffer.readBoolean(), buffer.readLong(), buffer.readUtf(262_144));
    }

    public static void handle(EmoteRecordPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getSender() == null) return;
            if (packet.stop) {
                RecordingManager.acceptEmoteStop(context.getSender(), packet.worldTick);
            } else {
                RecordingManager.acceptEmote(context.getSender(), packet.worldTick, packet.animationJson);
            }
        });
        context.setPacketHandled(true);
    }
}
