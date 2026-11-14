package com.negativestudios.npcrecordmod.recording;

import com.negativestudios.npcrecordmod.network.NetworkHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RecordingManager {
    private static final Map<UUID, Session> ACTIVE = new ConcurrentHashMap<>();
    private static final int MAX_EMOTE_JSON_LENGTH = 262_144;

    private RecordingManager() {
    }

    public static boolean start(ServerPlayer player, String name) {
        if (!RecordingStorage.isValidName(name) || ACTIVE.containsKey(player.getUUID())) return false;
        ACTIVE.put(player.getUUID(), new Session(name, player));
        NetworkHandler.sendRecordingState(player, true);
        return true;
    }

    public static RecordingData stop(ServerPlayer player) throws IOException {
        Session session = ACTIVE.remove(player.getUUID());
        NetworkHandler.sendRecordingState(player, false);
        if (session == null || session.data.frames.isEmpty()) return null;

        closeOpenEmote(session, session.data.frames.size());
        session.data.durationTicks = session.data.frames.size();
        RecordingStorage.save(player.getServer(), session.data);
        return session.data;
    }

    public static boolean cancel(ServerPlayer player) {
        boolean removed = ACTIVE.remove(player.getUUID()) != null;
        NetworkHandler.sendRecordingState(player, false);
        return removed;
    }

    public static void acceptEmote(ServerPlayer player, long worldTick, String animationJson) {
        Session session = ACTIVE.get(player.getUUID());
        if (session == null || animationJson == null || animationJson.isBlank()
                || animationJson.length() > MAX_EMOTE_JSON_LENGTH) return;

        int relativeTick = relativeTick(session, worldTick);
        if (relativeTick < 0) return;
        closeOpenEmote(session, relativeTick);
        session.data.emotes.add(new RecordedEmoteEvent(relativeTick, animationJson));
    }

    public static void acceptEmoteStop(ServerPlayer player, long worldTick) {
        Session session = ACTIVE.get(player.getUUID());
        if (session == null) return;
        int relativeTick = relativeTick(session, worldTick);
        if (relativeTick >= 0) closeOpenEmote(session, relativeTick);
    }

    private static int relativeTick(Session session, long worldTick) {
        long relative = worldTick - session.startWorldTick;
        if (relative < -2 || relative > 1_000_000) return -1;
        return (int) Math.max(0, relative);
    }

    private static void closeOpenEmote(Session session, int stopTick) {
        if (session.data.emotes.isEmpty()) return;
        RecordedEmoteEvent active = session.data.emotes.get(session.data.emotes.size() - 1);
        if (active.stopTick < 0) active.stopTick = Math.max(active.startTick + 1, stopTick);
    }

    public static void tick(ServerLevel level) {
        for (Map.Entry<UUID, Session> entry : ACTIVE.entrySet()) {
            Session session = entry.getValue();
            if (!session.dimension.equals(level.dimension().location().toString())) continue;
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(entry.getKey());
            if (player == null || player.level() != level) continue;

            Vec3 relative = player.position().subtract(session.origin);
            RecordedFrame frame = new RecordedFrame();
            frame.tick = session.data.frames.size();
            frame.worldTick = level.getGameTime();
            frame.x = relative.x;
            frame.y = relative.y;
            frame.z = relative.z;
            frame.yaw = player.getYRot();
            frame.pitch = player.getXRot();
            frame.headYaw = player.getYHeadRot();

            Vec3 velocity = player.getDeltaMovement();
            frame.velocityX = velocity.x;
            frame.velocityY = velocity.y;
            frame.velocityZ = velocity.z;
            frame.onGround = player.onGround();
            frame.sprinting = player.isSprinting();
            frame.crouching = player.isCrouching();
            session.data.frames.add(frame);
        }
    }

    private static final class Session {
        private final RecordingData data = new RecordingData();
        private final Vec3 origin;
        private final String dimension;
        private final long startWorldTick;

        private Session(String name, ServerPlayer player) {
            this.data.name = name;
            this.data.dimension = player.level().dimension().location().toString();
            this.data.createdAt = System.currentTimeMillis();
            this.origin = player.position();
            this.dimension = this.data.dimension;
            this.startWorldTick = player.level().getGameTime();
        }
    }
}
