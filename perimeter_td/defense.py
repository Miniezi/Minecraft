"""Build a Forge 1.20.1 data-only mod. No third-party Python dependencies."""
import json
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED

files={}
def put(path,data):
    files[path]=data if isinstance(data,str) else json.dumps(data,ensure_ascii=False)
def fn(name,*lines):
    put('data/relay/functions/'+name+'.mcfunction','\n'.join(lines)+'\n')
def say(msg):
    return 'tellraw @s '+json.dumps({'text':msg},ensure_ascii=False)
def score(name,n):
    return f'scoreboard players set #{name} rd {n}'
core='@e[type=marker,tag=rd.core,limit=1]'
enemy='@e[tag=rd.mob]'
put('META-INF/mods.toml','''modLoader="lowcodefml"
loaderVersion="[47,)"
license="MIT"
[[mods]]
modId="perimeter_defense"
version="0.2.0"
displayName="Периметр: оборона"
description="Кооперативная оборона узла снабжения"
''')
put('pack.mcmeta',{'pack':{'pack_format':15,'description':'Периметр: оборона узла'}})
for name in ('tick','load'):
    put('data/minecraft/tags/functions/'+name+'.json',{'values':['relay:'+name]})
fn('load','scoreboard objectives add rd dummy','scoreboard objectives add radio trigger',
   'scoreboard objectives add rd_pt dummy','scoreboard objectives add rd_role dummy',
   'scoreboard objectives add rd_raid dummy','scoreboard players add #clock rd 0')
fn('tick','scoreboard players enable @a radio',
   'execute as @a[scores={radio=1..}] at @s run function relay:input',
   'scoreboard players add #clock rd 1','execute if score #clock rd matches 20.. run function relay:second')
fn('second',score('clock',0),
   f'execute in minecraft:overworld as {core} at @s if entity @a[distance=..160] run function relay:base_tick')
fn('base_tick',
   'execute if score #active rd matches 1 run function relay:battle',
   'scoreboard players add #production rd 1',
   'execute if score #production rd matches 60.. run function relay:produce',
   'execute as @a[distance=64..150,scores={rd_raid=1}] at @s if entity @e[tag=rd.site,distance=..7] unless entity @e[tag=rd.guard,distance=..24] run scoreboard players set @s rd_raid 2',
   'execute as @a[distance=..8,scores={rd_raid=2}] run function relay:turnin')
fn('produce',score('production',0),'scoreboard players operation #ore rd += #mine rd',
   'execute if score #ore rd matches 129.. run '+score('ore',128))
fn('turnin','scoreboard players set @s rd_raid 0','scoreboard players add #cash rd 12',
   'give @s minecraft:iron_ingot 4',say('Груз доставлен. +12 кредитов базе и 4 железа тебе.'))
fn('deploy',f'summon marker ~ ~ ~ {{Tags:["rd.core"]}}','forceload add ~-32 ~-32 ~32 ~32',
   'summon villager ~ ~ ~ {Tags:["rd.lure"],NoAI:1b,Invulnerable:1b,Silent:1b,PersistenceRequired:1b,CustomName:\'{"text":"Оператор узла"}\'}',
   *[score(k,v) for k,v in {'active':0,'wave':0,'hp':30,'max':30,'cash':15,'mine':0,'ore':0,'gun':0,'production':0,'built':1}.items()],
   'give @s shield','give @s iron_axe','give @s bread 12',say('Узел развёрнут. Открой радио: /trigger radio'))
fn('battle',
   'scoreboard players add #time rd 1',
   'execute if score #gun rd matches 1..2 as @e[tag=rd.mob,distance=..18,sort=nearest,limit=1] run damage @s 4 minecraft:generic',
   'execute if score #gun rd matches 2 as @e[tag=rd.mob,distance=..24,sort=nearest,limit=1] run damage @s 5 minecraft:generic',
   'execute as @e[tag=rd.mob,distance=..4] run function relay:breach',
   'execute if score #hp rd matches ..0 run function relay:lose',
   'execute if score #active rd matches 1 if score #time rd matches 120.. if entity @e[tag=rd.mob] run function relay:timeout',
   'execute if score #active rd matches 1 unless entity @e[tag=rd.mob] run function relay:win')
fn('breach','scoreboard players remove #hp rd 3','kill @s')
fn('timeout','scoreboard players remove #hp rd 5','kill @e[tag=rd.mob]',
   'function relay:lose')
fn('lose',score('active',0),'kill @e[tag=rd.mob]',
   'scoreboard players operation #hp rd = #max rd','scoreboard players remove #wave rd 1',
   'tellraw @a {"text":"Узел пал. Награда потеряна. После перегруппировки повтори волну."}')
fn('win',score('active',0),'scoreboard players add #cash rd 10','scoreboard players operation #cash rd += #wave rd',
   'scoreboard players add @a[distance=..64] rd_pt 1',
   'tellraw @a {"text":"Волна пройдена. База получила кредиты, защитники — по очку талантов."}')
fn('wave',score('active',1),score('time',0),'scoreboard players add #wave rd 1',
   *[f'execute if score #wave rd matches {n}.. positioned ~{x} ~ ~{z} positioned over motion_blocking_no_leaves run summon {mob} ~ ~ ~ {{Tags:["rd.mob"],PersistenceRequired:1b}}'
     for n,x,z,mob in [(1,14,0,'husk'),(1,-14,1,'husk'),(1,0,16,'husk'),(1,1,-16,'husk'),(2,15,1,'husk'),(3,14,2,'husk'),(4,-16,3,'husk'),(5,0,-17,'husk'),(6,17,-2,'husk'),(7,-16,1,'husk'),(8,18,0,'husk')]],
   'execute if score #wave rd matches 2.. if loaded ~16 ~ ~ positioned ~16 ~ ~ positioned over motion_blocking_no_leaves run function relay:spores',
   'tellraw @a {"text":"Контакт! Не дай заражённым подойти к оператору ближе четырёх блоков."}')
# Forge loads optional entity references via a separate function only when Spore is present.
fn('spores','execute positioned ~ ~ ~1 run summon spore:inf_human ~ ~ ~ {Tags:["rd.mob"],PersistenceRequired:1b}',
   'execute if score #wave rd matches 4.. run summon spore:inf_human ~ ~ ~ {Tags:["rd.mob"],PersistenceRequired:1b}')
fn('collect','execute if score #ore rd matches 1.. run function relay:payout')
fn('payout','give @s iron_ingot','scoreboard players remove #ore rd 1','execute if score #ore rd matches 1.. run function relay:payout')
fn('site','execute positioned ~80 ~ ~ positioned over motion_blocking_no_leaves run function relay:tower')
fn('tower','execute store success score #placed rd run place template minecraft:pillager_outpost/watchtower ~ ~ ~',
   'execute if score #placed rd matches 1 run summon marker ~ ~ ~ {Tags:["rd.site"]}',
   'execute if score #placed rd matches 1 run '+score('site',1))
fn('guards',*[f'summon husk ~{x} ~ ~-2 {{Tags:["rd.guard"],PersistenceRequired:1b}}' for x in (0,3,6)])
fn('raid','execute unless score #site rd matches 1 run function relay:site',
   'execute if score #site rd matches 1 at @e[tag=rd.site,limit=1] unless entity @e[tag=rd.guard,distance=..30] run function relay:guards',
   'execute if score #site rd matches 1 run scoreboard players set @s rd_raid 1',
   'execute if score #site rd matches 1 run '+say('Задание: вышка в 80 блоках к востоку от узла. Зачисти охрану, подойди к северо-западному углу и вернись к оператору.'),
   'execute unless score #site rd matches 1 run '+say('Сектор ещё не загружен. Разведай восток и повтори заказ.'))
orders={3:('Волна','unless score #active rd matches 1','wave'),4:('Ремонт • 6','if score #cash rd matches 6.. if score #hp rd < #max rd','repair'),5:('Укрепление • 18','if score #cash rd matches 18.. if score #max rd matches ..59','walls'),6:('Автопушка I/II • 24','if score #cash rd matches 24.. if score #gun rd matches ..1','gun'),7:('Шахта I–IV • 15','if score #cash rd matches 15.. if score #mine rd matches ..3','mine'),8:('Забрать железо','if score #ore rd matches 1..','collect'),9:('Паёк • 3','if score #cash rd matches 3..','food'),10:('Вылазка','unless score @s rd_raid matches 1..','raid')}
fn('repair','scoreboard players remove #cash rd 6','scoreboard players add #hp rd 12','execute if score #hp rd > #max rd run scoreboard players operation #hp rd = #max rd')
fn('walls','scoreboard players remove #cash rd 18','scoreboard players add #max rd 10','scoreboard players add #hp rd 10')
for k,price in [('gun',24),('mine',15)]:
    fn(k,f'scoreboard players remove #cash rd {price}',f'scoreboard players add #{k} rd 1')
fn('food','scoreboard players remove #cash rd 3','give @s cooked_beef 8')
actions=['execute if score @s radio matches 2 if dimension minecraft:overworld unless score #built rd matches 1 run function relay:deploy']
menu=[(2,'Развернуть базу'),*[(n,x[0]) for n,x in orders.items()]]
for n,(_,cond,name) in orders.items():
    actions.append(f'execute if score @s radio matches {n} if entity {core[:-1]},distance=..8] {cond} at {core} run function relay:{name}')
for idx,(name,att,amt,op) in enumerate([('Броня','armor',2,'add'),('Здоровье','max_health',4,'add'),('Разведка','movement_speed',0.08,'multiply_base')],1):
    for rank in (1,2):
        num=10+2*idx+rank
        menu.append((num,f'{name} {rank} • {rank} ОТ'))
        fn(f'talent{num}',f'scoreboard players remove @s rd_pt {rank}',f'scoreboard players set @s rd_t{idx} {rank}',
           f'attribute @s minecraft:generic.{att} modifier add 9c748cc1-1ab9-4bcf-a002-{num:012d} rd_{num} {amt} {op}')
        cond=f'unless score @s rd_t{idx} matches 1..' if rank==1 else f'if score @s rd_t{idx} matches 1'
        actions.append(f'execute if score @s radio matches {num} if score @s rd_pt matches {rank}.. {cond} run function relay:talent{num}')
    files['data/relay/functions/load.mcfunction']+=f'scoreboard objectives add rd_t{idx} dummy\n'
def label(n,txt):
    return {'text':f' [{txt}] ','color':'aqua','clickEvent':{'action':'run_command','value':f'/trigger radio set {n}'}}
fn('input',*actions,'scoreboard players set @s radio 0','function relay:menu')
parts=['']
for key,title in [('wave','Волна'),('hp','Прочность'),('max','Макс.'),('cash','Кредиты'),('gun','Пушка'),('mine','Шахта'),('ore','Железо')]:
    parts.extend([{'text':f' {title} '},{'score':{'name':'#'+key,'objective':'rd'}}])
parts.extend([{'text':' ОТ '},{'score':{'name':'@s','objective':'rd_pt'}}])
fn('menu','tellraw @s '+json.dumps(parts,ensure_ascii=False),*['tellraw @s '+json.dumps(['']+[label(n,t) for n,t in menu[k:k+4]],ensure_ascii=False) for k in range(0,len(menu),4)])
def build(path):
    with ZipFile(path,'w',ZIP_DEFLATED) as out:
        for k,v in files.items():out.writestr(k,v)
if __name__=='__main__':build(Path('Perimeter-TD-0.2.0.jar'))
