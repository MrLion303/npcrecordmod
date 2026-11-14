package com.negativestudios.npcrecordmod.client;

import com.negativestudios.npcrecordmod.emote.AnimationPayloadCodec;
import com.negativestudios.npcrecordmod.recording.RecordedEmoteEvent;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientActorTimelines {
    private static final Map<Integer, List<LoadedEmote>> TIMELINES = new ConcurrentHashMap<>();

    private ClientActorTimelines() {
    }

    public static void setTimeline(int entityId, List<RecordedEmoteEvent> events) {
        List<LoadedEmote> loaded = new ArrayList<>();
        for (RecordedEmoteEvent event : events) {
            KeyframeAnimation animation = AnimationPayloadCodec.decode(event.animationJson);
            if (animation != null) loaded.add(new LoadedEmote(event.startTick, event.stopTick, animation));
        }
        loaded.sort(Comparator.comparingInt(LoadedEmote::startTick));
        TIMELINES.put(entityId, loaded);
    }

    public static LoadedEmote getActive(int entityId, int playbackTick) {
        List<LoadedEmote> events = TIMELINES.get(entityId);
        if (events == null) return null;

        for (int i = events.size() - 1; i >= 0; i--) {
            LoadedEmote event = events.get(i);
            if (playbackTick < event.startTick) continue;

            int naturalEnd = event.animation.isInfinite
                    ? Integer.MAX_VALUE
                    : event.startTick + event.animation.stopTick;
            int actualEnd = event.stopTick >= 0 ? Math.min(naturalEnd, event.stopTick) : naturalEnd;
            if (playbackTick < actualEnd) return event;
        }
        return null;
    }

    public static void clear() {
        TIMELINES.clear();
    }

    public static void prune() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            TIMELINES.clear();
            return;
        }
        TIMELINES.keySet().removeIf(entityId -> minecraft.level.getEntity(entityId) == null);
    }

    public record LoadedEmote(int startTick, int stopTick, KeyframeAnimation animation) {
    }
}
