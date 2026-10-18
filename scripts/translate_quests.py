#!/usr/bin/env python3
"""Best-effort Russian localization generator for MCA Quests.

Translates *untranslated quest/project/situation narrative keys* from the
upstream English locale via a public machine translation endpoint.
Manual Russian strings always take priority. Generated strings are validated
for printf format placeholders. Unverified batches stay in English upstream.
Not a human-reviewed localization. Errors never corrupt the manual locale.
"""
import argparse, json, re, time, urllib.parse, urllib.request, urllib.error
from pathlib import Path

SRC="https://raw.githubusercontent.com/otectus/MCAQuests/main/src/main/resources/assets/mcaquests/lang/en_us.json"
TRANSLATE="https://translate.googleapis.com/translate_a/single"
FORMAT=re.compile(r"%(?:\d+\$)?[-+#0,.(\d]*[a-zA-Z%]")
MARK="🟩"
MANUAL=Path(__file__).resolve().parents[1]/"localization"/"mcaquests_ru_ru.json"
OUT=Path(__file__).resolve().parents[1]/"localization"/"mcaquests_ru_ru_generated.json"

def get_json(url):
    req=urllib.request.Request(url,headers={"User-Agent":"MCA-Quest-RU-Builder/1.0"})
    with urllib.request.urlopen(req,timeout=40) as r:return json.load(r)

def translated(text):
    url=TRANSLATE+"?"+urllib.parse.urlencode({"client":"gtx","sl":"en","tl":"ru","dt":"t","q":text})
    data=get_json(url)
    return "".join(fragment[0] for fragment in data[0] if fragment and fragment[0] is not None)

def matches(src,dst):
    return sorted(FORMAT.findall(src))==sorted(FORMAT.findall(dst)) and bool(dst.strip())

def chunks(pairs,limit=1050):
    batch=[];size=0
    for key,value in pairs:
        cost=len(value)+len(MARK)+2
        if batch and size+cost>limit:
            yield batch;batch=[];size=0
        batch.append((key,value));size+=cost
    if batch:yield batch

def main():
    p=argparse.ArgumentParser()
    p.add_argument("--seconds",type=int,default=240)
    p.add_argument("--limit",type=int,default=0,help="0 means all")
    args=p.parse_args()
    manual=json.loads(MANUAL.read_text(encoding="utf-8"))
    try:english=get_json(SRC)
    except Exception as exc:
        print("Machine translation unavailable (English source):",exc)
        OUT.write_text(json.dumps(manual,ensure_ascii=False,indent=2),encoding="utf-8")
        return
    targets=[(k,v) for k,v in english.items()
      if (k.startswith("mcaquests.quest.") or k.startswith("mcaquests.project.")
          or k.startswith("mcaquests.situation.") or k.startswith("mcaquests.voice."))
      and k not in manual and isinstance(v,str) and v.strip()]
    if args.limit:targets=targets[:args.limit]
    result=dict(manual)
    start=time.monotonic();success=0;skipped=0
    for i,batch in enumerate(chunks(targets)):
        if time.monotonic()-start>args.seconds:
            print("Translation time budget reached")
            break
        source=MARK.join(v.replace("\n"," ⏎ ") for _,v in batch)
        try:
            target=translated(source)
            pieces=target.split(MARK)
            if len(pieces)!=len(batch):raise ValueError("Separator count changed")
            for (key,orig),value in zip(batch,pieces):
                value=value.strip().replace(" ⏎ ","\n")
                if matches(orig,value) and re.search("[А-Яа-яЁё]",value):
                    result[key]=value;success+=1
                else:skipped+=1
        except Exception as exc:
            skipped+=len(batch)
            if i<5 or i%20==0:print("Skipped batch",i,str(exc)[:120])
        if i%25==0:
            print("Progress:",i,"translated",success,"skipped",skipped,flush=True)
        time.sleep(0.12)
    OUT.write_text(json.dumps(result,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
    print("Russian localization keys:",len(result),"machine translated:",success,"untranslated or failed:",len(targets)-success)

if __name__=="__main__":main()
