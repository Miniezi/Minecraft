execute unless block 6 81 0 minecraft:lodestone run setblock 6 81 0 minecraft:lodestone
execute unless block 2 81 0 minecraft:furnace unless entity @s[nbt={Inventory:[{id:"minecraft:furnace"}]}] run give @s minecraft:furnace 1
scoreboard players set @s eg_amount 0
execute unless block 3 81 0 #energogrid:cables run scoreboard players add @s eg_amount 1
execute unless block 4 81 0 #energogrid:cables run scoreboard players add @s eg_amount 1
execute unless block 5 81 0 #energogrid:cables run scoreboard players add @s eg_amount 1
execute if score @s eg_amount matches 1.. unless entity @s[nbt={Inventory:[{id:"minecraft:waxed_cut_copper"}]}] run give @s minecraft:waxed_cut_copper 3
execute unless function energogrid:connected run return run function energogrid:help
execute unless score @s eg_bank matches ..2 run return run tellraw @s {"text":"На счёте достаточно средств для покупки угля.","color":"yellow"}
execute if entity @s[nbt={Inventory:[{id:"minecraft:coal"}]}] run return run tellraw @s {"text":"Уголь уже есть в инвентаре. Используйте [Пуск].","color":"yellow"}
execute if score @s eg_run matches 1.. run return run tellraw @s {"text":"Топливо уже загружено. Восстановите линию и завершите цикл.","color":"yellow"}
scoreboard players set @s eg_run 400
tellraw @s {"text":"Сеть предоставила аварийное топливо и запустила один цикл. После выплаты купите уголь.","color":"green"}
