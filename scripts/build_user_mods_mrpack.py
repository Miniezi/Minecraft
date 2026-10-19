import os,json,zipfile,io,urllib.request,urllib.parse,time
base=12172899685
mods=[
("BetterF3-7.0.2-Forge-1.20.1","betterf3"),
("cloth-config-11.1.136-forge","cloth-config"),
("Clumps-forge-1.20.1-12.0.0.4","clumps"),
("Controlling-forge-1.20.1-12.0.2","controlling"),
("DistantHorizons-3.3.3-1.20.1-fabric-forge","distanthorizons"),
("fast-ip-ping-v1.0.12-mc1.20.4-forge","fast-ip-ping"),
("MouseTweaks-forge-mc1.20.1-2.25.1","mouse-tweaks"),
("polymorph-forge-0.49.11+1.20.1","polymorph"),
("Searchables-forge-1.20.1-1.0.3","searchables"),
("sophisticatedbackpacks-1.20.1-3.26.12.2206","sophisticated-backpacks"),
("sophisticatedcore-1.20.1-1.5.12.2412","sophisticated-core"),
("voicechat-forge-1.20.1-2.6.24","simple-voice-chat"),
("jei-1.20.1-forge-15.71.0.224","jei")]
def get(url,auth=False):
 h={"User-Agent":"MCA-Modpack-Builder/1.0"}
 if auth:h["Authorization"]="Bearer "+os.environ["GH_TOKEN"]
 for i in range(5):
  try:
   with urllib.request.urlopen(urllib.request.Request(url,headers=h),timeout=100) as r:return r.read()
  except Exception:
   if i==4:raise
   time.sleep(2+i)
def versions(slug,filter=True):
 q=urllib.parse.urlencode({"loaders":json.dumps(["forge","neoforge"]),"game_versions":json.dumps(["1.20.1"])}) if filter else ""
 return json.loads(get("https://api.modrinth.com/v2/project/"+slug+"/version?"+q))
def find(name,slug):
 for candidate in [slug,slug.replace("-",""),slug.replace("sophisticated-","sophisticated"),"simple-voice-chat" if slug=="simple-voice-chat" else slug]:
  for restricted in [True,False]:
   try: vv=versions(candidate,restricted)
   except Exception: continue
   for v in vv:
    for f in v["files"]:
     if f["filename"].lower()==(name+".jar").lower():return f
 return None
url=f"https://api.github.com/repos/Miniezi/Minecraft/actions/artifacts/{base}/zip"
with zipfile.ZipFile(io.BytesIO(get(url,True))) as outer:
 name=next(n for n in outer.namelist() if n.endswith(".mrpack"))
 basebytes=outer.read(name)
z=zipfile.ZipFile(io.BytesIO(basebytes))
data=json.loads(z.read("modrinth.index.json"))
data["files"]=[f for f in data["files"] if not f["path"].lower().startswith("mods/jei-")]
missing=[]
for name,slug in mods:
 f=find(name,slug)
 if not f:missing.append(name);print("MISSING",name,flush=True);continue
 data["files"].append({"path":"mods/"+name+".jar","hashes":{"sha1":f["hashes"]["sha1"],"sha512":f["hashes"]["sha512"]},"downloads":[f["url"]],"fileSize":f["size"]})
 print("ADDED",name,flush=True)
if missing:raise RuntimeError("Missing exact versions: "+str(missing))
data["name"]="MCA Industrial Villages RU + User Mods (No Skills)"
data["versionId"]="2026.10.19-custom"
out="MCA-Industrial-Villages-RU-All-Mods-1.20.1.mrpack"
with zipfile.ZipFile(out,"w",compression=zipfile.ZIP_DEFLATED) as w:
 w.writestr("modrinth.index.json",json.dumps(data,ensure_ascii=False,indent=2))
 for n in z.namelist():
  if n!="modrinth.index.json":w.writestr(n,z.read(n))
with zipfile.ZipFile(out) as result:
 assert result.testzip() is None
 addon=zipfile.ZipFile(io.BytesIO(result.read("overrides/mods/mca-worker-chests-0.1.0.jar")))
 assert not any("VillageSkills" in n for n in addon.namelist())
print("BUILT",out,len(data["files"]),flush=True)
