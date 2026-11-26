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
    append('arrival',d.say('У оператора: /forgegear — перековка, /supplyshop — магазин. Первая волна по радио. Первое бедствие на волне 10.'))
    d.fn('wave','scoreboard players set #active rd 1','scoreboard players add #wave rd 1','scoreboard players set #time rd 0',
         'execute as @e[tag=rd.core,limit=1] at @s store result score #rostered rd run perimeter_muster',
         'tellraw @a [{"text":"Началась волна "},{"score":{"name":"#wave","objective":"rd"}}]')
    d.fn('battle','perimeter_wave_tick','scoreboard players add #time rd 1',
         'execute if score #hp rd matches 1.. if score #gun rd matches 1.. as @e[tag=rd.mob,distance=..18,sort=nearest,limit=1] run damage @s 4 minecraft:generic',
         'execute if score #hp rd matches 1.. if score #gun rd matches 2 as @e[tag=rd.mob,distance=..24,sort=nearest,limit=1] run damage @s 5 minecraft:generic',
         'execute if entity @e[tag=rd.mob,distance=..4] if score #hp rd matches 1.. run scoreboard players remove #hp rd 1',
         'execute if score #rostered rd matches 1 unless entity @e[tag=rd.ticket,distance=..3] run function relay:win')
    # A protocol upgrade during an old-style wave retries that wave under the new roster.
    prepend('battle','execute unless score #rostered rd matches 1 run function relay:adopt_wave')
    d.fn('adopt_wave','tag @e[tag=rd.mob] add rd.cleanup','kill @e[tag=rd.mob]','execute store result score #rostered rd run perimeter_muster')
    append('win','scoreboard players set #rostered rd 0','scoreboard players set #rest rd 45',
           'execute if score #wave rd matches 5.. run scoreboard players set #rest rd 60',
           'scoreboard players operation #cycle rd = #wave rd','scoreboard players set #five rd 5','scoreboard players operation #cycle rd %= #five rd',
           'execute if score #wave rd matches 10.. if score #cycle rd matches 0 run scoreboard players set #rest rd 100',
           'tellraw @a [{"text":"Передышка: "},{"score":{"name":"#rest","objective":"rd"}},{"text":" сек. Отсчёт идёт, пока защитник в 48 блоках от базы."}]')
    prepend('base_tick','execute unless score #active rd matches 1 if score #wave rd matches 1.. if entity @a[distance=..48,gamemode=!spectator] run function relay:rest')
    d.fn('rest','execute if score #rest rd matches 1.. run scoreboard players remove #rest rd 1',
         'execute if score #rest rd matches 10 run tellraw @a {"text":"Следующая волна через 10 секунд!"}',
         'execute unless score #rest rd matches 1.. run function relay:wave')
    d.files[key('input')]=d.files[key('input')].replace('radio matches 3 if','radio matches 3 unless score #rest rd matches 1.. if')

    d.files[key('battle')]='\n'.join(s for s in d.files[key('battle')].splitlines() if 'scoreboard players remove #hp rd 1' not in s)+'\n'
    d.files[key('deploy')]=d.files[key('deploy')].replace('Invulnerable:1b','Invulnerable:0b,Health:120f,Attributes:[{Name:"minecraft:generic.max_health",Base:120d}]').replace('#hp rd 30','#hp rd 120').replace('#max rd 30','#max rd 120')
    d.fn('repair','execute if score #active rd matches 0 if score #wave rd matches 1.. if score #hp rd < #max rd if score #cash rd matches 6.. run function relay:treat')
    d.fn('treat','scoreboard players remove #cash rd 6','scoreboard players add #hp rd 18','execute if score #hp rd > #max rd run scoreboard players operation #hp rd = #max rd')
    d.files[key('walls')]=d.files[key('walls')].replace('matches 30','matches 120').replace('matches 40','matches 140').replace('matches 50','matches 160').replace('add #max rd 10','add #max rd 20').replace('scoreboard players add #hp rd 10','execute if score #active rd matches 0 run scoreboard players add #hp rd 20')
    d.files[key('input')]=d.files[key('input')].replace('matches ..59','matches ..179')
    d.files[key('arrival')]='\n'.join(s for s in d.files[key('arrival')].splitlines() if not s.startswith('tellraw'))+'\n'
    append('arrival',d.say('ПКМ по жителю с надписью «Ядро» открывает штаб. Первая волна запускается в штабе.'))

    d.files.pop(key('arsenal_reforge'),None)
