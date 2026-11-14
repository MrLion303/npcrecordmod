package com.negativestudios.npcrecordmod.recording;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class RecordingStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Pattern SAFE_NAME = Pattern.compile("[a-zA-Z0-9_-]{1,48}");

    private RecordingStorage() {
    }

    public static boolean isValidName(String name) {
        return name != null && SAFE_NAME.matcher(name).matches();
    }

    private static Path directory(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).resolve("npcrecord").resolve("recordings");
    }

    private static Path file(MinecraftServer server, String name) {
        if (!isValidName(name)) throw new IllegalArgumentException("Nombre de grabación inválido");
        return directory(server).resolve(name + ".json");
    }

    public static void save(MinecraftServer server, RecordingData data) throws IOException {
        Files.createDirectories(directory(server));
        Files.writeString(file(server, data.name), GSON.toJson(data), StandardCharsets.UTF_8);
    }

    public static RecordingData load(MinecraftServer server, String name) throws IOException {
        Path path = file(server, name);
        if (!Files.isRegularFile(path)) return null;
        RecordingData data = GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), RecordingData.class);
        if (data == null || data.frames == null || data.frames.isEmpty()
                || (data.formatVersion != 1 && data.formatVersion != 2)) {
            throw new IOException("La grabación está vacía o usa un formato no compatible");
        }
        if (data.emotes == null) data.emotes = new ArrayList<>();
        return data;
    }

    public static List<String> list(MinecraftServer server) throws IOException {
        List<String> names = new ArrayList<>();
        Path folder = directory(server);
        if (!Files.isDirectory(folder)) return names;
        try (Stream<Path> paths = Files.list(folder)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .map(path -> path.getFileName().toString().replaceFirst("\\.json$", ""))
                    .filter(RecordingStorage::isValidName)
                    .sorted()
                    .forEach(names::add);
        }
        return names;
    }
}
