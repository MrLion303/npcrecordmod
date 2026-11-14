package com.negativestudios.npcrecordmod.recording;

/**
 * A portable copy of one Emotecraft animation and its position in the recording timeline.
 * The JSON is the keyframe data itself, not a resource identifier or local emote filename.
 */
public class RecordedEmoteEvent {
    public int startTick;
    /** -1 means the animation uses its own natural stop time. */
    public int stopTick = -1;
    public String animationJson;

    public RecordedEmoteEvent() {
    }

    public RecordedEmoteEvent(int startTick, String animationJson) {
        this.startTick = startTick;
        this.animationJson = animationJson;
    }
}
