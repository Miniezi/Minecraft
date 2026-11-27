tp @s 0.5 81 0.5
spawnpoint @s 0 81 0
scoreboard players set @s eg_kit 1
scoreboard players set @s eg_bank 0
scoreboard players set @s eg_run 0
scoreboard players set @s eg_sales 0
scoreboard players set @s eg_spend 0
scoreboard players set @s eg_last 0
tellraw @s {"text":"Откройте сундук у кафедры. Наберите /trigger eg set 1 для вызова терминала.","color":"gold"}
