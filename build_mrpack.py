#!/usr/bin/env python3
"""MRPACK BUILDER (not a ready-made modpack). Uses live Modrinth metadata."""
import argparse, json, sys, urllib.parse, urllib.request, zipfile, hashlib, io, os
from pathlib import Path

MINECRAFT="1.20.1"
FORGE="47.4.10"
NAME="MCA-Ultimate-Life-Friends-1.20.1.mrpack"
BASE="https://api.modrinth.com/v2"
CORE=["minecraft-comes-alive-reborn","mca-conversations","mca-quests","architectury-api","patchouli","geckolib","capitals-a-monarchy-mod-for-mca-reborn","paxi","power-grid"]
EXTRAS=[
 "farmers-delight","handcrafted","supplementaries","serene-seasons","waystones","jade","jei",
 "xaeros-minimap","xaeros-world-map","embeddium","bountiful","ftb-quests","ftb-library","ftb-teams",
 "chipped","another-furniture","decorative-blocks","macaws-doors","macaws-windows",
 "macaws-roofs","macaws-furniture","macaws-bridges","macaws-fences-and-walls",
 "comforts","carry-on","guard-villagers","naturalist",
 "yungs-better-villages","repurposed-structures","towns-and-towers",
 "lets-do-vinery","lets-do-bakery","lets-do-farm-charm",
 "create","create-deco","create-steam-n-rails",
 "lootr","corpse","appleskin","ferrite-core","modernfix","entityculling","spark",
 "immersive-engineering","create-new-age","createaddition","immersive-aircraft",
 "create-bells-and-whistles","create-stuff-additions","create-enchantment-industry",
 "create-ore-excavation","create-diesel-generators","create-industry",
 "engineers-decor","industrial-foregoing",
 "oculus","sound-physics-remastered","ambient-sounds","fallingleaves","better-third-person",
 "ct-overhaul-village","prefab","effortless-building","prefabricated-structures"
]

def api(path):
    req=urllib.request.Request(BASE+path,headers={"User-Agent":"MCA-MRPACK-Builder/1.0","Accept":"application/json"})
    with urllib.request.urlopen(req,timeout=35) as response:return json.load(response)

def choose(project,townstead=False):
    query=urllib.parse.urlencode({"loaders":json.dumps(["forge"]),"game_versions":json.dumps([MINECRAFT])})
    versions=api("/project/"+urllib.parse.quote(project,safe="")+"/version?"+query)
    versions=[v for v in versions if (v.get("version_type")=="release" or project in ("capitals-a-monarchy-mod-for-mca-reborn","minecraft-comes-alive-reborn")) and "forge" in v.get("loaders",[]) and MINECRAFT in v.get("game_versions",[])]
    if project=="capitals-a-monarchy-mod-for-mca-reborn":
        # Select newest published Forge release on Minecraft 1.20.1.
        # The resolver rejects any dependency conflict with MCA versions.
        import re
        def vers(v):
            m=re.search(r"(\\d+)\\.(\\d+)\\.(\\d+)",v.get("version_number",""))
            return tuple(map(int,m.groups())) if m else (0,0,0)
        versions=[v for v in versions if vers(v)>=(1,3,5)]
        if not versions:
            # Older but published Modrinth 1.3.5 release is not returned by
            # some filtered version listings; use its exact confirmed version ID.
            fallback=api("/version/cE7zt4nS")
            if MINECRAFT not in fallback.get("game_versions",[]) or "forge" not in fallback.get("loaders",[]):
                raise ValueError("No compatible MCA Capitals release found")
            versions=[fallback]
    if project=="minecraft-comes-alive-reborn" and townstead:
        pinned=[v for v in versions if v.get("version_number","").startswith("7.6.26")]
        if not pinned:raise ValueError("Townstead mode requires MCA 7.6.26")
        return max(pinned,key=lambda v:v.get("date_published",""))
    if project=="minecraft-comes-alive-reborn":
        # Prefer current release line, but dependency resolution will enforce
        # the exact MCA version specified by MCA Capitals.
        versions=[v for v in versions if v.get("version_number","").startswith("7.7.")]
    if not versions:raise ValueError("No Forge 1.20.1 release: "+project)
    return max(versions,key=lambda v:v.get("date_published",""))

def build(output,lean=False,strict_extras=False,townstead=False):
    # Canonical Modrinth project IDs prevent slug/id aliases from double-installing mods.
    selected={}; pending=[]; logs=[]; incompat=[]; aliases={}
    def add(project,required=True,version=None):
        try:
            version=version or choose(project,townstead)
            if MINECRAFT not in version.get("game_versions",[]) or "forge" not in version.get("loaders",[]):
                raise ValueError("Incompatible game version or loader")
            project_id=version.get("project_id")
            if not project_id:raise ValueError("Version has no canonical project_id")
            aliases[project]=project_id
            if project_id in selected:
                previous=selected[project_id][0]
                if previous["id"]!=version["id"]:
                    raise ValueError("Version conflict for "+project+": "+previous.get("version_number","")+" vs "+version.get("version_number",""))
                return
            files=[f for f in version["files"] if f["filename"].endswith(".jar") and f.get("hashes",{}).get("sha1") and f.get("hashes",{}).get("sha512")]
            if not files:raise ValueError("No hashed JAR")
            file=next((f for f in files if f.get("primary")),files[0])
            if not file["url"].startswith("https://"):raise ValueError("Non-HTTPS file")
            selected[project_id]=(version,file)
            pending.append(project_id)
            logs.append(project+" -> "+version["version_number"])
        except Exception as exc:
            if required or strict_extras:raise ValueError(project+": "+str(exc)) from exc
            logs.append("SKIPPED "+project+": "+str(exc))
    projects=[p for p in CORE if p!="capitals-a-monarchy-mod-for-mca-reborn"]+["townstead"] if townstead else CORE
    # Add Capitals before MCA so its exact dependency selects the required MCA build.
    if not townstead:projects=["capitals-a-monarchy-mod-for-mca-reborn"]+[p for p in projects if p!="capitals-a-monarchy-mod-for-mca-reborn"]
    for project in projects:
        if project=="minecraft-comes-alive-reborn" and not townstead:
            if "1W98a849" in selected:continue
            # Pin MCA to the actual MCA version requested by the selected Capitals release.
            capitals=next((v for v,_ in selected.values() if any("mcacapitals" in f["filename"].lower() for f in v["files"])),None)
            if capitals:
                dep=next((d for d in capitals.get("dependencies",[]) if d.get("project_id")=="1W98a849" and d.get("version_id") and d.get("dependency_type")=="required"),None)
                if dep:
                    add(project,True,api("/version/"+dep["version_id"]))
                    continue
        add(project)
    if not lean:
        for project in EXTRAS:add(project,False)
    while pending:
        project=pending.pop(0)
        version=selected[project][0]
        for dep in version.get("dependencies",[]):
            typ=dep.get("dependency_type")
            if typ=="incompatible":
                incompat.append((project,dep.get("project_id"),dep.get("version_id")))
                continue
            if typ!="required":continue
            vid=dep.get("version_id")
            pinned=api("/version/"+urllib.parse.quote(vid,safe="")) if vid else None
            dependency=dep.get("project_id") or (pinned and pinned.get("project_id"))
            if not dependency:raise ValueError("Missing dependency ID of "+project)
            if pinned and (MINECRAFT not in pinned.get("game_versions",[]) or "forge" not in pinned.get("loaders",[])):
                raise ValueError("Incompatible pinned dependency of "+project)
            # MCA Quests 1.7.x and Conversations 1.8.x officially support MCA [7.6,8).
            # Some Modrinth version metadata still pins the dev MCA 7.6.26 version.
            # Honor the declared compatibility range for ONLY these two projects.
            parent_filename=selected[project][1]["filename"].lower()
            if not townstead and dependency=="1W98a849" and (parent_filename.startswith("mcaquests-") or parent_filename.startswith("mcaconversations-")):
                installed=selected.get("1W98a849")
                if installed and installed[0].get("version_number","").startswith("7.7.1"):
                    logs.append("ACCEPTED supported MCA [7.6,8) for "+parent_filename+
                                " instead of stale Modrinth pinned dev version")
                    continue
            add(dependency,True,pinned)
    for parent,project,vid in incompat:
        canonical=aliases.get(project,project)
        if canonical in selected and (not vid or selected[canonical][0]["id"]==vid):
            raise ValueError(parent+" incompatible with "+project)
    result=[];paths=set()
    for project,(version,file) in selected.items():
        fname=file["filename"]
        if fname!=Path(fname).name:raise ValueError("Unsafe JAR filename")
        path="mods/"+fname
        if path in paths:
            # Modrinth can identify the same dependency both by slug and by project ID.
            previous=next((e for e in result if e["path"]==path),None)
            if previous and previous["hashes"]["sha512"]==file["hashes"]["sha512"]:
                continue
            raise ValueError("Filename conflict with different content: "+fname)
        paths.add(path)
        sides=api("/project/"+urllib.parse.quote(project,safe=""))
        entry={"path":path,"hashes":{"sha1":file["hashes"]["sha1"],"sha512":file["hashes"]["sha512"]},"downloads":[file["url"]],"fileSize":file["size"]}
        if sides.get("server_side")=="unsupported":entry["env"]={"client":"required","server":"unsupported"}
        if sides.get("client_side")=="unsupported":entry["env"]={"client":"unsupported","server":"required"}
        # Some Modrinth projects label purely client UI mods as server-optional.
        # A dedicated server should not install UI, minimap or recipe viewer JARs.
        client_only={"embeddium","entityculling","xaeros-minimap","xaeros-world-map","jei","appleskin"}
        if project in client_only or sides.get("slug") in client_only:
            entry["env"]={"client":"required","server":"unsupported"}
        result.append(entry)
    # MCA: Crime GPL-3.0 release, sourced from the author's official GitHub Releases.
    # Download at build time so sha1/sha512 in the .mrpack are verified, never guessed.
    crime_url="https://github.com/otectus/MCACrime/releases/download/forge/1.20.1/v0.7.8/mcacrime-0.7.8-forge-1.20.1.jar"
    try:
        req=urllib.request.Request(crime_url,headers={"User-Agent":"MCA-MRPACK-Builder/1.0"})
        with urllib.request.urlopen(req,timeout=90) as response: crime_bytes=response.read()
        if not crime_bytes.startswith(b"PK") or len(crime_bytes)<10000:
            raise ValueError("Downloaded file is not a valid mod archive")
        crime_name="mods/mcacrime-0.7.8-forge-1.20.1.jar"
        if crime_name in paths:raise ValueError("Duplicate MCA Crime archive")
        result.append({"path":crime_name,
          "hashes":{"sha1":hashlib.sha1(crime_bytes).hexdigest(),"sha512":hashlib.sha512(crime_bytes).hexdigest()},
          "downloads":[crime_url],"fileSize":len(crime_bytes)})
        logs.append("MCA Crime -> 0.7.8 (author GitHub Release)")
    except Exception as exc:
        if strict_extras:raise
        logs.append("SKIPPED MCA Crime: "+str(exc))
    # MCA: Reputation 0.6.1 GPL-3.0 from the CurseForge CDN for published file 9023819.
    # Only include if the real archive can be retrieved and checksummed at build time.
    reputation_url="https://mediafilez.forgecdn.net/files/9023/819/mcareputation-0.6.1.jar"
    try:
        req=urllib.request.Request(reputation_url,headers={"User-Agent":"Mozilla/5.0 MCA-MRPACK-Builder"})
        with urllib.request.urlopen(req,timeout=35) as response: reputation_bytes=response.read()
        if not reputation_bytes.startswith(b"PK") or len(reputation_bytes)<10000:
            raise ValueError("Invalid Reputation jar")
        result.append({"path":"mods/mcareputation-0.6.1.jar",
          "hashes":{"sha1":hashlib.sha1(reputation_bytes).hexdigest(),"sha512":hashlib.sha512(reputation_bytes).hexdigest()},
          "downloads":[reputation_url],"fileSize":len(reputation_bytes)})
        logs.append("MCA Reputation -> 0.6.1 (CurseForge file 9023819)")
    except Exception as exc:
        if strict_extras:raise
        logs.append("SKIPPED MCA Reputation: "+str(exc))
    # Immersive Engineering 1.20.1-10.2.0-183, CurseForge official file #6206989.
    # Resolve and hash the real archive at build time (do not commit it into Git).
    ie_url="https://mediafilez.forgecdn.net/files/6206/989/ImmersiveEngineering-1.20.1-10.2.0-183.jar"
    try:
        req=urllib.request.Request(ie_url,headers={"User-Agent":"Mozilla/5.0 MCA-MRPACK-Builder"})
        with urllib.request.urlopen(req,timeout=90) as response: ie_bytes=response.read()
        if not ie_bytes.startswith(b"PK") or len(ie_bytes)<1000000:
            raise ValueError("Immersive Engineering JAR could not be validated")
        result.append({"path":"mods/ImmersiveEngineering-1.20.1-10.2.0-183.jar",
          "hashes":{"sha1":hashlib.sha1(ie_bytes).hexdigest(),"sha512":hashlib.sha512(ie_bytes).hexdigest()},
          "downloads":[ie_url],"fileSize":len(ie_bytes)})
        logs.append("Immersive Engineering -> 10.2.0-183 (CurseForge file 6206989)")
    except Exception as exc:
        # Industry preset is incomplete without its namesake mod.
        raise RuntimeError("Immersive Engineering must be downloadable: "+str(exc)) from exc
    mca_versions=[(project,v.get("version_number",""),f.get("filename",""))
                  for project,(v,f) in selected.items()
                  if "minecraft-comes-alive" in f.get("filename","").lower()]
    if len(mca_versions)!=1 or (townstead and not mca_versions[0][1].startswith("7.6.26")):
        raise RuntimeError("MCA Capitals 1.3.5 requires MCA 7.7.1: "+repr(mca_versions))
    if not townstead and any("townstead" in f["path"].lower() for f in result):
        raise RuntimeError("Townstead 0.7.6 cannot load with MCA 7.7.1")
    if townstead:
        if not any("townstead-0.7.6" in f["path"].lower() for f in result):
            raise RuntimeError("Townstead 0.7.6 missing from Townstead profile")
        if any("mcacapitals" in f["path"].lower() for f in result):
            raise RuntimeError("MCA Capitals cannot join MCA 7.6.26 Townstead profile")
    index={"formatVersion":1,"game":"minecraft","versionId":"1.0.0","name":"MCA Ultimate Life — Friends Edition","summary":"Produced by MRPACK Builder; multiplayer requires testing","files":sorted(result,key=lambda f:f["path"]),"dependencies":{"minecraft":MINECRAFT,"forge":FORGE}}
    with zipfile.ZipFile(output,"w",zipfile.ZIP_DEFLATED) as archive:
        archive.writestr("modrinth.index.json",json.dumps(index,indent=2,ensure_ascii=False))
        archive.writestr("overrides/README.txt", """MCA INDUSTRIAL VILLAGES RU — ATMOSPHERE UPDATE

Minecraft 1.20.1 / Forge 47.4.10 / Prism Launcher.

VISUAL SETUP
1. Start the instance once. Configure Video Settings > Shaders if Oculus
   was installed. Shaderpacks are NOT bundled; without shaders this pack
   remains fully playable.
2. Low/medium GPU: keep shaders off, use moderate render distance and
   lower particles before adding visual effects.
3. Strong GPU: enable a shaderpack separately and raise graphics settings
   gradually. Check RAM use and FPS before entering old worlds.
4. Optional Sound Physics Remastered adds reverb and positional ambience;
   adjust volumes in Sounds to avoid overwhelming village conversations.
5. Better Third Person provides a clearer camera for walking around towns.

GAMEPLAY SETUP
1. Create machines form the workshop backbone.
2. Immersive Engineering generators can feed FE-compatible devices.
3. Create Power Grid wires use a dedicated FE inverter for cross-mod
   electricity; directly joining two unlike cables does not work.
4. MCA conversations, quests, village projects and Capitals are included.
5. Chipped is deliberately excluded from custom Russian translation.

BACKUPS
Use a NEW Prism instance and BACK UP worlds before replacing a pack.
Not every optional visual mod is available on every mod platform; see
the .mod-list.txt build log for exact installed versions.
The metadata and Java addon are checked in CI, but the game has not been
fully run or stress-tested with the new visuals.
""")
    with zipfile.ZipFile(output,"a",zipfile.ZIP_DEFLATED) as archive:
        ru=Path(__file__).parent/"localization"/"mcaquests_ru_ru.json"
        localization=json.loads(ru.read_text(encoding="utf-8"))
        generated=Path(__file__).parent/"localization"/"mcaquests_ru_ru_generated.json"
        if generated.exists():
            translations=json.loads(generated.read_text(encoding="utf-8"))
            translations.update(localization)  # manually reviewed strings win
            localization=translations
        print("Quest localization:",len(localization),"Russian strings")
        memory=io.BytesIO()
        with zipfile.ZipFile(memory,"w",zipfile.ZIP_DEFLATED) as pack:
            pack.writestr("pack.mcmeta",json.dumps({"pack":{"pack_format":15,"description":"MCA Quests Russian partial translation"}},ensure_ascii=False))
            pack.writestr("assets/mcaquests/lang/ru_ru.json",json.dumps(localization,ensure_ascii=False,indent=2))
        archive.writestr("overrides/resourcepacks/MCA-Quests-Russian-UI.zip",memory.getvalue())
        # Bundled custom addon is our own open source code compiled in GitHub CI.
        # Include a local override JAR instead of pretending it is hosted on Modrinth.
        addon_dir=Path(__file__).parent/"addons"/"worker-chests"/"build"/"libs"
        addon_jars=sorted(addon_dir.glob("mca-worker-chests-*.jar")) if addon_dir.exists() else []
        addon_jars=[p for p in addon_jars if not any(x in p.name for x in ("-sources","-dev","-javadoc"))]
        if os.environ.get("MRPACK_REQUIRE_WORKER_CHESTS")=="1" and len(addon_jars)!=1:
            raise RuntimeError("Missing compiled worker-chests JAR; run gradle build first")
        if len(addon_jars)==1:
            archive.writestr("overrides/mods/"+addon_jars[0].name,addon_jars[0].read_bytes())
            print("Bundled custom addon:",addon_jars[0].name)
        elif addon_jars:
            raise RuntimeError("More than one worker-chests JAR found")

        # Paxi loads community village project datapacks into every world.
        project_directory=Path(__file__).parent/"datapacks"/"mcavillage"/"data"/"mcavillage"/"mcaquests"/"projects"
        if not project_directory.exists():raise RuntimeError("Missing village project definitions")
        required_projects={"festival","granary","industrial_revolution","watch","street_lights","public_library","trade_routes","emergency_clinic"}
        found_projects={p.stem for p in project_directory.glob("*.json")}
        if not required_projects.issubset(found_projects):
            raise RuntimeError("Missing village projects: "+str(required_projects-found_projects))
        project_memory=io.BytesIO()
        with zipfile.ZipFile(project_memory,"w",zipfile.ZIP_DEFLATED) as datapack:
            datapack.writestr("pack.mcmeta",json.dumps({"pack":{"pack_format":15,"description":"MCA: совместные проекты деревни"}} ,ensure_ascii=False))
            for project_file in sorted(project_directory.glob("*.json")):
                project_data=json.loads(project_file.read_text(encoding="utf-8"))
                if project_data.get("scope")!="village" or not project_data.get("phases"):
                    raise RuntimeError("Invalid shared village project: "+project_file.name)
                datapack.writestr("data/mcavillage/mcaquests/projects/"+project_file.name,json.dumps(project_data,ensure_ascii=False,indent=2))
            # Visible Russian progression tab for ordinary survival gameplay.
            advancements_dir=Path(__file__).parent/"datapacks"/"mcavillage"/"data"/"mcavillage"/"advancements"
            required_advancements={"root","food","market","workshop","electricity","agriculture","livestock","hospitality","education","defense","infrastructure","automation","illumination","navigation","industry","prestige","harvest"}
            present={p.stem for p in advancements_dir.glob("*.json")}
            if not required_advancements.issubset(present):
                raise RuntimeError("Missing village progression milestones: "+str(required_advancements-present))
            for advancement_file in sorted(advancements_dir.glob("*.json")):
                advancement=json.loads(advancement_file.read_text(encoding="utf-8"))
                if not advancement.get("criteria") or not advancement.get("display"):
                    raise RuntimeError("Invalid advancement "+advancement_file.name)
                datapack.writestr("data/mcavillage/advancements/"+advancement_file.name,
                                  json.dumps(advancement,ensure_ascii=False,indent=2))
        archive.writestr("overrides/config/paxi/datapacks/MCA-Shared-Village-Projects-RU.zip",project_memory.getvalue())

        archive.writestr("overrides/options.txt",'lang:ru_ru\nresourcePacks:["vanilla","file/MCA-Quests-Russian-UI.zip"]\n')
    Path(output).with_suffix(".mod-list.txt").write_text("\n".join(logs)+"\n",encoding="utf-8")
    print("Generated:",output,"entries:",len(result))
    print("\n".join(logs))

if __name__=="__main__":
    p=argparse.ArgumentParser(description="Generate an MRPACK from Modrinth")
    p.add_argument("--output",default=NAME)
    p.add_argument("--lean",action="store_true")
    p.add_argument("--townstead",action="store_true",help="MCA 7.6.26 + Townstead 0.7.6, without MCA Capitals")
    p.add_argument("--strict-extras",action="store_true")
    args=p.parse_args()
    try:build(args.output,args.lean,args.strict_extras,args.townstead)
    except Exception as exc:print("BUILD FAILED:",exc,file=sys.stderr);sys.exit(1)
