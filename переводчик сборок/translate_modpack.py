#!/usr/bin/env python3
"""Translate Minecraft modpacks into an audited ru_ru resource pack.

Python 3.10+, stdlib only. For new translations set OPENAI_API_KEY.
Never translates registry IDs, filenames, format markers or Chipped.
"""
import argparse
import csv
import hashlib
import io
import json
import os
from pathlib import Path
import re
import sys
import time
import urllib.request
import zipfile

USER_AGENT = "minecraft-russian-localizer/1.0"
API_URL = "https://api.openai.com/v1/chat/completions"
FORMAT = re.compile(r'%(?:\d+\$)?[-+# 0,(]*\d*(?:\.\d+)?[a-zA-Z%]|§[0-9a-fk-or]|&[0-9a-fk-or]|\$\{[^}]+\}|\{[a-zA-Z_][a-zA-Z0-9_.-]*\}|\{\d+\}|\\[nrt]|<[^<>]+>|https?://\S+', re.I)
PLACEHOLDER = re.compile(r'@@PH(\d+)@@')
ALLOWED = re.compile(r'^[a-z0-9_.-]+$')
CYRILLIC = re.compile('[а-яёА-ЯЁ]')
ENGLISH = re.compile('[A-Za-z]')
TECH = re.compile(r'^[\d\s_\-+.,:;|/\\%#@(){}\[\]<>=$*!?^~]+$')
IDENTIFIER = re.compile(r'^[a-z0-9_.-]+(?::[a-z0-9_./-]+)?$')
FILE_CAP_MB = 350
MAX_DOWNLOAD_MB = 700

def fetch(url, maximum=MAX_DOWNLOAD_MB):
    if not url.startswith("https://"):
        raise ValueError("Only HTTPS download URLs allowed")
    with urllib.request.urlopen(urllib.request.Request(url, headers={"User-Agent": USER_AGENT}), timeout=75) as response:
        data = response.read(maximum * 1024 * 1024 + 1)
        if len(data) > maximum * 1024 * 1024:
            raise ValueError("Download too large")
        return data

def jar_language(data):
    with zipfile.ZipFile(io.BytesIO(data)) as z:
        for path in z.namelist():
            match = re.fullmatch(r'assets/([a-z0-9_.-]+)/lang/(en_us|ru_ru)\.json', path)
            if not match:
                continue
            namespace, locale = match.groups()
            try:
                obj = json.loads(z.read(path))
            except (ValueError, UnicodeError):
                continue
            if isinstance(obj, dict):
                yield namespace, locale, {str(k):v for k,v in obj.items() if isinstance(v, str)}

def collect_zip(data, registry, labels, sha=None):
    if sha and hashlib.sha512(data).hexdigest().lower() != sha.lower():
        raise ValueError("SHA512 mismatch")
    with zipfile.ZipFile(io.BytesIO(data)) as z:
        for entry in z.infolist():
            if entry.is_dir() or entry.file_size > FILE_CAP_MB * 1024 * 1024:
                continue
            name = entry.filename.lower()
            if name.endswith('.jar') and ('mods/' in name or name.count('/') == 0):
                payload = z.read(entry)
                collect_jar(payload, registry, labels)
            elif name.endswith('/lang/en_us.json') or name.endswith('/lang/ru_ru.json'):
                match = re.search(r'(?:^|/)assets/([a-z0-9_.-]+)/lang/(en_us|ru_ru)\.json$', name)
                if match:
                    try: values = json.loads(z.read(entry))
                    except ValueError: continue
                    if isinstance(values, dict):
                        namespace, locale = match.groups()
                        registry.setdefault(namespace, {}).setdefault(locale, {}).update(
                            {k:v for k,v in values.items() if isinstance(k,str) and isinstance(v,str)})
                        labels.add(namespace)

def collect_jar(data, registry, labels):
    if not zipfile.is_zipfile(io.BytesIO(data)):
        return
    for namespace, locale, values in jar_language(data):
        registry.setdefault(namespace, {}).setdefault(locale, {}).update(values)
        labels.add(namespace)

def collect_mrpack(data, registry, labels, allow_downloads):
    with zipfile.ZipFile(io.BytesIO(data)) as z:
        index = json.loads(z.read('modrinth.index.json'))
        for name in z.namelist():
            if name.endswith('/lang/en_us.json') or name.endswith('/lang/ru_ru.json'):
                match = re.search(r'assets/([a-z0-9_.-]+)/lang/(en_us|ru_ru)\.json$', name.lower())
                if match:
                    try: values = json.loads(z.read(name))
                    except ValueError: continue
                    if isinstance(values, dict):
                        ns, locale=match.groups()
                        registry.setdefault(ns,{}).setdefault(locale,{}).update({k:v for k,v in values.items() if isinstance(k,str) and isinstance(v,str)})
                        labels.add(ns)
        files=[f for f in index.get('files',[]) if str(f.get('path','')).lower().endswith('.jar')]
        if files and not allow_downloads:
            raise ValueError("MRPACK contains remote JARs. Pass --download-mods to fetch them.")
        for i, f in enumerate(files,1):
            urls=f.get('downloads') or []
            if not urls:
                print("No URL:",f.get('path'),file=sys.stderr)
                continue
            try:
                content=fetch(urls[0])
                digest=(f.get('hashes') or {}).get('sha512')
                if digest and hashlib.sha512(content).hexdigest().lower()!=digest.lower():
                    raise ValueError("SHA512 mismatch for "+f.get('path',''))
                collect_jar(content,registry,labels)
                print(f"[{i}/{len(files)}] loaded {f.get('path')}",flush=True)
            except Exception as e:
                print(f"[WARN] unable to read {f.get('path')}: {e}",file=sys.stderr)
                raise  # Incomplete downloads invalidate completeness claims

def is_technical(key,value):
    s=value.strip()
    if not s or not ENGLISH.search(s): return True,"empty_or_non_english"
    if IDENTIFIER.fullmatch(s) or TECH.fullmatch(s): return True,"identifier_or_symbol"
    if s.startswith(("http://","https://")): return True,"url"
    if re.fullmatch(r'(?:[A-Z0-9][A-Z0-9_.:/+-]{1,15}|[IVXLCDM]{1,8})', s): return True,"abbreviation"
    if re.fullmatch(r'\s*(?:%[\d$]*[sdf]|§[0-9a-fk-or]|\{\w+\}|\\n)+\s*',s,re.I): return True,"placeholder_only"
    if key.startswith(("key.categories.","itemGroup.")) and len(s)<=2: return True,"technical_label"
    return False,""

def mask(value):
    parts=[]
    def replace(m):
        index=len(parts)
        parts.append(m.group(0))
        return f"@@PH{index}@@"
    return FORMAT.sub(replace,value),parts

def unmask(text, original_tokens):
    all_matches=list(PLACEHOLDER.finditer(text))
    counts=[int(m.group(1)) for m in all_matches]
    if sorted(counts)!=list(range(len(original_tokens))):
        raise ValueError("placeholder mismatch")
    return PLACEHOLDER.sub(lambda m:original_tokens[int(m.group(1))],text)

def api_translate(rows,model,attempts=4):
    instructions=("You are a professional Minecraft Russian localization editor. "
      "Translate every English user-visible UI/gameplay string to natural idiomatic Russian. "
      "Respect item/gameplay meaning, tone, names and conventions. "
      "DO NOT translate or change any marker @@PHn@@, numeric values, commands, code, mod IDs. "
      "Preserve JSON mapping keys exactly. Return a single JSON object mapping row IDs to translations. "
      "Do not skip any entries. If text is already Russian, return it unchanged. "
      "Do not add commentary. Handle formatting and Minecraft terminology carefully.")
    items={str(i):{"namespace":r['namespace'],"key":r['key'],"text":r['masked']} for i,r in enumerate(rows)}
    payload={"model":model,"temperature":0,"response_format":{"type":"json_object"},
         "messages":[{"role":"system","content":instructions},
                     {"role":"user","content":json.dumps(items,ensure_ascii=False)}]}
    binary=json.dumps(payload,ensure_ascii=False).encode("utf-8")
    for attempt in range(attempts):
        try:
            request=urllib.request.Request(API_URL,data=binary,
                   headers={"Authorization":"Bearer "+os.environ["OPENAI_API_KEY"],
                           "Content-Type":"application/json"},method="POST")
            with urllib.request.urlopen(request,timeout=150) as response:
                obj=json.load(response)
            result=json.loads(obj['choices'][0]['message']['content'])
            if not isinstance(result,dict): raise ValueError("Expected object")
            return result
        except Exception as ex:
            if attempt==attempts-1: raise
            print("Translation attempt failed:",str(ex)[:250],file=sys.stderr)
            time.sleep(min(2**attempt,15))
    raise RuntimeError("unreachable")

def write_pack(output, translations):
    output.parent.mkdir(parents=True,exist_ok=True)
    with zipfile.ZipFile(output,'w',compression=zipfile.ZIP_DEFLATED,compresslevel=9) as z:
        z.writestr('pack.mcmeta',json.dumps({"pack":{"pack_format":15,"description":"RU modpack translation (automated, audited)"}},ensure_ascii=False))
        for ns,values in sorted(translations.items()):
            if values:
                z.writestr(f'assets/{ns}/lang/ru_ru.json',json.dumps(dict(sorted(values.items())),ensure_ascii=False,indent=2)+'\n')
    with zipfile.ZipFile(output) as z:
        assert z.testzip() is None
        for n in z.namelist():
            if n.endswith('.json') or n.endswith('.mcmeta'): json.loads(z.read(n))

def run(args):
    if args.source_url:
        content=fetch(args.source_url)
    elif args.input:
        content=Path(args.input).read_bytes()
    else:
        raise ValueError("Specify --input or --source-url")
    if not zipfile.is_zipfile(io.BytesIO(content)):
        raise ValueError("Input must be ZIP or MRPACK")
    registry={}; namespaces=set()
    with zipfile.ZipFile(io.BytesIO(content)) as z:
        if 'modrinth.index.json' in z.namelist():
            collect_mrpack(content,registry,namespaces,args.download_mods)
        else:
            collect_zip(content,registry,namespaces)

    cache_path=Path(args.cache)
    if cache_path.exists():
        cache=json.loads(cache_path.read_text(encoding='utf-8'))
    else: cache={}
    outputs={}; candidates=[]; audit=[]
    for namespace,locales in sorted(registry.items()):
        if namespace in args.exclude: continue
        en=locales.get('en_us',{}); ru=locales.get('ru_ru',{})
        if not en and not ru: continue
        result=dict(ru) if not args.override_existing else {}
        outputs[namespace]=result
        for key,value in sorted(en.items()):
            if not ALLOWED.fullmatch(namespace):continue
            row={"namespace":namespace,"key":key,"english":value,"status":"","reason":""}
            if key in ru and CYRILLIC.search(ru[key]) and not args.override_existing:
                row["status"]="native_ru";audit.append(row);continue
            technical,reason=is_technical(key,value)
            if technical:
                row["status"]="excluded";row["reason"]=reason;audit.append(row);continue
            ck=namespace+"\\0"+key+"\\0"+hashlib.sha256(value.encode()).hexdigest()
            row['cache_key']=ck
            cached=cache.get(ck)
            if isinstance(cached,str) and CYRILLIC.search(cached):
                normalized,tokens=mask(value)
                try:
                    _,original=mask(cached)
                    # cached is stored unmasked: compare tokens as a multiset
                    if sorted(tokens)!=sorted(original): raise ValueError("tokens differ")
                    result[key]=cached;row["status"]="cached"
                except ValueError: row["status"]="pending";candidates.append(row)
            else:
                row["status"]="pending";candidates.append(row)
            audit.append(row)

    if args.limit and len(candidates)>args.limit:
        candidates.sort(key=lambda r:(r["namespace"],r["key"]))
        candidates=candidates[:args.limit]
    if candidates and os.environ.get("OPENAI_API_KEY") and not args.audit_only:
        for start in range(0,len(candidates),args.batch_size):
            batch=candidates[start:start+args.batch_size]
            for row in batch:
                row['masked'],row['tokens']=mask(row['english'])
            try:
                translations=api_translate(batch,args.model)
            except Exception as e:
                print("API batch failed:",str(e),file=sys.stderr)
                for row in batch:row['reason']="api_error"
                continue
            for index,row in enumerate(batch):
                response=translations.get(str(index))
                if not isinstance(response,str) or not CYRILLIC.search(response):
                    row["reason"]="missing_russian";continue
                try:
                    restored=unmask(response,row['tokens'])
                    # Refuse accidental modifications of printf, formatting and URLs.
                    if sorted(mask(restored)[1]) != sorted(row['tokens']):
                        raise ValueError("format changes")
                    if '\n' in row['english'] and '\n' not in restored:
                        raise ValueError("lost newlines")
                except ValueError as e:
                    row['reason']="invalid_placeholder: "+str(e)
                    continue
                outputs[row['namespace']][row['key']]=restored
                cache[row['cache_key']]=restored
                row['status']='translated';row['reason']=''
            cache_path.parent.mkdir(parents=True,exist_ok=True)
            cache_path.write_text(json.dumps(cache,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
            print("Translated batch", min(start+args.batch_size,len(candidates)),"/",len(candidates),flush=True)

    write_pack(Path(args.output),outputs)
    with open(args.report,'w',newline='',encoding='utf-8-sig') as f:
        writer=csv.DictWriter(f,fieldnames=['namespace','key','english','status','reason'])
        writer.writeheader()
        for row in audit:writer.writerow({key:row.get(key,'') for key in writer.fieldnames})
    from collections import Counter
    counts=Counter(x['status'] for x in audit)
    summary={"namespaces":sorted(namespaces),"status_counts":counts,"total_keys":len(audit),
             "pack":str(args.output),"report":str(args.report),
             "language_key_candidates_done":counts.get('pending',0)==0,
             "complete_modpack_translation_verified":False}
    Path(args.summary).write_text(json.dumps(summary,ensure_ascii=False,indent=2,default=dict)+'\n',encoding='utf-8')
    print(json.dumps(summary,ensure_ascii=False,indent=2,default=dict))
    if args.fail_on_pending and counts.get('pending',0):
        return 2
    return 0

def cli():
    ap=argparse.ArgumentParser(description=__doc__)
    group=ap.add_mutually_exclusive_group(required=True)
    group.add_argument('--input',help='Local ZIP or MRPACK')
    group.add_argument('--source-url',help='Public HTTPS URL of ZIP/MRPACK')
    ap.add_argument('--download-mods',action='store_true',help='Fetch all MRPACK referenced JARs (verify SHA512)')
    ap.add_argument('--output',default='build/modpack-ru.zip')
    ap.add_argument('--report',default='build/audit.csv')
    ap.add_argument('--summary',default='build/summary.json')
    ap.add_argument('--cache',default='translations/cache.json')
    ap.add_argument('--model',default='gpt-4o-mini')
    ap.add_argument('--batch-size',type=int,default=20)
    ap.add_argument('--limit',type=int,default=0)
    ap.add_argument('--audit-only',action='store_true')
    ap.add_argument('--override-existing',action='store_true')
    ap.add_argument('--exclude',nargs='*',default=['chipped'])
    ap.add_argument('--fail-on-pending',action='store_true')
    opts=ap.parse_args()
    if opts.batch_size<1 or opts.batch_size>50: ap.error('--batch-size must be 1..50')
    try:sys.exit(run(opts))
    except Exception as e:
        print("ERROR:",e,file=sys.stderr)
        sys.exit(1)
if __name__=='__main__':cli()
