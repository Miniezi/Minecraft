"""Сборщик профиля CurseForge. Запуск: python Spore_Perimeter.py"""
import io
import json
import sys
from pathlib import Path
from urllib.request import urlopen
from zipfile import ZipFile, ZIP_DEFLATED

ROOT = Path(__file__).resolve().parent
BASE = 'https://mediafilez.forgecdn.net/files/7829/406/inf.zip'
EXTRA = {238222:8769625, 248787:4770828, 60089:5338457, 429235:4810975,
         790626:8765815, 354339:4780029}
# Each branch awards one permanent attribute modifier at its final milestone.
BRANCHES = [
 ('scout','Разведка','compass', [
 ('Ориентирование','compass','Собери компас для разведки города.'),
 ('Ночное наблюдение','spyglass','Подготовь подзорную трубу.'),
 ('Дальний рейд','map','Подготовь пустую карту. Награда: +8% к скорости.')],
 'movement_speed',0.08,'multiply_base'),
 ('engineer','Инженерная служба','redstone',[
 ('Электроснабжение','redstone_block','Собери блок редстоуна.'),
 ('Датчик движения','sculk_sensor','Добудь скалк-сенсор для системы наблюдения.'),
 ('Защитный контур','observer','Собери наблюдатель. Награда: +2 к броне.')],
 'armor',2,'add'),
 ('medic','Полевая медицина','honey_bottle',[
 ('Первая помощь','honey_bottle','Собери мёд в бутылочку.'),
 ('Полевой госпиталь','brewing_stand','Подготовь зельеварку.'),
 ('Резерв медика','golden_apple','Подготовь золотое яблоко. Награда: +2 сердца.')],
 'max_health',4,'add'),
 ('gunner','Огневая поддержка','shield',[
 ('Укрытие','shield','Подготовь щит.'),
 ('Боекомплект','gunpowder','Собери 32 единицы пороха.'),
 ('Оружейник','tacz:modern_kinetic_gun','Возьми в руки огнестрельное оружие TaCZ. Награда: +10% сопротивления отбрасыванию.')],
 'knockback_resistance',0.1,'add')]
def encode(x):
    return json.dumps(x, ensure_ascii=False, indent=2).encode()
def campaign():
    files = {'pack.mcmeta':encode({'pack':{'pack_format':15,'description':'Периметр • полевые специализации'}})}
    def add(key,title,desc,item,criterion,parent=None,reward=None):
        display={'icon':{'item':'minecraft:'+item},'title':{'text':title},'description':{'text':desc},
                 'frame':'challenge' if reward else 'task','announce_to_chat':False}
        obj={'display':display,'criteria':{'goal':criterion}}
        if parent: obj['parent']='perimeter:'+parent
        else: display['background']='minecraft:textures/block/deepslate.png'
        if reward: obj['rewards']={'function':'perimeter:'+key,'experience':100}
        files['data/perimeter/advancements/'+key+'.json']=encode(obj)
    add('root','Периметр','Открой четыре полевые специализации.','iron_door',{'trigger':'minecraft:tick'})
    for index,(branch,label,icon,steps,attr,value,op) in enumerate(BRANCHES,1):
        parent='root'
        for stage,(title,item,desc) in enumerate(steps,1):
            key=f'{branch}_{stage}'
            item_id=item if ':' in item else 'minecraft:'+item
            stack={'items':[item_id]}
            if item=='gunpowder': stack['count']={'min':32}
            # Parent advances must be earned before the inventory check succeeds.
            pred={'condition':'minecraft:entity_properties','entity':'this','predicate':{
                'type_specific':{'type':'player','advancements':{'perimeter:'+parent:True}}}}
            add(key,label+' • '+title,desc,item if ':' not in item else 'crossbow',{'trigger':'minecraft:inventory_changed',
                'conditions':{'player':[pred],'items':[stack]}},parent,stage==3)
            parent=key
        uid=f'd752be00-be11-40ce-8100-{index:012d}'
        files[f'data/perimeter/functions/{key}.mcfunction']=(
            f'attribute @s minecraft:generic.{attr} modifier add {uid} perimeter_{branch} {value} {op}\n').encode()
    return files

def build(source, destination):
    with ZipFile(source) as src:
        manifest=json.loads(src.read('manifest.json'))
        if manifest['minecraft']['version']!='1.20.1' or not any(
            m['id'].startswith('forge-') for m in manifest['minecraft']['modLoaders']):
            raise ValueError('Нужен исходный профиль INFEKCIA для Forge 1.20.1.')
        manifest.update(name='Spore — Периметр',version='1.0 (INFEKCIA 0.6)')
        present={m['projectID'] for m in manifest['files']}
        manifest['files'].extend({'projectID':p,'fileID':f,'required':True} for p,f in EXTRA.items() if p not in present)
        overrides=manifest.get('overrides','overrides').rstrip('/')
        additions={overrides+'/config/openloader/data/perimeter/'+k:v for k,v in campaign().items()}
        additions['manifest.json']=encode(manifest)
        with ZipFile(destination,'w',ZIP_DEFLATED) as out:
            for info in src.infolist():
                if info.filename not in additions: out.writestr(info,src.read(info))
            for k,v in additions.items(): out.writestr(k,v)

def main():
    local=Path(sys.argv[1]) if len(sys.argv)>1 else ROOT/'inf.zip'
    if local.is_file(): source=local
    else:
        print('Загрузка INFEKCIA 0.6 с CurseForge...')
        with urlopen(BASE,timeout=30) as r: source=io.BytesIO(r.read())
    result=io.BytesIO()
    build(source,result)
    dest=ROOT/'Spore_Perimeter_CurseForge.zip'
    dest.write_bytes(result.getvalue())
    print('Готово. Импортируй в CurseForge:',dest)
if __name__=='__main__':
    try: main()
    except Exception as e:
        print('Сборка профиля не завершена:',e)
        print('Если загрузка недоступна, положи оригинальный inf.zip рядом и запусти снова.')
        sys.exit(1)
