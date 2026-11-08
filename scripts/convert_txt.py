#!/usr/bin/env python3
"""Turn a plain-text file containing Java statements into a Fabric Java source."""
from pathlib import Path

source = Path("mod.txt")
if not source.is_file():
    raise SystemExit("ERROR: mod.txt not found. Upload mod.txt to the repository root.")

snippet = source.read_text(encoding="utf-8-sig").strip()
if not snippet:
    raise SystemExit("ERROR: mod.txt is empty.")

# mod.txt contains statements INSIDE initialize(), not a JS file or entire class.
out = Path("src/main/java/dev/miniezi/safetools/TxtMod.java")
out.parent.mkdir(parents=True, exist_ok=True)
out.write_text(
    "package dev.miniezi.safetools;\n\n"
    "public final class TxtMod {\n"
    "    private TxtMod() {}\n"
    "    public static void initialize() {\n"
    + "\n".join("        " + line for line in snippet.splitlines())
    + "\n    }\n}\n",
    encoding="utf-8",
)
print(f"Generated {out} from mod.txt; compiling with Gradle...")
