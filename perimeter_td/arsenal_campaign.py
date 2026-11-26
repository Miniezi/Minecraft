import json
import defense as d
from upgrade import append,prepend

def install():
    append('arrival','perimeter_injector','perimeter_injector')
    append('win','execute as @a[distance=..64] run perimeter_crate')
    append('turnin','perimeter_crate')
    for num,cost,command in [(40,8,'injector'),(41,20,'crate'),(42,10,'reforge')]:
        fn='arsenal_'+command
        d.fn(fn,'execute store success score @s rd_buy run perimeter_'+command,
             f'execute if score @s rd_buy matches 1 run scoreboard players remove #cash rd {cost}')
        prepend('input',f'execute if score @s radio matches {num} if entity @e[tag=rd.core,distance=..8] if score #cash rd matches {cost}.. run function relay:{fn}')
    append('load','scoreboard objectives add rd_buy dummy')
    append('menu','tellraw @s '+json.dumps(['',d.label(40,'Автоинъектор • 8'),d.label(41,'Ящик арсенала • 20'),d.label(42,'Перековать в руке • 10')],ensure_ascii=False))
