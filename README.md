# Outline Guard

Outline Guard is a fully client-side Fabric mod for Minecraft 1.21.4.

It helps prevent accidental vertical mining past your own outline markers while clearing an area. The mod only affects the player who has it installed. It does not protect blocks from other players, and it should not be installed on a dedicated server.

## Features

- Client-side only
- No server installation required
- No custom networking
- No mixins
- Configurable marker block list
- Smooth mining pause/resume behavior
- In-game GUI opened with **D + C**

## How to use

1. Install the mod on your client.
2. Join a world or server.
3. Hold **D** and press **C** to open the Outline Guard GUI.
4. Enter a block ID, such as `minecraft:netherrack` or `minecraft:red_concrete`.
5. Click **Set as only marker** or **Add marker**.
6. Place marker blocks around the area you want to clear.

When your crosshair reaches a protected block column, Outline Guard pauses your mining so you do not accidentally mine past the outline. When you move back to a safe block while still holding left click, mining resumes automatically.

## GUI options

- **Set as only marker**: replaces your local marker list with the typed block ID.
- **Add marker**: adds the typed block ID to your local marker list.
- **Remove marker**: removes the typed block ID from your local marker list.
- **Reload config**: re-reads `.minecraft/config/outlineguard-client.json` from disk. Most players do not need this button. It is only useful if you manually edit the config file while Minecraft is still open.

## Config file

The config is saved locally at:

```text
.minecraft/config/outlineguard-client.json
```

Default config:

```json
{
  "marker_blocks": [
    "minecraft:netherrack"
  ]
}
```

## Build

Windows PowerShell:

```powershell
.\gradlew.bat build
```

macOS/Linux:

```bash
./gradlew build
```

The built mod jar will appear in:

```text
build/libs/
```

Use the regular `.jar`, not the `-sources.jar`.

## Requirements

- Minecraft 1.21.4
- Fabric Loader 0.16.10 or newer
- Fabric API
- Java 21 or newer

## Author

CSClub05

## License

MIT
