fill -9 78 -7 9 78 7 minecraft:stone
fill -9 79 -7 9 79 7 minecraft:dirt
fill -9 80 -7 9 80 7 minecraft:grass_block
fill -3 80 -2 7 80 3 minecraft:polished_andesite
setblock 2 80 0 minecraft:gold_block
setblock 3 80 0 minecraft:orange_concrete
setblock 4 80 0 minecraft:orange_concrete
setblock 5 80 0 minecraft:orange_concrete
setblock 6 81 0 minecraft:lodestone
setblock -2 81 0 minecraft:lectern
setblock -2 81 2 minecraft:chest
item replace block -2 81 2 container.0 with powah:furnator_starter 1
item replace block -2 81 2 container.1 with powah:energy_cable_starter 3
item replace block -2 81 2 container.2 with minecraft:coal 1
item replace block -2 81 2 container.3 with minecraft:crafting_table 1
setblock -7 81 -4 minecraft:oak_log
setblock -7 82 -4 minecraft:oak_log
setblock -7 83 -4 minecraft:oak_log
fill -8 84 -5 -6 85 -3 minecraft:oak_leaves[persistent=true]
setblock -7 84 -4 minecraft:oak_log
fill -8 81 1 -8 81 6 minecraft:oak_fence
setworldspawn 0 81 0
scoreboard players set #island eg_state 1
item replace block -2 81 2 container.4 with minecraft:oak_sapling 2
item replace block -2 81 2 container.5 with minecraft:cobblestone 8
