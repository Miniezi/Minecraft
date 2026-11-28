"""Verify a built installer, its exact dependency lock and optional staged JARs."""
import argparse
import gzip
import hashlib
import io
import json
import re
from pathlib import Path
from zipfile import ZipFile

from build import ADDON_NAME, LOCK_FILE, MINECRAFT, NEOFORGE, OUTPUT, SOURCE, level_dat, main


def verify(staged_mods=None):
    main()
    pack_path = OUTPUT / "Energy-Grid-Skyblock-RU.mrpack"
    baseline = pack_path.read_bytes()
    main()
    assert pack_path.read_bytes() == baseline
    locked = json.loads(LOCK_FILE.read_text(encoding="utf-8"))
    with ZipFile(pack_path) as pack:
        assert pack.testzip() is None
        manifest = json.loads(pack.read("modrinth.index.json"))
        assert (manifest["formatVersion"], manifest["game"]) == (1, "minecraft")
        assert manifest["dependencies"] == {"minecraft": MINECRAFT, "neoforge": NEOFORGE}
        assert manifest["files"] == [mod["file"] for mod in locked if mod["source"] != "Local"]
        paths = [f["path"] for f in manifest["files"]]
        assert len(paths) == len(set(paths)) and len(paths) > 0
        for file in manifest["files"]:
            assert file["path"].startswith("mods/") and ".." not in Path(file["path"]).parts
            assert all(url.startswith("https://") for url in file["downloads"])
            assert re.fullmatch(r"[0-9a-f]{40}", file["hashes"]["sha1"])
            assert re.fullmatch(r"[0-9a-f]{128}", file["hashes"]["sha512"])
            if staged_mods:
                local = (staged_mods / Path(file["path"]).name).read_bytes()
                assert len(local) == file["fileSize"]
                assert all(hashlib.new(name, local).hexdigest() == value
                           for name, value in file["hashes"].items()), file["path"]
        assert pack.read("overrides/saves/Energy-Grid/level.dat") == level_dat()
        assert b"minecraft:the_void" in gzip.decompress(level_dat())
        assert b"lang:ru_ru" in pack.read("overrides/options.txt")
        assert pack.read("overrides/resourcepacks/Energy-Grid-RU.zip") == (
            OUTPUT / "Energy-Grid-RU.zip"
        ).read_bytes()
        addon_bytes = pack.read("overrides/mods/" + ADDON_NAME)
        with ZipFile(io.BytesIO(addon_bytes)) as addon:
            assert addon.testzip() is None
            assert "dev/energogrid/EnergyGridExport.class" in addon.namelist()
            assert b'modId="energogrid_export"' in addon.read("META-INF/neoforge.mods.toml")
        assert "overrides/config/ftbquests/quests/chapters/first_sale.snbt" in pack.namelist()

    functions = {path.stem: path.read_text(encoding="utf-8") for path in
                 SOURCE.joinpath("data/energogrid/function").glob("*.mcfunction")}
    for text in functions.values():
        for target in re.findall(r"\bfunction energogrid:(\w+)", text):
            assert target in functions, target
    book = SOURCE.joinpath("data/energogrid/loot_table/terminal_book.json").read_text(encoding="utf-8")
    routes = set(re.findall(r"eg matches (\d+) run function", functions["terminal"]))
    choices = set(re.findall(r"/trigger eg set (\d+)", "".join(functions.values()) + book))
    assert choices <= (routes | {"1"})
    print(f"OK: reproducible MRPACK, {len(paths)} downloads, custom NeoForge JAR and first-sale quest")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--staged-mods", type=Path)
    verify(parser.parse_args().staged_mods)
