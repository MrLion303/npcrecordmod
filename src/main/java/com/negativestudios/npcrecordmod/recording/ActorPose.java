package com.negativestudios.npcrecordmod.recording;

import com.google.gson.Gson;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Pose capturada del modelo del jugador. Se guarda como datos para que el espectador no
 * necesite los archivos de emotes que generaron esa pose.
 */
public class ActorPose {
    private static final Gson GSON = new Gson();

    public PartPose head;
    public PartPose body;
    public PartPose rightArm;
    public PartPose leftArm;
    public PartPose rightLeg;
    public PartPose leftLeg;

    public ActorPose() {
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    public static ActorPose fromJson(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return GSON.fromJson(json, ActorPose.class);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    public void write(FriendlyByteBuf buffer) {
        writePart(buffer, head);
        writePart(buffer, body);
        writePart(buffer, rightArm);
        writePart(buffer, leftArm);
        writePart(buffer, rightLeg);
        writePart(buffer, leftLeg);
    }

    public static ActorPose read(FriendlyByteBuf buffer) {
        ActorPose pose = new ActorPose();
        pose.head = readPart(buffer);
        pose.body = readPart(buffer);
        pose.rightArm = readPart(buffer);
        pose.leftArm = readPart(buffer);
        pose.rightLeg = readPart(buffer);
        pose.leftLeg = readPart(buffer);
        return pose;
    }

    private static void writePart(FriendlyByteBuf buffer, PartPose part) {
        buffer.writeBoolean(part != null);
        if (part == null) return;
        buffer.writeFloat(part.x);
        buffer.writeFloat(part.y);
        buffer.writeFloat(part.z);
        buffer.writeFloat(part.xRot);
        buffer.writeFloat(part.yRot);
        buffer.writeFloat(part.zRot);
    }

    private static PartPose readPart(FriendlyByteBuf buffer) {
        if (!buffer.readBoolean()) return null;
        PartPose part = new PartPose();
        part.x = buffer.readFloat();
        part.y = buffer.readFloat();
        part.z = buffer.readFloat();
        part.xRot = buffer.readFloat();
        part.yRot = buffer.readFloat();
        part.zRot = buffer.readFloat();
        return part;
    }
}
