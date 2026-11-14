package com.negativestudios.npcrecordmod.client;

public final class ClientRecordingState {
    private static volatile boolean recording;

    private ClientRecordingState() {
    }

    public static boolean isRecording() {
        return recording;
    }

    public static void setRecording(boolean active) {
        recording = active;
    }
}
