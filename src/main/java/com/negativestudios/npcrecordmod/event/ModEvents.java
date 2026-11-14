package com.negativestudios.npcrecordmod.event;

import com.negativestudios.npcrecordmod.NpcRecordMod;
import com.negativestudios.npcrecordmod.recording.RecordingManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NpcRecordMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ModEvents {
    private ModEvents() {
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide) return;
        if (event.level instanceof ServerLevel serverLevel) {
            RecordingManager.tick(serverLevel);
        }
    }
}
