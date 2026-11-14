package com.negativestudios.npcrecordmod.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.negativestudios.npcrecordmod.NpcRecordMod;
import com.negativestudios.npcrecordmod.entity.RecordedActorEntity;
import com.negativestudios.npcrecordmod.skin.SkinUrls;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class SkinTextureCache {
    private static final int MAX_IMAGE_BYTES = 1_048_576;
    private static final Map<String, ResourceLocation> TEXTURES = new ConcurrentHashMap<>();
    private static final Set<String> DOWNLOADING = ConcurrentHashMap.newKeySet();
    private static final Set<String> FAILED = ConcurrentHashMap.newKeySet();

    private SkinTextureCache() {
    }

    public static ResourceLocation getTexture(RecordedActorEntity actor) {
        String url = actor.getSkinUrl();
        if (url == null || url.isBlank()) return DefaultPlayerSkin.getDefaultSkin(actor.getUUID());
        ResourceLocation texture = TEXTURES.get(url);
        if (texture != null) return texture;
        if (SkinUrls.isAllowed(url) && !FAILED.contains(url) && DOWNLOADING.add(url)) {
            download(url);
        }
        return DefaultPlayerSkin.getDefaultSkin(actor.getUUID());
    }

    private static void download(String originalUrl) {
        Thread worker = new Thread(() -> {
            NativeImage image = null;
            try {
                byte[] bytes = fetchImage(originalUrl);
                image = NativeImage.read(new ByteArrayInputStream(bytes));
                if (image.getWidth() != 64 || (image.getHeight() != 64 && image.getHeight() != 32)) {
                    throw new IllegalArgumentException("La imagen debe medir 64x64 o 64x32 píxeles.");
                }

                NativeImage readyImage = image;
                image = null;
                Minecraft.getInstance().execute(() -> {
                    try {
                        ResourceLocation location = new ResourceLocation(NpcRecordMod.MOD_ID, "skins/" + sha256(originalUrl));
                        Minecraft.getInstance().getTextureManager().register(location, new DynamicTexture(readyImage));
                        TEXTURES.put(originalUrl, location);
                    } catch (Throwable throwable) {
                        readyImage.close();
                        FAILED.add(originalUrl);
                        NpcRecordMod.LOGGER.warn("No se pudo registrar la textura de skin {}", originalUrl, throwable);
                    } finally {
                        DOWNLOADING.remove(originalUrl);
                    }
                });
            } catch (Throwable throwable) {
                if (image != null) image.close();
                FAILED.add(originalUrl);
                DOWNLOADING.remove(originalUrl);
                NpcRecordMod.LOGGER.warn("No se pudo descargar la skin {}", originalUrl, throwable);
            }
        }, "NPC Record Skin Download");
        worker.setDaemon(true);
        worker.start();
    }

    private static byte[] fetchImage(String startUrl) throws Exception {
        String current = startUrl;
        for (int redirect = 0; redirect <= 3; redirect++) {
            URI uri = URI.create(current);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
                throw new IllegalArgumentException("Solo se permiten enlaces HTTPS.");
            }
            rejectPrivateAddress(uri.getHost());

            HttpURLConnection connection = (HttpURLConnection) new URL(current).openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(7000);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("User-Agent", "NPCRecordMod/0.1");
            int status = connection.getResponseCode();

            if (status >= 300 && status < 400) {
                String location = connection.getHeaderField("Location");
                connection.disconnect();
                if (location == null || redirect == 3) throw new IllegalArgumentException("Demasiadas redirecciones o destino inválido.");
                current = uri.resolve(location).toString();
                continue;
            }
            if (status < 200 || status >= 300) {
                connection.disconnect();
                throw new IllegalArgumentException("El servidor de imágenes respondió HTTP " + status);
            }

            long declaredLength = connection.getContentLengthLong();
            if (declaredLength > MAX_IMAGE_BYTES) {
                connection.disconnect();
                throw new IllegalArgumentException("La imagen supera el límite de 1 MB.");
            }

            try (InputStream input = connection.getInputStream();
                 ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int total = 0;
                int read;
                while ((read = input.read(buffer)) != -1) {
                    total += read;
                    if (total > MAX_IMAGE_BYTES) throw new IllegalArgumentException("La imagen supera el límite de 1 MB.");
                    output.write(buffer, 0, read);
                }
                return output.toByteArray();
            } finally {
                connection.disconnect();
            }
        }
        throw new IllegalArgumentException("No se pudo descargar la imagen.");
    }

    private static void rejectPrivateAddress(String host) throws Exception {
        for (InetAddress address : InetAddress.getAllByName(host)) {
            if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
                    || address.isSiteLocalAddress() || address.isMulticastAddress()) {
                throw new IllegalArgumentException("El destino de la skin debe ser una dirección pública.");
            }
        }
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte valueByte : digest) result.append(String.format("%02x", valueByte));
            return result.toString();
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
