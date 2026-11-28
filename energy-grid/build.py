"""Build the Prism prototype and its standalone resource pack."""
import gzip
import io
import json
import struct
from pathlib import Path
from zipfile import ZIP_DEFLATED, ZipFile, ZipInfo

SOURCE = Path(__file__).resolve().parent / "src"
OUTPUT = Path(__file__).resolve().parent.parent / "dist"
MINECRAFT = "1.21.1"
NEOFORGE = "21.1.268"
ADDON_NAME = "energy-grid-export-0.3.0.jar"
ADDON = SOURCE.parent / "addon" / "build" / "libs" / ADDON_NAME
LOCK_FILE = SOURCE.parent / "mods.lock.json"
PACK_NAME = "Энергосеть — Небесный контракт"


def json_bytes(value):
    return json.dumps(value, ensure_ascii=False, indent=2).encode()


def nbt_string(value):
    encoded = value.encode()
    return struct.pack(">H", len(encoded)) + encoded


def nbt_payload(kind, value):
    if kind == 10:
        return b"".join(
            bytes([tag]) + nbt_string(name) + nbt_payload(tag, item)
            for name, (tag, item) in value.items()
        ) + b"\x00"
    if kind == 9:
        child, items = value
        return bytes([child]) + struct.pack(">i", len(items)) + b"".join(
            nbt_payload(child, item) for item in items
        )
    if kind == 8:
        return nbt_string(value)
    return struct.pack({1: ">b", 3: ">i", 4: ">q"}[kind], value)


def level_dat():
    integer, string, compound = 3, 8, 10
    dimensions = {
        "minecraft:overworld": (compound, {
            "type": (string, "minecraft:overworld"),
            "generator": (compound, {
                "type": (string, "minecraft:flat"),
                "settings": (compound, {
                    "biome": (string, "minecraft:the_void"),
                    "features": (1, 0),
                    "lakes": (1, 0),
                    "layers": (9, (compound, [])),
                    "structure_overrides": (9, (string, [])),
                }),
            }),
        }),
    }
    for dimension, biome_source in {
        "the_nether": {"type": (string, "minecraft:multi_noise"),
                       "preset": (string, "minecraft:nether")},
        "the_end": {"type": (string, "minecraft:the_end")},
    }.items():
        key = f"minecraft:{dimension}"
        dimensions[key] = (compound, {
            "type": (string, key),
            "generator": (compound, {
                "type": (string, "minecraft:noise"),
                "settings": (string, "minecraft:" + dimension.removeprefix("the_")),
                "biome_source": (compound, biome_source),
            }),
        })
    data = {
        "DataVersion": (integer, 3955),
        "Version": (compound, {"Id": (integer, 3955), "Name": (string, "1.21.1"),
                               "Series": (string, "main"), "Snapshot": (1, 0)}),
        "version": (integer, 19133),
        "DragonFight": (compound, {}),
        "LevelName": (string, "Энергосеть — Небесный контракт"),
        "GameType": (integer, 0), "Difficulty": (1, 2), "allowCommands": (1, 0),
        "initialized": (1, 1),
        "SpawnX": (integer, 0), "SpawnY": (integer, 81), "SpawnZ": (integer, 0),
        "WorldGenSettings": (compound, {
            "seed": (4, 348179), "generate_features": (1, 0),
            "bonus_chest": (1, 0), "dimensions": (compound, dimensions),
        }),
        "DataPacks": (compound, {
            "Enabled": (9, (string, ["vanilla", "file/energy-grid"])),
            "Disabled": (9, (string, [])),
        }),
        "GameRules": (compound, {"spawnRadius": (string, "0")}),
    }
    return gzip.compress(b"\x0a\x00\x00" + nbt_payload(compound, {"Data": (compound, data)}), mtime=0)


def archive(files):
    buffer = io.BytesIO()
    with ZipFile(buffer, "w", compression=ZIP_DEFLATED) as output:
        for name, contents in sorted(files.items()):
            entry = ZipInfo(name)
            entry.compress_type = ZIP_DEFLATED
            output.writestr(entry, contents)
    return buffer.getvalue()


def main():
    OUTPUT.mkdir(exist_ok=True)
    resource_name = "Energy-Grid-RU.zip"
    resources = {"pack.mcmeta": json_bytes({"pack": {
        "pack_format": 34, "description": "Энергосеть — русский интерфейс"
    }})}
    for path in SOURCE.joinpath("assets").rglob("*.json"):
        resources[path.relative_to(SOURCE).as_posix()] = path.read_bytes()
    resource_pack = archive(resources)
    world = "saves/Energy-Grid"
    datapack = world + "/datapacks/energy-grid"
    files = {
        "options.txt": (
            'lang:ru_ru\nresourcePacks:["vanilla","file/Energy-Grid-RU.zip"]\n'
        ).encode(),
        f"resourcepacks/{resource_name}": resource_pack,
        f"{world}/level.dat": level_dat(),
        f"{datapack}/pack.mcmeta": json_bytes({"pack": {
            "pack_format": 48, "description": "Первый платёж"
        }}),
    }
    for tag in ("load", "tick"):
        files[f"{datapack}/data/minecraft/tags/function/{tag}.json"] = json_bytes({
            "values": [f"energogrid:{tag}"]
        })
    for path in SOURCE.joinpath("data").rglob("*"):
        if path.is_file():
            files[f"{datapack}/{path.relative_to(SOURCE).as_posix()}"] = path.read_bytes()
    locked = json.loads(LOCK_FILE.read_text(encoding="utf-8"))
    index = {
        "formatVersion": 1,
        "game": "minecraft",
        "versionId": "0.3.0",
        "name": PACK_NAME,
        "summary": "SkyBlock, Forge Energy, промышленность и дата-центр",
        "files": [mod["file"] for mod in locked if mod["source"] != "Local"],
        "dependencies": {"minecraft": MINECRAFT, "neoforge": NEOFORGE},
    }
    modrinth_files = {f"overrides/{path}": contents for path, contents in files.items()}
    for path in SOURCE.joinpath("config").rglob("*"):
        if path.is_file():
            modrinth_files[f"overrides/{path.relative_to(SOURCE).as_posix()}"] = path.read_bytes()
    modrinth_files[f"overrides/mods/{ADDON_NAME}"] = ADDON.read_bytes()
    (OUTPUT / resource_name).write_bytes(resource_pack)
    modrinth_files["modrinth.index.json"] = json_bytes(index)
    (OUTPUT / "Energy-Grid-Skyblock-RU.mrpack").write_bytes(archive(modrinth_files))


if __name__ == "__main__":
    main()
