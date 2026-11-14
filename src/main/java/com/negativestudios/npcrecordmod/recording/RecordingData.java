package com.negativestudios.npcrecordmod.recording;

import java.util.ArrayList;
import java.util.List;

public class RecordingData {
    public int formatVersion = 2;
    public String name;
    public String dimension;
    public long createdAt;
    public int durationTicks;
    public List<RecordedFrame> frames = new ArrayList<>();
    public List<RecordedEmoteEvent> emotes = new ArrayList<>();

    public RecordingData() {
    }
}
