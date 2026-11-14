package com.negativestudios.npcrecordmod.registry;

import com.negativestudios.npcrecordmod.NpcRecordMod;
import com.negativestudios.npcrecordmod.entity.RecordedActorEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, NpcRecordMod.MOD_ID);

    public static final RegistryObject<EntityType<RecordedActorEntity>> RECORDED_ACTOR =
            ENTITY_TYPES.register("recorded_actor", () ->
                    EntityType.Builder.<RecordedActorEntity>of(RecordedActorEntity::new, MobCategory.MISC)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build(NpcRecordMod.MOD_ID + ":recorded_actor")
            );

    private ModEntities() {
    }
}
