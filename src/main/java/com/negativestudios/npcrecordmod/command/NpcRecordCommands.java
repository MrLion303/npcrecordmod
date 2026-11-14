package com.negativestudios.npcrecordmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.negativestudios.npcrecordmod.entity.RecordedActorEntity;
import com.negativestudios.npcrecordmod.registry.ModEntities;
import com.negativestudios.npcrecordmod.recording.RecordingData;
import com.negativestudios.npcrecordmod.recording.RecordingManager;
import com.negativestudios.npcrecordmod.recording.RecordingStorage;
import com.negativestudios.npcrecordmod.skin.SkinUrls;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = "npcrecordmod", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class NpcRecordCommands {
    private NpcRecordCommands() {
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("npcrecord")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("record")
                        .then(Commands.literal("start")
                                .then(Commands.argument("nombre", StringArgumentType.word())
                                        .executes(context -> startRecording(context.getSource(), StringArgumentType.getString(context, "nombre")))))
                        .then(Commands.literal("stop").executes(context -> stopRecording(context.getSource(), false)))
                        .then(Commands.literal("cancel").executes(context -> stopRecording(context.getSource(), true))))
                .then(Commands.literal("play")
                        .then(Commands.argument("grabacion", StringArgumentType.word())
                                .executes(context -> play(context.getSource(),
                                        StringArgumentType.getString(context, "grabacion"),
                                        StringArgumentType.getString(context, "grabacion")))
                                .then(Commands.argument("actor", StringArgumentType.word())
                                        .executes(context -> play(context.getSource(),
                                                StringArgumentType.getString(context, "grabacion"),
                                                StringArgumentType.getString(context, "actor"))))))
                .then(Commands.literal("skin")
                        .then(Commands.literal("set")
                                .then(Commands.argument("actor", StringArgumentType.word())
                                        .then(Commands.argument("url", StringArgumentType.string())
                                                .executes(context -> setSkin(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "actor"),
                                                        StringArgumentType.getString(context, "url"))))))
                        .then(Commands.literal("clear")
                                .then(Commands.argument("actor", StringArgumentType.word())
                                        .executes(context -> clearSkin(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "actor"))))))
                .then(Commands.literal("list")
                        .executes(context -> listRecordings(context.getSource()))
                )
        );
    }

    private static int startRecording(CommandSourceStack source, String name) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Este comando debe ejecutarlo un jugador."));
            return 0;
        }
        if (!RecordingStorage.isValidName(name)) {
            source.sendFailure(Component.literal("Nombre inválido. Usa letras, números, guion o guion bajo (máximo 48 caracteres)."));
            return 0;
        }
        if (!RecordingManager.start(player, name)) {
            source.sendFailure(Component.literal("Ya estás grabando o no se pudo iniciar la grabación."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Grabación iniciada: " + name + ". Reproduce los emotes de Emotecraft normalmente mientras actúas."), false);
        return 1;
    }

    private static int stopRecording(CommandSourceStack source, boolean cancel) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Este comando debe ejecutarlo el jugador que está grabando."));
            return 0;
        }
        if (cancel) {
            if (!RecordingManager.cancel(player)) {
                source.sendFailure(Component.literal("No tienes una grabación activa."));
                return 0;
            }
            source.sendSuccess(() -> Component.literal("Grabación cancelada."), false);
            return 1;
        }

        try {
            RecordingData saved = RecordingManager.stop(player);
            if (saved == null) {
                source.sendFailure(Component.literal("No había una grabación activa o no se capturaron fotogramas."));
                return 0;
            }
            source.sendSuccess(() -> Component.literal("Grabación guardada: " + saved.name + " (" + saved.durationTicks + " ticks, " + saved.emotes.size() + " emotes)."), false);
            return 1;
        } catch (IOException exception) {
            source.sendFailure(Component.literal("No se pudo guardar la grabación: " + exception.getMessage()));
            return 0;
        }
    }

    private static int play(CommandSourceStack source, String recordingName, String actorName) {
        ServerLevel level = source.getLevel();
        try {
            RecordingData data = RecordingStorage.load(source.getServer(), recordingName);
            if (data == null) {
                source.sendFailure(Component.literal("No existe la grabación: " + recordingName));
                return 0;
            }

            Vec3 position = source.getPosition();
            float requestedYaw = source.getRotation().y;
            float yawOffset = requestedYaw - data.frames.get(0).yaw;
            RecordedActorEntity actor = ModEntities.RECORDED_ACTOR.get().create(level);
            if (actor == null) {
                source.sendFailure(Component.literal("No se pudo crear el actor."));
                return 0;
            }
            actor.configurePlayback(recordingName, actorName, data, level.getGameTime(),
                    position.x, position.y, position.z, yawOffset);
            actor.setYRot(requestedYaw);
            actor.setYHeadRot(requestedYaw);
            actor.setYBodyRot(requestedYaw);
            if (!level.addFreshEntity(actor)) {
                source.sendFailure(Component.literal("No se pudo añadir el actor al mundo."));
                return 0;
            }
            source.sendSuccess(() -> Component.literal("Actor " + actorName + " reproduciendo " + recordingName + "."), false);
            return 1;
        } catch (IOException exception) {
            source.sendFailure(Component.literal("No se pudo leer la grabación: " + exception.getMessage()));
            return 0;
        }
    }

    private static int setSkin(CommandSourceStack source, String actorName, String url) {
        if (!SkinUrls.isAllowed(url)) {
            source.sendFailure(Component.literal("URL inválida. Usa un enlace HTTPS directo a una imagen PNG/JPG pública."));
            return 0;
        }
        RecordedActorEntity actor = findActor(source, actorName);
        if (actor == null) {
            source.sendFailure(Component.literal("No se encontró un actor cargado llamado " + actorName + "."));
            return 0;
        }
        actor.setSkinUrl(url);
        source.sendSuccess(() -> Component.literal("Skin asignada a " + actorName + ". Los clientes descargarán la textura al verla."), false);
        return 1;
    }

    private static int clearSkin(CommandSourceStack source, String actorName) {
        RecordedActorEntity actor = findActor(source, actorName);
        if (actor == null) {
            source.sendFailure(Component.literal("No se encontró un actor cargado llamado " + actorName + "."));
            return 0;
        }
        actor.setSkinUrl("");
        source.sendSuccess(() -> Component.literal("Skin personalizada retirada de " + actorName + "."), false);
        return 1;
    }

    private static RecordedActorEntity findActor(CommandSourceStack source, String actorName) {
        AABB searchArea = new AABB(-3.0E7, -2048, -3.0E7, 3.0E7, 2048, 3.0E7);
        for (ServerLevel level : source.getServer().getAllLevels()) {
            for (RecordedActorEntity actor : level.getEntitiesOfClass(RecordedActorEntity.class, searchArea,
                    entity -> entity.getActorName() != null && entity.getActorName().equalsIgnoreCase(actorName))) {
                return actor;
            }
        }
        return null;
    }

    private static int listRecordings(CommandSourceStack source) {
        try {
            var recordings = RecordingStorage.list(source.getServer());
            if (recordings.isEmpty()) {
                source.sendSuccess(() -> Component.literal("Todavía no hay grabaciones."), false);
                return 1;
            }
            source.sendSuccess(() -> Component.literal("Grabaciones: " + String.join(", ", recordings)), false);
            return recordings.size();
        } catch (IOException exception) {
            source.sendFailure(Component.literal("No se pudo leer la carpeta de grabaciones."));
            return 0;
        }
    }
}
