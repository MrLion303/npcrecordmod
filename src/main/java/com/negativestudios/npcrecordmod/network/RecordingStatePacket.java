package com.negativestudios.npcrecordmod.network;

import com.negativestudios.npcrecordmod.client.ClientRecordingState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RecordingStatePacket {
    private final boolean recording;

    public RecordingStatePacket(boolean recording) {
        this.recording = recording;
    }

    public static void encode(RecordingStatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.recording);
    }

    public static RecordingStatePacket decode(FriendlyByteBuf buffer) {
        return new RecordingStatePacket(buffer.readBoolean());
    }

    public static void handle(RecordingStatePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientRecordingState.setRecording(packet.recording))
        );
        context.setPacketHandled(true);
    }
}
