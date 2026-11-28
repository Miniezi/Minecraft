execute in minecraft:overworld positioned 0 81 0 if entity @a[nbt={Dimension:"minecraft:overworld"}] unless score #island eg_state matches 1 run function energogrid:island
execute in minecraft:overworld positioned 0 81 0 as @a[nbt={Dimension:"minecraft:overworld"}] unless score @s eg_kit matches 1 run function energogrid:player
execute as @a[scores={eg_kit=1}] unless score @s eg_menu matches 1 run function energogrid:give_menu
scoreboard players enable @a eg
advancement grant @a[scores={eg_fe_paid=1..},advancements={energogrid:first_sale=false}] only energogrid:first_sale
execute as @a[scores={eg=1}] run function energogrid:menu
execute as @a[scores={eg=2..10}] run function energogrid:terminal
scoreboard players set @a[scores={eg=1..}] eg 0
