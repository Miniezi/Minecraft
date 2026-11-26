import json
import defense as d
from upgrade import append,prepend,key

def install():
    # Route old radio shortcuts through the same two counters.
    lines=d.files[key('input')].splitlines()
    lines=[s for s in lines if not any('radio matches '+str(n)+' ' in s for n in (40,41,42))]
    d.files[key('input')]='\n'.join(lines)+'\n'
    for num,cmd in [(40,'supplyshop'),(41,'supplyshop'),(42,'forgegear')]:
        prepend('input',f'execute if score @s radio matches {num} run {cmd}')
    lines=d.files[key('menu')].splitlines()
    d.files[key('menu')]='\n'.join(s for s in lines if 'radio set 42' not in s)+'\n'
    append('menu','tellraw @s '+json.dumps(['',d.label(41,'Магазин снабжения'),d.label(42,'Мастерская перековки')],ensure_ascii=False))
    append('arrival',d.say('У оператора: /forgegear — перековка, /supplyshop — магазин. Каждая пятая волна — босс.'))
    append('wave','scoreboard players operation #bossCycle rd = #wave rd','scoreboard players set #ten rd 10','scoreboard players operation #bossCycle rd %= #ten rd',
           'execute if score #bossCycle rd matches 5 run function relay:boss_guard',
           'execute if score #bossCycle rd matches 0 run function relay:boss_scout')
    for guard in (True,False):
        kind='guard' if guard else 'scout'
        title='Панцирник' if guard else 'Дозорный'
        hp=90 if guard else 75
        nbt='{Tags:["rd.mob","rd.boss","rd.'+kind+'boss"],PersistenceRequired:1b,CustomNameVisible:1b,CustomName:'+repr(json.dumps({'text':title},ensure_ascii=False))+',Health:'+str(hp)+'f,Attributes:[{Name:"minecraft:generic.max_health",Base:'+str(hp)+'d},{Name:"minecraft:generic.armor",Base:'+('8' if guard else '4')+'d}]}'
        d.fn('boss_'+kind,'execute positioned ~22 ~ ~4 positioned over motion_blocking_no_leaves run summon minecraft:husk ~ ~ ~ '+nbt,'tellraw @a '+json.dumps({'text':'Босс: '+title+('. Держи дистанцию!' if guard else '. Сокращай дистанцию или ищи укрытие!'),'color':'gold'},ensure_ascii=False))
    for name in ('lose','timeout'):
        prepend(name,'tag @e[tag=rd.boss] add rd.cleanup')
    prepend('breach','tag @s add rd.cleanup')
