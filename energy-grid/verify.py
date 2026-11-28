"""Validate the built prototype against its fixed starter economy."""
import gzip
import json
import re
from zipfile import ZipFile

from build import OUTPUT, SOURCE, level_dat, main


def verify():
    main()
    archive_path = OUTPUT / "Energy-Grid-Skyblock-RU-Prototype.zip"
    modrinth_path = OUTPUT / "Energy-Grid-Skyblock-RU.mrpack"
    baseline = archive_path.read_bytes()
    modrinth_baseline = modrinth_path.read_bytes()
    main()
    assert archive_path.read_bytes() == baseline
    assert modrinth_path.read_bytes() == modrinth_baseline
    with ZipFile(archive_path) as archive, ZipFile(modrinth_path) as modrinth:
        assert archive.testzip() is None
        assert modrinth.testzip() is None
        manifest = json.loads(modrinth.read("modrinth.index.json"))
        assert (manifest["formatVersion"], manifest["game"]) == (1, "minecraft")
        assert manifest["dependencies"] == {"minecraft": "1.21.1", "neoforge": "21.1.172"}
        assert manifest["files"] == []
        expected_overrides = {
            "overrides/" + path.removeprefix(".minecraft/"): archive.read(path)
            for path in archive.namelist()
            if path.startswith(".minecraft/")
        }
        assert set(modrinth.namelist()) == {"modrinth.index.json", *expected_overrides}
        for path, data in expected_overrides.items():
            assert modrinth.read(path) == data
        assert [item["version"] for item in json.loads(
            archive.read("mmc-pack.json")
        )["components"]] == ["1.21.1", "21.1.172"]
        assert archive.read(".minecraft/saves/Energy-Grid/level.dat") == level_dat()
        raw = gzip.decompress(level_dat())
        assert raw.startswith(b"\x0a\x00\x00\x0a\x00\x04Data")
        assert b"minecraft:the_void" in raw and b"file/energy-grid" in raw
        assert b"lang:ru_ru" in archive.read(".minecraft/options.txt")
        assert archive.read(".minecraft/resourcepacks/Energy-Grid-RU.zip") == (
            OUTPUT / "Energy-Grid-RU.zip"
        ).read_bytes()
    functions = {
        path.stem: path.read_text()
        for path in SOURCE.joinpath("data/energogrid/function").glob("*.mcfunction")
    }
    for function in functions.values():
        for target in re.findall(r"\bfunction energogrid:(\w+)", function):
            assert target in functions, target
    book = SOURCE.joinpath("data/energogrid/loot_table/terminal_book.json").read_text()
    assert "written_book_content" in book and "loot energogrid:terminal_book" in functions["give_menu"]
    routes = set(re.findall(r"eg matches (\d+) run function", functions["terminal"]))
    choices = set(re.findall(r"/trigger eg set (\d+)", "".join(functions.values()) + book))
    assert choices <= (routes | {"1"})
    ticks = int(re.search(r"eg_run (\d+)$", functions["start"], re.M)[1])
    rate = int(re.search(r"#rate eg_state (\d+)$", functions["load"], re.M)[1])
    payout = int(re.search(r"add @s eg_bank (\d+)", functions["pay"])[1])
    cost = int(re.search(r"remove @s eg_bank (\d+)", functions["buy"])[1])
    assert ticks * rate == payout * 4000 == 20000
    assert payout - cost == 2 and 2 * payout - cost == 7
    print("OK: reproducible ZIP and MRPACK, overrides, menu routes, 5 → 2 → 7")


if __name__ == "__main__":
    verify()
