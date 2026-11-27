execute if score @s eg_run matches 1.. run return run tellraw @s {"text":"Цикл уже идёт. Восстановите линию, если экспорт остановился.","color":"yellow"}
execute unless function energogrid:connected run return run function energogrid:help
execute store result score @s eg_fuel run clear @s minecraft:coal 1
execute unless score @s eg_fuel matches 1 run return run tellraw @s {"text":"Нет угля в инвентаре.","color":"red"}
scoreboard players set @s eg_run 400
tellraw @s {"text":"Уголь загружен. На счётчик поступает 50 ед. за тик при целой линии.","color":"green"}
