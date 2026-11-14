package com.negativestudios.npcrecordmod.emote;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.data.gson.AnimationJson;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Converts Emotecraft's own keyframe format to a self-contained JSON payload.
 * The payload travels with a recording, so viewers do not need the original emote file.
 */
public final class AnimationPayloadCodec {
    private static final Gson ENCODER = new GsonBuilder()
            .registerTypeAdapter(KeyframeAnimation.class, new AnimationJson())
            .create();

    private static final Type ANIMATION_LIST = AnimationJson.getListedTypeToken();
    private static final Gson DECODER = new GsonBuilder()
            .registerTypeAdapter(ANIMATION_LIST, new AnimationJson())
            .create();

    private AnimationPayloadCodec() {
    }

    public static String encode(KeyframeAnimation animation) {
        JsonObject json = JsonParser.parseString(ENCODER.toJson(animation, KeyframeAnimation.class)).getAsJsonObject();
        // Emotecraft's serializer normally includes the display name. Keep a valid name even for custom animations.
        if (!json.has("name")) json.addProperty("name", "Recorded animation");
        if (!json.has("version")) json.addProperty("version", 3);
        return json.toString();
    }

    public static KeyframeAnimation decode(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            List<KeyframeAnimation> animations = DECODER.fromJson(json, ANIMATION_LIST);
            return animations == null || animations.isEmpty() ? null : animations.get(0);
        } catch (RuntimeException exception) {
            return null;
        }
    }
}
