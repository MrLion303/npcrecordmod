package com.negativestudios.npcrecordmod.entity;

import com.negativestudios.npcrecordmod.NpcRecordMod;
import com.negativestudios.npcrecordmod.recording.RecordedEmoteEvent;
import com.negativestudios.npcrecordmod.recording.RecordedFrame;
import com.negativestudios.npcrecordmod.recording.RecordingData;
import com.negativestudios.npcrecordmod.recording.RecordingStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.io.IOException;
import java.util.List;

public class RecordedActorEntity extends PathfinderMob {
    private static final EntityDataAccessor<String> ACTOR_NAME =
            SynchedEntityData.defineId(RecordedActorEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> SKIN_URL =
            SynchedEntityData.defineId(RecordedActorEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Long> PLAYBACK_START_TICK =
            SynchedEntityData.defineId(RecordedActorEntity.class, EntityDataSerializers.LONG);

    private String recordingName = "";
    private double originX;
    private double originY;
    private double originZ;
    private float yawOffset;
    private transient RecordingData playbackData;

    public RecordedActorEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        setNoAi(true);
        setNoGravity(true);
        setNoPhysics(true);
        setPersistenceRequired();
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ACTOR_NAME, "Actor");
        entityData.define(SKIN_URL, "");
        entityData.define(PLAYBACK_START_TICK, 0L);
    }

    public void configurePlayback(String recordingName, String actorName, RecordingData data,
                                  long startTick, double x, double y, double z, float yawOffset) {
        this.recordingName = recordingName;
        this.playbackData = data;
        this.originX = x;
        this.originY = y;
        this.originZ = z;
        this.yawOffset = yawOffset;
        entityData.set(PLAYBACK_START_TICK, startTick);
        setActorName(actorName);
        setPos(x, y, z);
    }

    public String getActorName() {
        return entityData.get(ACTOR_NAME);
    }

    public void setActorName(String name) {
        entityData.set(ACTOR_NAME, name);
        setCustomName(net.minecraft.network.chat.Component.literal(name));
        setCustomNameVisible(false);
    }

    public String getSkinUrl() {
        return entityData.get(SKIN_URL);
    }

    public void setSkinUrl(String url) {
        entityData.set(SKIN_URL, url == null ? "" : url);
    }

    public int getPlaybackTick() {
        return (int) Math.max(0L, level().getGameTime() - entityData.get(PLAYBACK_START_TICK));
    }

    public List<RecordedEmoteEvent> getRecordedEmotes() {
        return playbackData == null || playbackData.emotes == null ? List.of() : playbackData.emotes;
    }

    public String getRecordingName() {
        return recordingName;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;

        if (playbackData == null && !recordingName.isEmpty() && level() instanceof ServerLevel serverLevel) {
            try {
                playbackData = RecordingStorage.load(serverLevel.getServer(), recordingName);
            } catch (IOException exception) {
                NpcRecordMod.LOGGER.error("No se pudo cargar la grabación {} para el actor {}", recordingName, getActorName(), exception);
                recordingName = "";
            }
        }

        if (playbackData == null || playbackData.frames.isEmpty()) return;

        int elapsed = getPlaybackTick();
        int frameIndex = Math.min(elapsed, playbackData.frames.size() - 1);
        RecordedFrame frame = playbackData.frames.get(frameIndex);

        setPos(originX + frame.x, originY + frame.y, originZ + frame.z);
        float yaw = frame.yaw + yawOffset;
        setYRot(yaw);
        setXRot(frame.pitch);
        setYHeadRot(frame.headYaw + yawOffset);
        setYBodyRot(yaw);
        setSprinting(frame.sprinting);
        setShiftKeyDown(frame.crouching);
        setDeltaMovement(Vec3.ZERO);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("ActorName", getActorName());
        tag.putString("SkinUrl", getSkinUrl());
        tag.putString("RecordingName", recordingName);
        tag.putLong("PlaybackStartTick", entityData.get(PLAYBACK_START_TICK));
        tag.putDouble("OriginX", originX);
        tag.putDouble("OriginY", originY);
        tag.putDouble("OriginZ", originZ);
        tag.putFloat("YawOffset", yawOffset);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setActorName(tag.getString("ActorName"));
        setSkinUrl(tag.getString("SkinUrl"));
        recordingName = tag.getString("RecordingName");
        entityData.set(PLAYBACK_START_TICK, tag.getLong("PlaybackStartTick"));
        originX = tag.getDouble("OriginX");
        originY = tag.getDouble("OriginY");
        originZ = tag.getDouble("OriginZ");
        yawOffset = tag.getFloat("YawOffset");
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
