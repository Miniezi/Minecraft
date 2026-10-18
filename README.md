# MRPACK Builder — MCA Ultimate Village (Forge 1.20.1)

**Это исходники СБОРЩИКА MRPACK (генератора), а не готовая вручную протестированная сборка.**
**This repository is an MRPACK BUILDER, not a prepackaged/tested modpack.**

This project creates a Prism Launcher-compatible `.mrpack`, resolving real JAR URLs and SHA1/SHA512 hashes at build time. GitHub Actions packages the **generated output** separately as a downloadable artifact. Third-party JARs are not committed.

## Target & purpose

- Minecraft Java **1.20.1**, **Forge 47.4.10**, **Java 17**
- 32 GB host RAM: start with 10 GB allocated to the client.
- The intended gameplay revolves around MCA villagers, marriage and children, conversations, reputation, crime, noble families, and co-op village projects.
- Core integrations: MCA Reborn 7.6.26, MCA Conversations, MCA Quests, Townstead 0.7.6, MCA Capitals (pinned to a MCA 7.6.26-compatible build), MCA Crime and MCA Reputation.
- Extra mods: bounty boards, food, farm/housing, building blocks, Create and village facilities, mapping and performance.
- **Three author-created co-op village projects in Russian:** communal granary, night watch and village festival. These are bundled as a Paxi datapack in every newly created world.

## How to build in GitHub

1. Open Actions → **Build MRPACK Builder Output**.
2. Select branch **`mrpack-builder`**, then click **Run workflow**.
3. Download artifact `mrpack-builder-output`.
4. Extract `MCA-Ultimate-Life-Friends-1.20.1.mrpack` and import in Prism Launcher via Add Instance → Import.
5. Share the **identical** generated `.mrpack` with friends. Do not build independently on different days because external releases can change.

Alternatively, with Python 3.10+ and internet access:

```bash
python scripts/translate_quests.py --seconds 365
python build_mrpack.py
# or a smaller experimental preset:
python build_mrpack.py --lean
```

`--strict-extras` makes missing optional mods fail the build. The `.mod-list.txt` report records resolved versions and skipped mods.

## Russian localization coverage

- `localization/mcaquests_ru_ru.json`: **150 manually translated UI and objectives strings**; manual translations win.
- `scripts/translate_quests.py`: best-effort machine translation of MCA Quests' English quest titles, dialogue, shared projects and situations, preserving/verifying formatting placeholders.
- The workflow embeds any validated generated keys into `overrides/resourcepacks/MCA-Quests-Russian-UI.zip` and enables it in Prism's new instance with `options.txt`.
- Machine translation is **not proofread**, and untranslated keys continue to display in English. This is **not a complete Russian translation** of every integrated mod.

## Dedicated server and co-op

Install a dedicated Forge 47.4.10 server for Minecraft 1.20.1 (Java 17). Install matching mods with:

```bash
python install_server_mods.py MCA-Ultimate-Life-Friends-1.20.1.mrpack /path/to/forge-server
```

The server installer copies only server-compatible JARs and the Paxi village datapack from the same `.mrpack`; it does **not** install Forge, generate launch scripts, or start the Minecraft server. Remove old duplicate JARs before starting and back up the world before updates.

Use `/datapack list` to inspect Paxi's community datapack, `/mcaquests project list` and `/mcaquests project validate` to inspect shared projects, and `/mcaquests validate` for quest definitions. MCA Quests supports shared project progress server-side; different players can contribute to the same village project.

## For other AI agents

1. This is **MRPACK BUILDER source code**. Distinguish the source repository from the output `.mrpack`. Do not describe output as multiplayer-tested unless two-client/one-dedicated-server testing actually occurred.
2. Work only in Git branch **`mrpack-builder`**, unless the repository owner authorizes another branch.
3. Entry point: `build_mrpack.py`. Translation: `scripts/translate_quests.py`. Server install: `install_server_mods.py`. CI: `.github/workflows/build-mrpack.yml`.
4. Do not fabricate release versions, URLs or hashes. Resolve downloads, hash downloaded external releases and keep Modrinth manifest entries correct.
5. The MCA Crime GitHub Release and MCA Reputation CurseForge CDN file are only referenced by verified URLs/hashes after a successful build-time download, not mirrored into git.
6. Protect Minecraft 1.20.1's datapack format `15`, supported MCA Quests schema, and distinct client/server environments. For player-facing text preserve placeholders (`%s`, `%1$s`, etc.).
7. GitHub CI currently validates packaging and contents, **not actual Forge startup, datapack semantic validity in a live game, or multiplayer stability**. Test a clean world, two clients and server reboot before publishing compatibility claims.

## Known limitations

- Some optional mods are skipped when no matching Modrinth release is available; consult `.mod-list.txt`.
- A full Russian translation of every MCA module is not included. Machine-generated text may contain awkward phrasing.
- Built-in quest definitions and custom projects require validation on a real Forge server.

## Industrial / steampunk expansion

Build preset additionally requests Immersive Engineering 10.2.0-183 (required, checksummed from official CurseForge file 6206989), Create New Age, Create Crafts & Additions, Create Diesel Generators, Create Enchantment Industry, Immersive Aircraft and others. Some optional modules are skipped if no Forge 1.20.1 Modrinth build exists. A new **Industry for the Village** cooperative project requires Create and Immersive Engineering components. This connects machines to village progression through resource donations; it does **not** make NPCs operate real multiblock machines autonomously.

## Custom Forge addon: MCA Worker Chests (experimental)

Source: `addons/worker-chests/`; build with `gradle build` using Gradle 8.8 and Java 17. The `build-mrpack.yml` workflow compiles the addon and embeds the resulting JAR inside `overrides/mods/`. No external redistribution is necessary; this mod's source code is ours. The dedicated server installer also copies embedded addon JARs.

**How to bind a chest**
1. Sneak + right-click an MCA NPC (or vanilla villager) with a **stick**.
2. Stand next to an ordinary chest within 16 blocks of the NPC, sneak + right-click it with the same stick.
3. The NPC will attempt to navigate toward dropped *farm items* within six blocks, pick them up and deliver them to its assigned chest.
4. The chest must be loaded and reachable; the NPC must stay nearby. Workers may need help with navigation. Avoid double chests and protected inventories in first tests.

Limitations:
- Source compiles against Forge 1.20.1, but actual MCA entity behavior in a multiplayer world is **not yet tested**.
- Currently supports **one chest per NPC** and collection of agricultural drops only; it does not automatically operate Immersive Engineering or Create machinery.
- Assignment is stored on the entity and chest NBT, and the code does not expose a separate GUI or role-based permission system yet.
- The initial release has no chunk loaders. Workers only work in loaded chunks.
- Further tests needed for item loss on interruption, chest destruction, entity death, claims and third-party inventory mods.

For other AI agents: the source must be compiled and validated independently before describing this addon as stable. The work-chest feature is **experimental**.
