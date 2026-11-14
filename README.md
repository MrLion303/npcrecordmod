# NPC Record Mod

Mod de Forge 1.20.1 para grabar los movimientos y las acciones de un único personaje y reproducir esa actuación como un NPC independiente durante eventos en vivo, con un flujo de trabajo inspirado en los personajes controlados de los eventos de Fortnite.

## Concepto principal

**Una grabación representa a un solo personaje NPC.** No es una escena cinematográfica ni un conjunto de personajes. Es la actuación completa de ese personaje: dónde camina, cuándo corre, hacia dónde mira, qué acciones hace y en qué momentos reproduce emotes.

El flujo es sencillo: grabas una actuación, la guardas y luego creas un NPC que la reproduce. La apariencia del NPC se configura por separado, de modo que puedes cambiar su skin o modelo sin volver a grabar sus movimientos.

El servidor controla el tiempo y el desplazamiento del personaje. Los clientes reciben los datos necesarios para mostrar la misma actuación sincronizada.

## Entorno objetivo

- Minecraft: 1.20.1
- Forge: 47.4.20
- Java: 17
- Emotecraft: **2.2.7-b.build.50 para Forge 1.20.1, obligatorio**
- PlayerAnimator: dependencia obligatoria de Emotecraft
- GeckoLib: 4.8.4, opcional para futuras apariencias animadas personalizadas

Emotecraft y PlayerAnimator deben estar instalados tanto en el servidor como en todos los clientes. La versión de Emotecraft objetivo es exactamente `2.2.7-b.build.50`.

## Emotes grabados: no hacen falta los archivos originales en los espectadores

Durante la grabación, NPC Record Mod escucha los eventos de Emotecraft y recibe el objeto `KeyframeAnimation`. Serializa los keyframes con el formato de Emotecraft y los guarda dentro del archivo de actuación, junto con el instante en que empieza y termina el emote.

Por tanto:

- El jugador que graba debe tener Emotecraft y poder reproducir el emote.
- Los espectadores deben tener Emotecraft y PlayerAnimator instalados.
- Los espectadores **no necesitan tener instalado el archivo individual del emote** usado durante la grabación.
- La grabación contiene los datos de animación necesarios para reproducir ese emote mediante PlayerAnimator.
- No se guarda solamente un nombre o UUID que obligue a cada cliente a buscar el emote en su carpeta.

Esto permite que el personaje reproduzca su actuación aunque los espectadores no tengan descargado el paquete de emotes original.

## Cómo se graba un personaje

1. Un operador inicia la grabación con `/npcrecord record start llegada_agente`.
2. El jugador actúa como el personaje: camina, corre, salta, gira, se agacha y reproduce emotes.
3. El servidor registra posición, orientación, velocidad y estados de movimiento en cada tick.
4. Cuando Emotecraft inicia o detiene un emote, el cliente del jugador que graba envía al servidor los datos de keyframes y su marca temporal.
5. Al detener la grabación, la actuación de ese único personaje se guarda como un JSON dentro del mundo.

Para capturar los emotes, el jugador que actúa necesita Emotecraft 2.2.7-b.build.50. Los clientes espectadores no necesitan los archivos originales de los emotes.

## Comandos

Todos los comandos requieren OP nivel 2.

### Grabar la actuación de un personaje

```mcfunction
/npcrecord record start <nombre>
/npcrecord record stop
/npcrecord record cancel
/npcrecord list
```

Ejemplo:

```mcfunction
/npcrecord record start llegada_agente
# Actúa como el personaje...
/npcrecord record stop
```

Cada archivo contiene la actuación de un único personaje y se guarda en:

```text
<carpeta-del-mundo>/npcrecord/recordings/<nombre>.json
```

### Crear el NPC y reproducir su actuación

```mcfunction
/npcrecord play <grabacion>
/npcrecord play <grabacion> <nombre_del_actor>
```

Ejemplo:

```mcfunction
/npcrecord play llegada_agente AgenteJones
```

Este comando crea un único NPC que reproduce la actuación guardada. El personaje aparece donde se ejecuta el comando y recorre la trayectoria registrada de forma relativa a ese punto, por lo que puedes reutilizar la misma actuación en otra ubicación del mapa.

### Asignar una skin desde un enlace

```mcfunction
/npcrecord skin set <nombre_del_actor> "<url_directa_de_la_imagen>"
/npcrecord skin clear <nombre_del_actor>
```

Ejemplo:

```mcfunction
/npcrecord skin set AgenteJones "https://i.imgur.com/ejemplo.png"
```

El enlace debe apuntar directamente a una imagen de skin compatible, no a una página de galería o publicación. Se aceptan imágenes PNG/JPG de 64x64 o 64x32. Los clientes que renderizan al NPC descargan la textura HTTPS y la guardan en caché local.

Usa enlaces directos como `i.imgur.com`. Un enlace a una página de Imgur como `imgur.com/a/...` no es una textura directa.

## Diseño para eventos en vivo

- Cada grabación contiene un solo personaje.
- El NPC reproduce la actuación sin depender de que el jugador que la grabó permanezca conectado.
- El servidor controla la línea de tiempo y el desplazamiento del NPC.
- Los espectadores no necesitan el archivo individual de los emotes grabados.
- La skin y, más adelante, el modelo del NPC se pueden cambiar sin volver a grabar la actuación.
- La línea temporal de emotes se envía a cada cliente cuando empieza a seguir al NPC.

## Estado del desarrollo

La base inicial ya incluye:

- Proyecto Forge 1.20.1 / 47.4.20 y configuración de Java 17.
- Emotecraft 2.2.7-b.build.50 y PlayerAnimator como dependencias obligatorias.
- Grabación de movimiento y orientación desde el servidor.
- Captura de eventos de Emotecraft y serialización de sus keyframes dentro del JSON.
- Sincronización de la línea temporal de emotes con los clientes que ven al NPC.
- Entidad NPC que reproduce la trayectoria y aplica los keyframes de PlayerAnimator al modelo humanoide.
- Comandos iniciales para grabar, reproducir, listar y asignar skins por URL.
- Compilación verificada mediante GitHub Actions.

Antes de utilizarlo en eventos reales faltan pruebas y trabajo en:

- Pruebas en servidor dedicado con Emotecraft 2.2.7-b.build.50 y varios espectadores, incluida la verificación visual de todos los emotes.
- Interpolación y sincronización fina para desplazamientos fluidos durante actuaciones largas.
- Transiciones configurables entre caminar, correr, idle y emotes.
- Modelos y animaciones personalizados de Blockbench mediante GeckoLib.
- Herramientas para revisar, organizar, recortar y ajustar la actuación individual del NPC.

La compilación correcta no sustituye las pruebas dentro del juego. El mod sigue en desarrollo y no se considera una versión terminada ni un lanzamiento.

## Compilación

El workflow **Compilar mod** se ejecuta al hacer push a `main`, al abrir un pull request o manualmente desde GitHub Actions. Usa Java 17 y Gradle 8.8 y publica el JAR como artefacto de Actions.

La compilación del commit que inicializó el proyecto terminó correctamente. El artefacto se llama `npcrecordmod-forge-1.20.1` y está disponible en la ejecución de GitHub Actions. El workflow no crea Releases de GitHub.

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
