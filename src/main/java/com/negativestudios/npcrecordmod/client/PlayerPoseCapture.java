package com.negativestudios.npcrecordmod.client;

import com.negativestudios.npcrecordmod.NpcRecordMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client cache maintenance. Emote data is now captured as keyframes through Emotecraft's API,
 * not by sampling and rebuilding rendered model poses.
 */
@Mod.EventBusSubscriber(modid = NpcRecordMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PlayerPoseCapture {
    private PlayerPoseCapture() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) ClientActorTimelines.prune();
    }
}
