#!/usr/bin/env python3
"""Install JARs needed by a Forge server from a generated .mrpack manifest."""
import argparse,hashlib,json,os,urllib.request,zipfile
from pathlib import Path

def main():
    p=argparse.ArgumentParser()
    p.add_argument("mrpack")
    p.add_argument("server_directory")
    args=p.parse_args()
    server=Path(args.server_directory)
    if not server.is_dir():p.error("Forge server directory must exist")
    with zipfile.ZipFile(args.mrpack) as z:index=json.loads(z.read("modrinth.index.json"))
    if index["dependencies"].get("minecraft")!="1.20.1":p.error("Unexpected Minecraft version")
    for file in index["files"]:
        if file.get("env",{}).get("server")=="unsupported":continue
        relative=Path(file["path"])
        if len(relative.parts)!=2 or relative.parts[0]!="mods" or relative.suffix!=".jar":raise ValueError("Unsafe path")
        dest=server/relative
        checksum=file["hashes"]["sha512"]
        if dest.exists() and hashlib.sha512(dest.read_bytes()).hexdigest()==checksum:continue
        url=file["downloads"][0]
        if not url.startswith("https://"):raise ValueError("Non-HTTPS URL")
        req=urllib.request.Request(url,headers={"User-Agent":"MCA-MRPACK-Builder/1.0"})
        with urllib.request.urlopen(req,timeout=120) as response:blob=response.read()
        if hashlib.sha512(blob).hexdigest()!=checksum:raise ValueError("Checksum mismatch: "+str(relative))
        dest.parent.mkdir(parents=True,exist_ok=True)
        tmp=dest.with_suffix(".jar.tmp")
        tmp.write_bytes(blob)
        os.replace(tmp,dest)
        print("Installed",relative)
    # Copy shared village Paxi datapacks from the same MRPACK. Never install client resourcepacks.
    with zipfile.ZipFile(args.mrpack) as pack:
        prefix="overrides/config/paxi/datapacks/"
        for filename in pack.namelist():
            if not filename.startswith(prefix) or not filename.endswith(".zip"):continue
            relative=Path(filename[len("overrides/"):])
            if ".." in relative.parts:raise ValueError("Unsafe datapack path")
            destination=server/relative
            destination.parent.mkdir(parents=True,exist_ok=True)
            destination.write_bytes(pack.read(filename))
            print("Installed shared village datapack:",relative)
    # Custom Forge mods embedded as local MRPACK overrides must also be on dedicated server.
    with zipfile.ZipFile(args.mrpack) as pack:
        for name in pack.namelist():
            if not name.startswith("overrides/mods/") or not name.endswith(".jar"):continue
            local=Path(name[len("overrides/"):])
            if len(local.parts)!=2 or local.parts[0]!="mods" or ".." in local.parts:
                raise ValueError("Unsafe embedded JAR path")
            dest=server/local
            dest.parent.mkdir(parents=True,exist_ok=True)
            dest.write_bytes(pack.read(name))
            print("Installed custom Forge addon:",local)
    print("Finished. Remove obsolete JAR duplicates; install Forge server and Java 17 separately.")
if __name__=="__main__":main()
