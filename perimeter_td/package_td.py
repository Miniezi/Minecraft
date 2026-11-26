"""Package the defense module with INFEKCIA and the companion client mods."""
import io
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
import Spore_Perimeter as base
import defense
def assemble(source, target):
    profile=io.BytesIO()
    base.EXTRA[698309]=5722938  # Explorify, public structure collection
    base.build(source, profile)
    import json
    with ZipFile(profile) as original, ZipFile(target,'w',ZIP_DEFLATED) as result:
        manifest=json.loads(original.read('manifest.json'))
        manifest.update(name='Периметр — Оборона узла',version='0.2.0')
        result.writestr('manifest.json',json.dumps(manifest,ensure_ascii=False))
        for entry in original.infolist():
            if entry.filename!='manifest.json':result.writestr(entry,original.read(entry))
        mod=io.BytesIO()
        defense.build(mod)
        result.writestr(manifest.get('overrides','overrides')+'/mods/Perimeter-TD-0.2.0.jar',mod.getvalue())
if __name__=='__main__':
    assemble(Path('inf.zip'),Path('Perimeter-TD-CurseForge.zip'))
