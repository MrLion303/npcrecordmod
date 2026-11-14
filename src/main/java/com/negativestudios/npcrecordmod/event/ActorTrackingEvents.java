package com.negativestudios.npcrecordmod.event;

import com.negativestudios.npcrecordmod.NpcRecordMod;
import com.negativestudios.npcrecordmod.entity.RecordedActorEntity;
import com.negativestudios.npcrecordmod.network.NetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NpcRecordMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ActorTrackingEvents {
    private ActorTrackingEvents() {
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getTarget() instanceof RecordedActorEntity actor)) return;
        NetworkHandler.sendActorTimeline(player, actor.getId(), actor.getRecordedEmotes());
    }
}
