package com.negativestudios.npcrecordmod.network;

import com.negativestudios.npcrecordmod.client.ClientActorTimelines;
import com.negativestudios.npcrecordmod.recording.RecordedEmoteEvent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ActorTimelinePacket {
    private static final int MAX_EVENTS = 2048;
    private final int entityId;
    private final List<RecordedEmoteEvent> events;

    public ActorTimelinePacket(int entityId, List<RecordedEmoteEvent> events) {
        this.entityId = entityId;
        this.events = events == null ? List.of() : events;
    }

    public static void encode(ActorTimelinePacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.entityId);
        int count = Math.min(packet.events.size(), MAX_EVENTS);
        buffer.writeVarInt(count);
        for (int i = 0; i < count; i++) {
            RecordedEmoteEvent event = packet.events.get(i);
            buffer.writeVarInt(event.startTick);
            buffer.writeVarInt(event.stopTick);
            buffer.writeUtf(event.animationJson == null ? "" : event.animationJson, 262_144);
        }
    }

    public static ActorTimelinePacket decode(FriendlyByteBuf buffer) {
        int entityId = buffer.readVarInt();
        int count = Math.max(0, Math.min(buffer.readVarInt(), MAX_EVENTS));
        List<RecordedEmoteEvent> events = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            RecordedEmoteEvent event = new RecordedEmoteEvent();
            event.startTick = buffer.readVarInt();
            event.stopTick = buffer.readVarInt();
            event.animationJson = buffer.readUtf(262_144);
            events.add(event);
        }
        return new ActorTimelinePacket(entityId, events);
    }

    public static void handle(ActorTimelinePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () ->
                        () -> ClientActorTimelines.setTimeline(packet.entityId, packet.events))
        );
        context.setPacketHandled(true);
    }
}
