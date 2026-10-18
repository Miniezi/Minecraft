# MRPACK Builder — Minecraft 1.20.1 Forge

**Этот репозиторий содержит СБОРЩИК (генератор) файлов .mrpack, а НЕ готовую Minecraft-сборку.**

The project is a **MRPACK BUILDER**, not a packaged, tested modpack. The generator downloads release metadata (not JARs), assembles SHA-verified references to Modrinth-hosted files, and writes a Prism Launcher-importable `.mrpack` manifest.

## How to use
- Python 3.10+ and internet access: `python build_mrpack.py`
- Optional: `python build_mrpack.py --lean` or `--strict-extras`
- GitHub Actions → **Build MRPACK Builder Output** → Run workflow, branch `minecraft`; download the generated MRPACK artifact.
- In Prism Launcher: **Add Instance → Import** the generated `.mrpack`.
- All friends should import the **same** resulting file, not independently rebuild against different current versions.
- Dedicated Forge 47.4.10 Minecraft 1.20.1 server (Java 17): run `python install_server_mods.py generated.mrpack /path/to/forge/server`. Forge itself must be installed separately.

## Design
- Core: MCA Reborn, MCA Conversations, MCA Quests, Townstead, Architectury API.
- Extras: farming, building, seasons, maps and optional client optimisation.
- Modrinth API is queried **when building**. Version numbers and hashes are taken from live responses, not fabricated.
- Optional mods missing from Modrinth may be skipped (shown in build report).
- CurseForge-only MCA Reputation / Crime are not silently bundled.
- Client-only files must be excluded from the dedicated server.
- Generated pack and multiplayer have **not been tested in-game**. Successful CI proves only manifest generation, not mod compatibility.

## For other AI agents
1. This is a Python **generator** for .mrpack; don't describe the repo or source zip as a completed pack.
2. Modify **branch `minecraft` only** unless the owner explicitly says otherwise.
3. Entry points: `build_mrpack.py` and `install_server_mods.py`. CI: `.github/workflows/build-mrpack.yml`.
4. Preserve Modrinth pack format, real hashes, dependency checks, and client/server distinctions.
5. Do not commit third-party JARs without redistribution permission.
6. Add real multiplayer integration tests before calling output fully compatible.
