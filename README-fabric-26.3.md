# Minecraft Safe Tools — Fabric 26.3

## How to build a mod by uploading only mod.txt

1. Switch GitHub to branch `fabric-26.3-safe-mod`.
2. Open the root-level `mod.txt`, click Edit (pencil), paste **Java statements** and commit.
   You can also upload a replacement file with the exact filename `mod.txt`.
3. On every push, GitHub Actions converts the text into `TxtMod.java` and runs Gradle.
4. Open **Actions → Build Fabric 26.3 mod**. After a successful run, download the `minecraft-safe-tools-fabric-26.3` artifact. Extract the compiled JAR and install it in `mods/`.

The text is inserted into the Java method `TxtMod.initialize()`, which runs when Fabric initializes this mod.

Example contents of mod.txt:

```java
System.out.println("[Minecraft Safe Tools] My code is running!");
```

This **only accepts valid Java statements** (Java 25) compatible with Fabric 26.3. It is not a JavaScript-to-Java converter and won't turn the previously uploaded Node.js proxy into a mod. Imports and entire Java classes are not supported in mod.txt; use fully qualified Java class names when necessary. Compile errors are shown in the GitHub Actions logs.

## Configuration

- Minecraft Java: 26.3
- Fabric Loader: 0.19.5+
- Java: 25
- Build: Gradle 9.6.0 via GitHub Actions
- Test command: `/modstatus`

Only run trusted Java code; code in mod.txt becomes executable code in your mod. Credential theft, protection bypass, IP spoofing and bot flooding are not part of this project.

## Note

The GitHub Actions build validates compilation, not behavior in-game.
