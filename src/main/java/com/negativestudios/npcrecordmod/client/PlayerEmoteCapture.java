package com.negativestudios.npcrecordmod.client;

import com.negativestudios.npcrecordmod.emote.AnimationPayloadCodec;
import com.negativestudios.npcrecordmod.network.EmoteRecordPacket;
import com.negativestudios.npcrecordmod.network.NetworkHandler;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import io.github.kosmx.emotes.api.events.client.ClientEmoteEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = "npcrecordmod", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class PlayerEmoteCapture {
    private static boolean registered;

    private PlayerEmoteCapture() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            if (registered) return;
            registered = true;
            ClientEmoteEvents.EMOTE_PLAY.register(PlayerEmoteCapture::onEmotePlay);
            ClientEmoteEvents.LOCAL_EMOTE_STOP.register(PlayerEmoteCapture::onEmoteStop);
        });
    }

    private static void onEmotePlay(KeyframeAnimation animation, java.util.UUID playerId) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (!ClientRecordingState.isRecording() || player == null || !player.getUUID().equals(playerId)) return;

        try {
            String payload = AnimationPayloadCodec.encode(animation);
            NetworkHandler.CHANNEL.sendToServer(
                    new EmoteRecordPacket(false, player.level().getGameTime(), payload)
            );
        } catch (RuntimeException exception) {
            // A failed serialization must not interrupt Emotecraft's own emote playback.
        }
    }

    private static void onEmoteStop() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (!ClientRecordingState.isRecording() || player == null || player.level() == null) return;
        NetworkHandler.CHANNEL.sendToServer(
                new EmoteRecordPacket(true, player.level().getGameTime(), "")
        );
    }
}
