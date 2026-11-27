execute in minecraft:overworld positioned 0 81 0 if entity @a[nbt={Dimension:"minecraft:overworld"}] unless score #island eg_state matches 1 run function energogrid:island
execute in minecraft:overworld positioned 0 81 0 as @a[nbt={Dimension:"minecraft:overworld"}] unless score @s eg_kit matches 1 run function energogrid:player
scoreboard players enable @a eg
execute in minecraft:overworld positioned 0 81 0 as @a[distance=..24,scores={eg=1..5}] run function energogrid:terminal
scoreboard players set @a[scores={eg=1..}] eg 0
execute in minecraft:overworld positioned 0 81 0 as @a[distance=..24,scores={eg_run=1..}] run function energogrid:generate
