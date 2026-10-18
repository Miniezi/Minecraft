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

### Autonomous resource decisions (experimental v0.2)

The Worker Chests source now models **standalone food need and comfort demand** on a villager, stored as persistent entity NBT:
- Needs rise while the villager is active, and comfort demand increases in rain/low health.
- When picking up edible drops, a villager reserves 0–12 pieces of food depending on need, and deposits the remaining crop items in the assigned chest.
- If the communal chest holds fewer than 16 food items, the villager prioritizes its stock unless critically hungry.
- Personal food reserve is a **virtual NBT counter** consumed by the simulated food need. It does not feed vanilla hunger, Townstead hunger, or MCA mood.
- This is an intentionally limited fallback, **not** a verified integration with MCA/Townstead internal need APIs. A future adapter should read the relevant real hunger, thirst, energy and mood signals via their documented API and consume actual held food safely.

**Known issue:** The original item-collection AI needs in-game testing for pathfinding, unloaded entities, protected item drops and interruptions. Do not claim autonomous work is production ready based solely on compilation.


## Better villages and fast housing (October 2026)

- **ChoiceTheorem's Overhauled Village (CTOV)** adds biome-specific village architecture and outposts. The matching Forge 1.20.1 release is resolved with Modrinth metadata. **Lithostitched** is a dependency for current CTOV builds. World-gen changes normally apply to newly generated chunks only. Existing world villages do not automatically rebuild.
- **Prefab** instantly creates predefined houses and functional structures using craftable blueprints. Forge 1.20.1 version 1.10.0.1 is available. This is the first choice for rapidly establishing MCA homes.
- **Effortless Building** supports mirror, array, and mass-block placement in survival as well as creative. It is the tool for *custom* fast building, whereas Prefab gives instant predefined houses.
- **Blueprints – Prefabricated Structures** adds more quick-build structures, subject to the availability check in the build report.
- **Important:** Towns and Towers, Repurposed Structures, YUNG's Better Villages, and CTOV can overlap. Check the actual generated mod list, test one new world and inspect village diversity, generation speed and NPC spawning before relying on the combination. CTOV does not automatically guarantee MCA villagers in every new building; compatibility may depend on structure/pool configuration.
- **Multiplayer:** all structure generation/building mods must be installed on the dedicated server too (except explicitly client-only UI). Instant-build tools may change terrain or many blocks at once, so take world backups.


## Village electrical grid — Create: Power Grid

This preset now includes **Create: Power Grid** for visually laid-out poles, cables, connectors, electric motors, lamps, electrical machines and power distribution. **It can accept power from other technology generators:** the built-in **FE inverter** converts Forge Energy (FE) into its electrical grid; device connectors carry FE to compatible consumers. This is the recommended simple route to mix energy from Immersive Engineering, Create: New Age and other FE-capable machines.

Quick start: build a FE-producing generator -> attach the Power Grid **FE inverter** as described in the mod's Ponder/JEI -> connect Power Grid wires -> connect a Power Grid machine, lamp or motor. Configure the FE inverter and connector according to the mod's documentation; ordinary FE cables from other mods are not the same as Power Grid's electrical wires. Inverter input/outputs and safety ratings matter. The Power Grid model simulates actual voltage/current; it is not a no-configuration FE cable mod. It is suitable for a power station next to the village, supplying industrial workshops and lights.

This connects the industrial settlement thematically. It does **not** automatically connect NPC Townstead needs or let villagers build wires themselves. Multiplayer server and client must use matching versions. Verify the generated mod list includes the power-grid project and its dependencies before installing.


## Townstead compatibility status (2026-10-19)

Townstead 0.7.6 is deliberately **not included** in the MCA 7.7.1 + MCA Capitals 1.3.5 profile. The official Townstead 0.7.6 Forge 1.20.1 release uses the old MCA `forge.net.mca` class layout and crashes against newer `forge.net.conczin.mca`. MCA Quests' TOWNSTEAD.md describes Townstead 0.7.7 modern and 0.8 but states they were not publicly released. Do **not** insert Townstead 0.7.6 into this profile manually. The custom worker-chests addon falls back to its own experimental virtual food/comfort counters without Townstead, not real Townstead hunger. Restore Townstead only when a compatible released or separately tested build is available.

## Expanded Russian resource pack

`translate_all_mods.py` runs after the quests translation and main builder. It examines the pinned mod JARs' `assets/<namespace>/lang/en_us.json` and `ru_ru.json`, preserves upstream Russian strings, and machine-translates missing strings when placeholders stay valid. It packages `MCA-Expanded-Russian-Language.zip` and enables it for new Prism instances. This covers supported lang JSON keys; mod-authored hard-coded text cannot be reliably replaced using a resource pack. Review machine translations before distributing them as polished Russian localization. Run the builder workflow and check its log for actual per-namespace coverage.

## Townstead compatibility edition (separate from Capitals edition)

The `--townstead` build profile uses **MCA Reborn 7.6.26 + Townstead 0.7.6**, together with MCA Quests, MCA Conversations, infrastructure, Create/IE/Power Grid and experimental worker chests. It intentionally omits **MCA Capitals**, because Capitals >=1.3.5 depends on MCA 7.7.1. This is the only tested-by-manifest combination available from *published* releases; launching Minecraft with these mods still needs a separate real client/server integration test.

Build:
```bash
python scripts/translate_quests.py --seconds 365
python build_mrpack.py --townstead --output MCA-Townstead-Villages-RU-1.20.1.mrpack
python translate_all_mods.py --seconds 330 --pack MCA-Townstead-Villages-RU-1.20.1.mrpack
```
CI workflow: `.github/workflows/build-townstead.yml`; artifact `mrpack-townstead-output`.

Keep worlds separately backed up when moving between profiles. Removing Capitals or changing MCA backwards can affect world data and NPC relationships. **Do not update to MCA 7.7.x in Townstead mode**, and do not add Townstead 0.7.6 to the Capitals mode. An official 0.7.7-modern/0.8 Townstead release may later allow combining them.


## Development cycle: Russian village progression

Implemented the first five vanilla advancement milestones in `datapacks/mcavillage/data/mcavillage/advancements/`: **Начало поселения**, **Тёплый хлеб**, **Деревенский рынок**, **Первая мастерская**, **Энергия для деревни**. The MRPACK builder embeds them in the existing Paxi village datapack, and CI verifies each advancement's JSON and inclusion. These are lightweight, translated progression indicators rather than a replacement for MCA Quests. The electricity advancement tracks redstone supplies, **not** actual Power Grid connectivity, which needs a Forge integration to detect safely.

Next development cycle: improve the custom MCA worker-chests GUI and indicators, including a low-overhead translated status overlay and clearer work/comfort controls; confirm class compatibility with the selected MCA version before shipping. Later cycles: unify the village's visual palette and improve sound/lighting using compatible optional mods. Test actual launch and back up world data before declaring runtime stability.
