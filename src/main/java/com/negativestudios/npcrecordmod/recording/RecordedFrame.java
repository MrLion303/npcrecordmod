package com.negativestudios.npcrecordmod.recording;

public class RecordedFrame {
    public int tick;
    public long worldTick;
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;
    public float headYaw;
    public double velocityX;
    public double velocityY;
    public double velocityZ;
    public boolean onGround;
    public boolean sprinting;
    public boolean crouching;
    public ActorPose pose;

    public RecordedFrame() {
    }
}
