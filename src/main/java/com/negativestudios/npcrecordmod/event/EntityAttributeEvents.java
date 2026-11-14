package com.negativestudios.npcrecordmod.event;

import com.negativestudios.npcrecordmod.NpcRecordMod;
import com.negativestudios.npcrecordmod.registry.ModEntities;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NpcRecordMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class EntityAttributeEvents {
    private EntityAttributeEvents() {
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        AttributeSupplier.Builder attributes = Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
        event.put(ModEntities.RECORDED_ACTOR.get(), attributes.build());
    }
}
