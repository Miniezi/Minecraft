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
SKIP_NAMESPACES={"chipped"}
SKIP_JARS=("chipped-",)

def jar_entries(jar):
    en,ru={},{}
    for name in jar.namelist():
        parts=name.split("/")
        if len(parts)!=4 or parts[0]!="assets" or parts[2]!="lang" or (parts[1] in SKIP_NAMESPACES or not re.fullmatch(r"[a-z0-9_.-]+",parts[1])):
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


# Protect formatting, interpolation, and non-language syntax from machine translation.
TOKEN = re.compile(
    r"%(?:\d+\$)?[-+#0,.(\d]*[a-zA-Z%]"
    r"|\{\{?[\w.:-]+\}?\}"
    r"|§[0-9a-fk-or]"
    r"|&[0-9a-fk-or]"
    r"|<[/!]?[A-Za-z][^<>]{0,120}>"
    r"|\\[nrt]"
    r"|\$\{[^{}]{1,120}\}"
    r"|\[[A-Za-z_]+:[^\]]+\]"
)
CYRILLIC=re.compile(r"[А-Яа-яЁё]")
def protected_tokens(value):
    return sorted(TOKEN.findall(value))

def complex_valid(source, value):
    return (bool(value.strip()) and protected_tokens(source)==protected_tokens(value)
            and bool(CYRILLIC.search(value)) and len(value)<max(150, len(source)*7))

def translate_safe(text):
    """Mask fragile tokens, then check exact token inventory on return."""
    tokens=[]
    def mask(m):
        tokens.append(m.group())
        return "ZXQPROTECT"+str(len(tokens)-1)+"QXZ"
    masked=TOKEN.sub(mask,text)
    result=translated(masked)
    for i, original in enumerate(tokens):
        marker="ZXQPROTECT"+str(i)+"QXZ"
        if result.count(marker)!=1:raise ValueError("Masked placeholder was altered")
        result=result.replace(marker,original)
    if not complex_valid(text,result):raise ValueError("Invalid translated string")
    return result

def translate_resilient(batch, deadline, counters, depth=0):
    """Try batching, then bisect failures, finally translate individually.

    A changed batch separator or placeholder must never corrupt other keys.
    """
    if time.monotonic()>deadline:return {}
    if len(batch)==1:
        key,value=batch[0]
        try:
            counters["individual_attempts"]+=1
            return {key:translate_safe(value)}
        except Exception:
            counters["individual_failures"]+=1
            return {}
    # Batch only plain text: complex markup needs single-key masking.
    if any(TOKEN.search(value) for _,value in batch):
        middle=len(batch)//2
        result=translate_resilient(batch[:middle],deadline,counters,depth+1)
        result.update(translate_resilient(batch[middle:],deadline,counters,depth+1))
        return result
    separator=" 🟩 "
    try:
        counters["batch_attempts"]+=1
        output=translated(separator.join(value.replace("\n"," ⏎ ") for _,value in batch))
        parts=output.split("🟩")
        if len(parts)!=len(batch):raise ValueError("Changed translation separators")
        translated_values={}
        invalid=[]
        for (key,source),part in zip(batch,parts):
            target=part.strip().replace(" ⏎ ","\n")
            if complex_valid(source,target):translated_values[key]=target
            else:invalid.append((key,source))
        for pair in invalid:
            translated_values.update(translate_resilient([pair],deadline,counters,depth+1))
        return translated_values
    except Exception:
        counters["batch_failures"]+=1
        middle=len(batch)//2
        left=translate_resilient(batch[:middle],deadline,counters,depth+1)
        left.update(translate_resilient(batch[middle:],deadline,counters,depth+1))
        return left

def main():
    parser=argparse.ArgumentParser()
    parser.add_argument("--pack",default="MCA-Ultimate-Life-Friends-1.20.1.mrpack")
    parser.add_argument("--seconds",type=int,default=185)
    parser.add_argument("--skip-existing-russian",action="store_true",help="Only translate keys not already provided by the mods")
    args=parser.parse_args()
    pack_path=Path(args.pack)
    start=time.monotonic()
    with zipfile.ZipFile(pack_path) as archive:
        manifest=json.loads(archive.read("modrinth.index.json"))
        existing_options=archive.read("overrides/options.txt").decode("utf-8") if "overrides/options.txt" in archive.namelist() else ""
    upstream_en,upstream_ru={},{}
    for entry in manifest["files"]:
        name=entry["path"].split("/")[-1].lower()
        if not name.endswith(".jar") or any(name.startswith(prefix) for prefix in SKIP_JARS):continue
        try:
            content=safe_download(entry["downloads"][0],max_bytes=110_000_000)
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
    # Preserve successfully translated keys from a previous pass of this same MRPACK.
    # Additional CI passes can focus on the remaining untranslated strings.
    with zipfile.ZipFile(pack_path) as archive:
        existing="overrides/resourcepacks/"+RESOURCE_NAME
        if existing in archive.namelist():
            with zipfile.ZipFile(io.BytesIO(archive.read(existing))) as previous:
                for name in previous.namelist():
                    parts=name.split("/")
                    if len(parts)==4 and parts[0]=="assets" and parts[2]=="lang" and parts[3]=="ru_ru.json" and parts[1] not in SKIP_NAMESPACES:
                        try:
                            previous_values=json.loads(previous.read(name))
                            upstream_ru.setdefault(parts[1],{}).update(previous_values)
                        except (ValueError,KeyError):
                            pass
    for namespace in SKIP_NAMESPACES:
        upstream_en.pop(namespace,None)
        upstream_ru.pop(namespace,None)
    count=0
    counters={"batch_attempts":0,"batch_failures":0,"individual_attempts":0,"individual_failures":0}
    deadline=start+args.seconds
    priority=("mcaconversations","mcareputation","mcacrime","mcacapitals","mca","mcaquests",
              "powergrid","create","immersiveengineering","prefab","effortlessbuilding")
    priority=priority+tuple(sorted(ns for ns in upstream_en if ns not in priority))
    for namespace in priority:
        en=upstream_en.get(namespace,{})
        ru=upstream_ru.setdefault(namespace,{})
        missing=[(k,v) for k,v in en.items() if k not in ru and v.strip()]
        missing.sort(key=lambda pair:(not any(t in pair[0].lower() for t in
                      ("dialog","greet","conversation","reput","journal","quest","title","gui")),pair[0]))
        for batch in chunks(missing,900):
            if time.monotonic()>deadline:break
            results=translate_resilient(batch,deadline,counters)
            ru.update(results)
            count+=len(results)
        print("Namespace",namespace,"keys",len(ru),"missing",sum(k not in ru for k in en),flush=True)
    print("TRANSLATOR DIAGNOSTICS",json.dumps(counters),flush=True)
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
    coverage={ns:{"english":len(values),"russian":sum(k in upstream_ru.get(ns,{}) for k in values),"missing":sum(k not in upstream_ru.get(ns,{}) for k in values)}
              for ns,values in upstream_en.items()}
    missing_total=sum(stat["missing"] for stat in coverage.values())
    Path("localization-coverage.json").write_text(json.dumps({"mod_files":len(manifest["files"]),"english_namespaces":len(upstream_en),"untranslated_keys":missing_total,"russian_keys":sum(map(len,upstream_ru.values())),"per_namespace":coverage},indent=2),encoding="utf-8")
    print("COVERAGE untranslated keys",missing_total,flush=True)
    print("COMPLETED LOCALE PACK namespaces",len(upstream_ru),"machine keys",count,
          "total Russian keys",sum(map(len,upstream_ru.values())),flush=True)

if __name__=="__main__":main()
