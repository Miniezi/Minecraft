execute in minecraft:overworld positioned 0 81 0 unless entity @s[distance=..24] run return run tellraw @s {"text":"Подойдите ближе к стартовой площадке, чтобы открыть этот раздел.","color":"yellow"}
execute in minecraft:overworld positioned 0 81 0 if score @s eg matches 2 run function energogrid:start
execute in minecraft:overworld positioned 0 81 0 if score @s eg matches 3 run function energogrid:buy
execute in minecraft:overworld positioned 0 81 0 if score @s eg matches 4 run function energogrid:recover
execute in minecraft:overworld positioned 0 81 0 if score @s eg matches 5 run function energogrid:help
execute in minecraft:overworld positioned 0 81 0 if score @s eg matches 6 run function energogrid:station
execute in minecraft:overworld positioned 0 81 0 if score @s eg matches 7 run function energogrid:shop
execute in minecraft:overworld positioned 0 81 0 if score @s eg matches 8 run function energogrid:status
execute in minecraft:overworld positioned 0 81 0 if score @s eg matches 9 run function energogrid:recovery_menu
execute in minecraft:overworld positioned 0 81 0 if score @s eg matches 10 run function energogrid:give_menu
