execute unless block 6 81 0 minecraft:lodestone run setblock 6 81 0 minecraft:lodestone
execute unless block 2 81 0 powah:furnator_starter unless items entity @s inventory.* powah:furnator_starter unless items entity @s hotbar.* powah:furnator_starter run give @s powah:furnator_starter
execute unless block 3 81 0 powah:energy_cable_starter run setblock 3 81 0 powah:energy_cable_starter
execute unless block 4 81 0 powah:energy_cable_starter run setblock 4 81 0 powah:energy_cable_starter
execute unless block 5 81 0 powah:energy_cable_starter run setblock 5 81 0 powah:energy_cable_starter
tellraw @s {"text":"Стартовая линия восстановлена. Уголь можно купить в терминале или заменить древесным углём.","color":"green"}
execute unless score @s eg_sales matches 1.. unless items entity @s hotbar.* minecraft:coal unless items entity @s inventory.* minecraft:coal unless items block -2 81 2 container.* minecraft:coal run give @s minecraft:coal 1
