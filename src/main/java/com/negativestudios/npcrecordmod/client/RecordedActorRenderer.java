package com.negativestudios.npcrecordmod.client;

import com.negativestudios.npcrecordmod.entity.RecordedActorEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RecordedActorRenderer extends MobRenderer<RecordedActorEntity, RecordedActorModel> {
    public RecordedActorRenderer(EntityRendererProvider.Context context) {
        super(context, new RecordedActorModel(context.bakeLayer(ModelLayers.PLAYER)), 0.45F);
    }

    @Override
    public ResourceLocation getTextureLocation(RecordedActorEntity actor) {
        return SkinTextureCache.getTexture(actor);
    }
}
