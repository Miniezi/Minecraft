# Minecraft Safe Tools — Fabric 26.3

This is a safe Java/Fabric starter project, not a direct translation of the supplied Node.js proxy.

## Build
Install JDK 25 and Gradle 9.6.0, then run `gradle clean build`.
Alternatively open **Actions → Build Fabric 26.3 mod → Run workflow** to build and download the JAR artifact.

## Installation
Install Fabric Loader 0.19.5+ for Minecraft Java 26.3 and Fabric API for 26.3.
Copy the built JAR from `build/libs/` into the `mods/` folder.

## Feature
Use `/modstatus` in a world/server to verify installation.

Credential interception, IP spoofing, anticheat bypass, bot flooding and unauthorized operator execution from the supplied file are intentionally not included.

Note: Building still needs to succeed in GitHub Actions; this project has not been runtime-tested.
