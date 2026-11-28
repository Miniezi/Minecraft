execute in minecraft:overworld positioned 0 81 0 if entity @a[nbt={Dimension:"minecraft:overworld"}] unless score #island eg_state matches 1 run function energogrid:island
execute in minecraft:overworld positioned 0 81 0 as @a[nbt={Dimension:"minecraft:overworld"}] unless score @s eg_kit matches 1 run function energogrid:player
execute as @a[scores={eg_kit=1}] unless score @s eg_menu matches 1 run function energogrid:give_menu
scoreboard players enable @a eg
advancement grant @a[scores={eg_fe_paid=1..},advancements={energogrid:first_sale=false}] only energogrid:first_sale
execute as @a[scores={eg=1}] run function energogrid:menu
execute as @a[scores={eg=2..199}] run function energogrid:terminal
scoreboard players set @a[scores={eg=1..}] eg 0
scoreboard players add #clock eg_clock 1
execute if score #clock eg_clock matches 20.. as @a[scores={eg_auto=1}] run energogrid_internal deliver
execute if score #clock eg_clock matches 20.. as @a[scores={eg_job=1..}] run energogrid_internal contract
execute if score #clock eg_clock matches 20.. as @a[scores={eg_surplus=1}] run energogrid_internal sell_surplus
execute if score #clock eg_clock matches 20.. run scoreboard players set #clock eg_clock 0
advancement grant @a[scores={eg_coalbuy=1..,eg_sales=1..},advancements={energogrid:purchase=false}] only energogrid:purchase
advancement grant @a[scores={eg_orders=1..},advancements={energogrid:delivery=false}] only energogrid:delivery
advancement grant @a[scores={eg_peak=1..},advancements={energogrid:peak=false}] only energogrid:peak
advancement grant @a[scores={eg_hostpaid=1..},advancements={energogrid:hosting=false}] only energogrid:hosting
advancement grant @a[scores={eg_last=1000..,eg_orders=10..,eg_deals=3..},advancements={energogrid:company=false}] only energogrid:company
advancement grant @a[scores={eg_fission=1..},advancements={energogrid:fission=false}] only energogrid:fission
advancement grant @a[scores={eg_turbine=1..},advancements={energogrid:turbine=false}] only energogrid:turbine
advancement grant @a[scores={eg_fusion=1..},advancements={energogrid:fusion=false}] only energogrid:fusion
advancement grant @a[scores={eg_storage=1..},advancements={energogrid:storage=false}] only energogrid:storage
advancement grant @a[scores={eg_compute=1..},advancements={energogrid:compute=false}] only energogrid:compute
