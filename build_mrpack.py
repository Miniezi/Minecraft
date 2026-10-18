#!/usr/bin/env python3
"""MRPACK BUILDER (not a ready-made modpack). Uses live Modrinth metadata."""
import argparse, json, sys, urllib.parse, urllib.request, zipfile, hashlib
from pathlib import Path

MINECRAFT="1.20.1"
FORGE="47.4.10"
NAME="MCA-Ultimate-Life-Friends-1.20.1.mrpack"
BASE="https://api.modrinth.com/v2"
CORE=["minecraft-comes-alive-reborn","mca-conversations","mca-quests","townstead","architectury-api"]
EXTRAS=["farmers-delight","handcrafted","supplementaries","serene-seasons","waystones","jade","jei","xaeros-minimap","xaeros-world-map","embeddium"]

def api(path):
    req=urllib.request.Request(BASE+path,headers={"User-Agent":"MCA-MRPACK-Builder/1.0","Accept":"application/json"})
    with urllib.request.urlopen(req,timeout=35) as response:return json.load(response)

def choose(project):
    query=urllib.parse.urlencode({"loaders":json.dumps(["forge"]),"game_versions":json.dumps([MINECRAFT])})
    versions=api("/project/"+urllib.parse.quote(project,safe="")+"/version?"+query)
    versions=[v for v in versions if v.get("version_type")=="release" and "forge" in v.get("loaders",[]) and MINECRAFT in v.get("game_versions",[])]
    if project=="minecraft-comes-alive-reborn":
        pinned=[v for v in versions if "7.6.26" in v.get("version_number","")]
        if pinned:versions=pinned
        else:
            stable=[v for v in versions if v.get("version_number","").startswith("7.6.")]
            if stable:versions=stable
    if not versions:raise ValueError("No Forge 1.20.1 release: "+project)
    return max(versions,key=lambda v:v.get("date_published",""))

def build(output,lean=False,strict_extras=False):
    selected={}; pending=[]; logs=[]; incompat=[]
    def add(project,required=True,version=None):
        if project in selected:
            if version and version["id"]!=selected[project][0]["id"]:raise ValueError("Conflicting required versions: "+project)
            return
        try:
            version=version or choose(project)
            files=[f for f in version["files"] if f["filename"].endswith(".jar") and f.get("hashes",{}).get("sha1") and f.get("hashes",{}).get("sha512")]
            if not files:raise ValueError("No hashed JAR")
            file=next((f for f in files if f.get("primary")),files[0])
            if not file["url"].startswith("https://"):raise ValueError("Non-HTTPS file")
            selected[project]=(version,file)
            pending.append(project)
            logs.append(project+" -> "+version["version_number"])
        except Exception as exc:
            if required or strict_extras:raise ValueError(project+": "+str(exc)) from exc
            logs.append("SKIPPED "+project+": "+str(exc))
    for project in CORE:add(project)
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
            add(dependency,True,pinned)
    for parent,project,vid in incompat:
        if project in selected and (not vid or selected[project][0]["id"]==vid):
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
        result.append(entry)
    index={"formatVersion":1,"game":"minecraft","versionId":"1.0.0","name":"MCA Ultimate Life — Friends Edition","summary":"Produced by MRPACK Builder; multiplayer requires testing","files":sorted(result,key=lambda f:f["path"]),"dependencies":{"minecraft":MINECRAFT,"forge":FORGE}}
    with zipfile.ZipFile(output,"w",zipfile.ZIP_DEFLATED) as archive:
        archive.writestr("modrinth.index.json",json.dumps(index,indent=2,ensure_ascii=False))
        archive.writestr("overrides/README.txt","This is generated output from MRPACK Builder. Test with two players and a dedicated Forge server before long-term use.\n")
    Path(output).with_suffix(".mod-list.txt").write_text("\n".join(logs)+"\n",encoding="utf-8")
    print("Generated:",output,"entries:",len(result))
    print("\n".join(logs))

if __name__=="__main__":
    p=argparse.ArgumentParser(description="Generate an MRPACK from Modrinth")
    p.add_argument("--output",default=NAME)
    p.add_argument("--lean",action="store_true")
    p.add_argument("--strict-extras",action="store_true")
    args=p.parse_args()
    try:build(args.output,args.lean,args.strict_extras)
    except Exception as exc:print("BUILD FAILED:",exc,file=sys.stderr);sys.exit(1)
