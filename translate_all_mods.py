#!/usr/bin/env python3
"""Expand Russian resources from the ACTUAL downloaded Minecraft mod jars.

Runs after the MRPACK builder: reads language namespaces directly from pinned
JARs, retains upstream ru_ru translations, fills untranslated keys from EN
with best-effort machine translation, and embeds one override resource pack.
No jar contents are modified. No arbitrary files downloaded.
"""
import argparse, io, json, re, time, urllib.request, urllib.parse, zipfile
from pathlib import Path
from scripts.translate_quests import translated, matches, chunks

NAMESPACES = {
    "mca", "townstead", "mcaconversations", "mcaquests", "mcacapitals",
    "mcacrime", "mcareputation", "blueprints", "prefab",
    "powergrid", "effortlessbuilding",
}
TARGET_FILENAMES = (
    "minecraft-comes-alive", "townstead", "mcaconversations", "mcaquests", "mcacapitals",
    "mcacrime", "mcareputation", "blueprintsmod", "prefab",
    "powergrid", "effortlessbuilding"
)
PLACEHOLDER = re.compile(r"%(?:\\d+\\$)?[-+#0,.(\\d]*[a-zA-Z%]|\\{\\d+\\}")
RESOURCE_NAME="MCA-Expanded-Russian-Language.zip"

def jar_entries(jar):
    en,ru={},{}
    for name in jar.namelist():
        parts=name.split("/")
        if len(parts)!=4 or parts[0]!="assets" or parts[2]!="lang" or parts[1] not in NAMESPACES:
            continue
        if parts[3] not in ("en_us.json","ru_ru.json"):continue
        try:
            data=json.loads(jar.read(name).decode("utf-8-sig"))
            if not isinstance(data,dict):continue
            target=en if parts[3]=="en_us.json" else ru
            target.setdefault(parts[1],{}).update({k:v for k,v in data.items() if isinstance(k,str) and isinstance(v,str)})
        except (ValueError,UnicodeError,KeyError) as exc:
            print("Skipped malformed mod locale",name,str(exc),flush=True)
    return en,ru

def safe_download(url, max_bytes=60_000_000):
    if not url.startswith("https://"):raise ValueError("Insecure JAR URL")
    req=urllib.request.Request(url,headers={"User-Agent":"MRPACK-Russian-Locale/1.0"})
    with urllib.request.urlopen(req,timeout=50) as response:
        if response.headers.get("Content-Length") and int(response.headers["Content-Length"])>max_bytes:
            raise ValueError("JAR too large")
        body=response.read(max_bytes+1)
        if len(body)>max_bytes:raise ValueError("JAR too large")
        if not body.startswith(b"PK"):raise ValueError("Non-ZIP JAR")
        return body

def valid(en,ru):
    return bool(ru.strip()) and sorted(PLACEHOLDER.findall(en))==sorted(PLACEHOLDER.findall(ru))

def main():
    parser=argparse.ArgumentParser()
    parser.add_argument("--pack",default="MCA-Ultimate-Life-Friends-1.20.1.mrpack")
    parser.add_argument("--seconds",type=int,default=185)
    args=parser.parse_args()
    pack_path=Path(args.pack)
    start=time.monotonic()
    with zipfile.ZipFile(pack_path) as archive:
        manifest=json.loads(archive.read("modrinth.index.json"))
        existing_options=archive.read("overrides/options.txt").decode("utf-8") if "overrides/options.txt" in archive.namelist() else ""
    upstream_en,upstream_ru={},{}
    for entry in manifest["files"]:
        name=entry["path"].split("/")[-1].lower()
        if not any(part in name for part in TARGET_FILENAMES):continue
        try:
            content=safe_download(entry["downloads"][0])
            with zipfile.ZipFile(io.BytesIO(content)) as jar:
                en,ru=jar_entries(jar)
            for namespace,values in en.items():upstream_en.setdefault(namespace,{}).update(values)
            for namespace,values in ru.items():upstream_ru.setdefault(namespace,{}).update(values)
            print("Extracted locales:",name, "English",sum(map(len,en.values())),"Russian",sum(map(len,ru.values())),flush=True)
        except Exception as exc:
            print("SKIPPED LOCALE:",name,str(exc)[:140],flush=True)
    # Existing MCA Quests translations from the earlier builder's resourcepack are authoritative
    # for untranslated keys; upstream Russian locales still take precedence when available.
    with zipfile.ZipFile(pack_path) as archive:
        legacy=archive.read("overrides/resourcepacks/MCA-Quests-Russian-UI.zip")
    with zipfile.ZipFile(io.BytesIO(legacy)) as archive:
        quest_values=json.loads(archive.read("assets/mcaquests/lang/ru_ru.json"))
    upstream_ru.setdefault("mcaquests",{}).update(quest_values)
    count=0
    # Dialogue first: the screenshots show unlocalized MCA Conversations lines & village screens.
    priority=("townstead","mcaconversations","mcareputation","mcacrime","mcacapitals","mca","mcaquests",
              "blueprints","prefab","effortlessbuilding","powergrid")
    for namespace in priority:
        en=upstream_en.get(namespace,{})
        ru=upstream_ru.setdefault(namespace,{})
        missing=[(k,v) for k,v in en.items() if k not in ru and v.strip()]
        missing.sort(key=lambda pair:(not any(t in pair[0].lower() for t in
                      ("dialog","greet","conversation","reput","journal","quest","title","gui")),pair[0]))
        for batch in chunks(missing,800):
            if time.monotonic()-start>args.seconds:break
            text="🟩".join(v.replace("\\n"," ⏎ ") for _,v in batch)
            try:
                translation=translated(text)
                pieces=translation.split("🟩")
                if len(pieces)!=len(batch):continue
                for (key,original),value in zip(batch,pieces):
                    value=value.strip().replace(" ⏎ ","\\n")
                    if valid(original,value) and re.search("[А-Яа-яЁё]",value):
                        ru[key]=value;count+=1
            except Exception as exc:
                print("Translation batch issue:",str(exc)[:100],flush=True)
        print("Namespace",namespace,"keys",len(ru),"missing",sum(k not in ru for k in en),flush=True)
    mem=io.BytesIO()
    with zipfile.ZipFile(mem,"w",zipfile.ZIP_DEFLATED) as res:
        res.writestr("pack.mcmeta",json.dumps({"pack":{"pack_format":15,
             "description":"MCA Ultimate RU: доперевод интерфейса и диалогов"}} ,ensure_ascii=False))
        for namespace,values in upstream_ru.items():
            if values:
                res.writestr("assets/"+namespace+"/lang/ru_ru.json",json.dumps(values,ensure_ascii=False,indent=2))
    options=existing_options
    options=options.replace('resourcePacks:["vanilla","file/MCA-Quests-Russian-UI.zip"]',
        'resourcePacks:["vanilla","file/MCA-Quests-Russian-UI.zip","file/'+RESOURCE_NAME+'"]')
    if RESOURCE_NAME not in options:
        options+='\\nresourcePacks:["vanilla","file/'+RESOURCE_NAME+'"]\\n'
    tmp=pack_path.with_suffix(".tmp")
    with zipfile.ZipFile(pack_path,"r") as source,zipfile.ZipFile(tmp,"w",zipfile.ZIP_DEFLATED) as destination:
        for entry in source.infolist():
            if entry.filename in ("overrides/options.txt","overrides/resourcepacks/"+RESOURCE_NAME):
                continue
            destination.writestr(entry,source.read(entry.filename))
        destination.writestr("overrides/resourcepacks/"+RESOURCE_NAME,mem.getvalue())
        destination.writestr("overrides/options.txt",options)
    tmp.replace(pack_path)
    print("COMPLETED LOCALE PACK namespaces",len(upstream_ru),"machine keys",count,
          "total Russian keys",sum(map(len,upstream_ru.values())),flush=True)

if __name__=="__main__":main()
