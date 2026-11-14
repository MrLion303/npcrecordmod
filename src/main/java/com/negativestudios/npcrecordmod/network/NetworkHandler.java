package com.negativestudios.npcrecordmod.network;

import com.negativestudios.npcrecordmod.NpcRecordMod;
import com.negativestudios.npcrecordmod.recording.RecordedEmoteEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.List;

public final class NetworkHandler {
    private static final String PROTOCOL = "2";
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(NpcRecordMod.MOD_ID, "main"))
            .networkProtocolVersion(() -> PROTOCOL)
            .clientAcceptedVersions(PROTOCOL::equals)
            .serverAcceptedVersions(PROTOCOL::equals)
            .simpleChannel();

    private static boolean registered;

    private NetworkHandler() {
    }

    public static void register() {
        if (registered) return;
        registered = true;

        CHANNEL.messageBuilder(RecordingStatePacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(RecordingStatePacket::encode)
                .decoder(RecordingStatePacket::decode)
                .consumerMainThread(RecordingStatePacket::handle)
                .add();

        CHANNEL.messageBuilder(EmoteRecordPacket.class, 1, NetworkDirection.PLAY_TO_SERVER)
                .encoder(EmoteRecordPacket::encode)
                .decoder(EmoteRecordPacket::decode)
                .consumerMainThread(EmoteRecordPacket::handle)
                .add();

        CHANNEL.messageBuilder(ActorTimelinePacket.class, 2, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ActorTimelinePacket::encode)
                .decoder(ActorTimelinePacket::decode)
                .consumerMainThread(ActorTimelinePacket::handle)
                .add();
    }

    public static void sendRecordingState(ServerPlayer player, boolean recording) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new RecordingStatePacket(recording));
    }

    public static void sendActorTimeline(ServerPlayer player, int entityId, List<RecordedEmoteEvent> events) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ActorTimelinePacket(entityId, events));
    }
}
