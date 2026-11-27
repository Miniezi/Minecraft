execute unless score @s eg_bank matches 3.. run return run tellraw @s {"text":"Нужно 3 монеты. Откройте восстановление в терминале.","color":"red"}
scoreboard players remove @s eg_bank 3
scoreboard players add @s eg_spend 3
give @s minecraft:coal 1
tellraw @s {"text":"Списано 3 монеты. Уголь доставлен игроку.","color":"yellow"}
