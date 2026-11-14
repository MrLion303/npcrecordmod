package com.negativestudios.npcrecordmod.client;

import com.negativestudios.npcrecordmod.entity.RecordedActorEntity;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.core.util.MathHelper;
import dev.kosmx.playerAnim.core.util.Vec3f;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;

public class RecordedActorModel extends PlayerModel<RecordedActorEntity> {
    public RecordedActorModel(ModelPart root) {
        super(root, false);
    }

    @Override
    public void setupAnim(RecordedActorEntity actor, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(actor, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        ClientActorTimelines.LoadedEmote event =
                ClientActorTimelines.getActive(actor.getId(), actor.getPlaybackTick());
        if (event == null) return;

        int animationTick = Math.max(0, actor.getPlaybackTick() - event.startTick());
        KeyframeAnimationPlayer player = new KeyframeAnimationPlayer(event.animation(), animationTick);
        float partialTick = ageInTicks - (float) Math.floor(ageInTicks);
        player.setupAnim(partialTick);

        apply(player, "head", head, partialTick);
        apply(player, "torso", body, partialTick);
        apply(player, "rightArm", rightArm, partialTick);
        apply(player, "leftArm", leftArm, partialTick);
        apply(player, "rightLeg", rightLeg, partialTick);
        apply(player, "leftLeg", leftLeg, partialTick);

        // Keep the vanilla skin overlays aligned with the animated base parts.
        hat.copyFrom(head);
        jacket.copyFrom(body);
        rightSleeve.copyFrom(rightArm);
        leftSleeve.copyFrom(leftArm);
        rightPants.copyFrom(rightLeg);
        leftPants.copyFrom(leftLeg);
    }

    private static void apply(KeyframeAnimationPlayer animation, String modelName, ModelPart part, float partialTick) {
        Vec3f position = animation.get3DTransform(modelName, TransformType.POSITION, partialTick,
                new Vec3f(part.x, part.y, part.z));
        part.setPos(position.getX(), position.getY(), position.getZ());

        Vec3f rotation = animation.get3DTransform(modelName, TransformType.ROTATION, partialTick,
                new Vec3f(
                        MathHelper.clampToRadian(part.xRot),
                        MathHelper.clampToRadian(part.yRot),
                        MathHelper.clampToRadian(part.zRot)
                ));
        part.setRotation(rotation.getX(), rotation.getY(), rotation.getZ());
    }
}
