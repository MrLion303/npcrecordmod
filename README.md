# NPC Record Mod

Mod de Forge 1.20.1 para grabar actuaciones de jugadores y reproducirlas como actores NPC durante eventos en vivo, con un flujo de trabajo inspirado en los eventos de Fortnite.

La prioridad es preparar una actuación una vez y ejecutarla con un comando durante un evento. El servidor controla el tiempo y el movimiento de los actores; los clientes reciben la actuación para verla sincronizada.

## Entorno objetivo

- Minecraft: 1.20.1
- Forge: 47.4.20
- Java: 17
- Emotecraft: **2.2.7-b.build.50 para Forge 1.20.1, obligatorio**
- PlayerAnimator: dependencia obligatoria de Emotecraft
- GeckoLib: 4.8.4, opcional para futuras apariencias animadas personalizadas

Emotecraft y PlayerAnimator deben estar instalados tanto en el servidor como en todos los clientes. La versión de Emotecraft objetivo es exactamente `2.2.7-b.build.50`.

## Emotes grabados: no hacen falta los archivos originales en los espectadores

Durante la grabación, NPC Record Mod recibe el objeto de animación de Emotecraft, lo serializa y guarda sus datos de keyframes dentro del archivo de actuación.

Por tanto:

- El jugador que graba debe tener Emotecraft y poder reproducir el emote.
- Los espectadores deben tener Emotecraft y PlayerAnimator instalados.
- Los espectadores **no necesitan tener instalado el archivo individual del emote** usado durante la grabación.
- La grabación incluye la animación necesaria para reproducir ese emote mediante el motor PlayerAnimator.
- No se guarda solamente un nombre o UUID que obligue a cada cliente a buscar el emote en su carpeta.

Esto permite compartir una misma actuación con los jugadores del evento aunque no tengan descargado el paquete de emotes original.

## Cómo se graba una actuación

1. Un operador inicia la grabación con `/npcrecord record start llegada_agente`.
2. El jugador actúa normalmente: camina, corre, salta, gira, se agacha y reproduce emotes.
3. El servidor registra posición, orientación, velocidad y estados de movimiento.
4. Cuando Emotecraft inicia un emote, el cliente del jugador que graba envía la animación serializada al servidor.
5. Al detener la grabación, los datos quedan guardados como un JSON dentro del mundo.

Para grabar los movimientos de la animación del cuerpo, usa tercera persona durante la actuación.

## Comandos

Todos los comandos requieren OP nivel 2.

### Grabación

```mcfunction
/npcrecord record start <nombre>
/npcrecord record stop
/npcrecord record cancel
/npcrecord list
```

Ejemplo:

```mcfunction
/npcrecord record start llegada_agente
# Actúa dentro del juego...
/npcrecord record stop
```

Las grabaciones se guardan en:

```text
<carpeta-del-mundo>/npcrecord/recordings/<nombre>.json
```

### Reproducir una grabación

```mcfunction
/npcrecord play <grabacion>
/npcrecord play <grabacion> <nombre_del_actor>
```

Ejemplo:

```mcfunction
/npcrecord play llegada_agente AgenteJones
```

El actor aparece donde se ejecuta el comando. La trayectoria se reproduce de forma relativa a ese punto, para reutilizar la misma actuación en distintas zonas del mapa.

### Asignar una skin desde un enlace

```mcfunction
/npcrecord skin set <nombre_del_actor> "<url_directa_de_la_imagen>"
/npcrecord skin clear <nombre_del_actor>
```

Ejemplo:

```mcfunction
/npcrecord skin set AgenteJones "https://i.imgur.com/ejemplo.png"
```

El enlace debe apuntar directamente a una imagen de skin compatible, no a una página de galería o publicación. Se aceptan imágenes PNG/JPG de 64x64 o 64x32. Los clientes que renderizan al actor descargan la textura HTTPS y la guardan en caché local.

Usa enlaces directos como `i.imgur.com`. Un enlace a una página de Imgur como `imgur.com/a/...` no es una textura directa.

## Diseño para eventos en vivo

- Las posiciones y el reloj de reproducción se controlan desde el servidor.
- Las grabaciones no dependen de que el jugador que las creó permanezca conectado.
- Los espectadores no necesitan el archivo individual de los emotes grabados.
- La apariencia del actor se asigna por separado de la actuación.
- Una misma grabación se puede reproducir varias veces y desde distintas ubicaciones.
- Los emotes se transmiten como datos de animación integrados en la grabación.

## Estado del desarrollo

El repositorio está en desarrollo temprano. La base inicial prepara el proyecto Forge, el formato de grabación, la entidad de actor, comandos, skins por URL y la ruta de integración con Emotecraft/PlayerAnimator.

Antes de utilizarlo en eventos reales faltan pruebas y trabajo en:

- Captura de emotes mediante la API de Emotecraft 2.2.7-b.build.50.
- Reproducción de los keyframes almacenados sobre el modelo NPC.
- Sincronización y pre-carga de actuaciones para grupos grandes de espectadores.
- Transiciones entre caminar, correr, idle y emotes.
- Modelos personalizados de Blockbench.
- Escenas con varios actores y reproducción coordinada.
- Editor visual para revisar y organizar actuaciones.

No se considera una versión terminada ni un lanzamiento.

## Compilación

El workflow **Compilar mod** se ejecuta al hacer push a `main`, al abrir un pull request o manualmente desde GitHub Actions. Usa Java 17 y Gradle 8.8 y publica el JAR como artefacto de Actions.

No crea Releases de GitHub.

Para compilar localmente con Gradle 8.8:

```bash
gradle build
```

El archivo JAR se genera en `build/libs/`.

## Créditos y licencias

- Emotecraft: GPL-3.0-or-later.
- PlayerAnimator: MIT.
- GeckoLib: conserva su licencia original.
- NPC Record Mod: proyecto de NegativeStudios.

Las dependencias no se incluyen ni se redistribuyen dentro del JAR del mod.
