execute in minecraft:overworld positioned 0 81 0 unless entity @s[distance=..24] run return run tellraw @s {"text":"Подойдите ближе к стартовой площадке, чтобы открыть этот раздел.","color":"yellow"}
execute in minecraft:overworld positioned 0 81 0 if score @s eg matches 3 run function energogrid:buy
execute in minecraft:overworld positioned 0 81 0 if score @s eg matches 4 run function energogrid:recover
execute in minecraft:overworld positioned 0 81 0 if score @s eg matches 5 run function energogrid:help
execute in minecraft:overworld positioned 0 81 0 if score @s eg matches 6 run function energogrid:station
execute in minecraft:overworld positioned 0 81 0 if score @s eg matches 7 run function energogrid:shop
execute in minecraft:overworld positioned 0 81 0 if score @s eg matches 8 run function energogrid:status
execute in minecraft:overworld positioned 0 81 0 if score @s eg matches 9 run function energogrid:recovery_menu
execute in minecraft:overworld positioned 0 81 0 if score @s eg matches 10 run function energogrid:give_menu
execute if score @s eg matches 11 run function energogrid:quests
execute if score @s eg matches 12 run function energogrid:supply
execute if score @s eg matches 13 run function energogrid:jobs
execute if score @s eg matches 14 run function energogrid:preview
execute if score @s eg matches 15 run function energogrid:auto_on
execute if score @s eg matches 16 run function energogrid:auto_off
execute if score @s eg matches 17 run function energogrid:small
execute if score @s eg matches 18 run function energogrid:bulk
execute if score @s eg matches 19 run function energogrid:spot
execute if score @s eg matches 20 run function energogrid:fixed
execute if score @s eg matches 21 run function energogrid:job_1
execute if score @s eg matches 22 run function energogrid:job_5
execute if score @s eg matches 40 run function energogrid:buy_0
execute if score @s eg matches 41 run function energogrid:buy_1
execute if score @s eg matches 42 run function energogrid:buy_2
execute if score @s eg matches 43 run function energogrid:buy_3
execute if score @s eg matches 44 run function energogrid:buy_4
execute if score @s eg matches 45 run function energogrid:buy_5
execute if score @s eg matches 46 run function energogrid:buy_6
execute if score @s eg matches 47 run function energogrid:buy_7
execute if score @s eg matches 48 run function energogrid:buy_8
execute if score @s eg matches 49 run function energogrid:buy_9
execute if score @s eg matches 50 run function energogrid:buy_10
execute if score @s eg matches 51 run function energogrid:buy_11
execute if score @s eg matches 52 run function energogrid:buy_12
execute if score @s eg matches 53 run function energogrid:buy_13
execute if score @s eg matches 54 run function energogrid:buy_14
execute if score @s eg matches 55 run function energogrid:buy_15
execute if score @s eg matches 56 run function energogrid:buy_16
execute if score @s eg matches 57 run function energogrid:buy_17
execute if score @s eg matches 58 run function energogrid:buy_18
execute if score @s eg matches 59 run function energogrid:buy_19
execute if score @s eg matches 60 run function energogrid:buy_20
execute if score @s eg matches 61 run function energogrid:buy_21
execute if score @s eg matches 62 run function energogrid:buy_22
execute if score @s eg matches 63 run function energogrid:buy_23
execute if score @s eg matches 64 run function energogrid:buy_24
execute if score @s eg matches 65 run function energogrid:buy_25
execute if score @s eg matches 66 run function energogrid:buy_26
execute if score @s eg matches 67 run function energogrid:buy_27
execute if score @s eg matches 68 run function energogrid:buy_28
execute if score @s eg matches 69 run function energogrid:buy_29
execute if score @s eg matches 70 run function energogrid:buy_30
execute if score @s eg matches 71 run function energogrid:buy_31
execute if score @s eg matches 72 run function energogrid:buy_32
execute if score @s eg matches 23 run function energogrid:catalog_0
execute if score @s eg matches 24 run function energogrid:catalog_1
execute if score @s eg matches 25 run function energogrid:catalog_2
execute if score @s eg matches 101 run function energogrid:part_0
execute if score @s eg matches 102 run function energogrid:part_1
execute if score @s eg matches 103 run function energogrid:part_2
execute if score @s eg matches 104 run function energogrid:part_3
execute if score @s eg matches 105 run function energogrid:part_4
execute if score @s eg matches 106 run function energogrid:part_5
execute if score @s eg matches 107 run function energogrid:part_6
execute if score @s eg matches 108 run function energogrid:part_7
execute if score @s eg matches 100 run function energogrid:parts
execute if score @s eg matches 113 run function energogrid:surplus_enable
execute if score @s eg matches 114 run function energogrid:surplus_disable
execute if score @s eg matches 109 run function energogrid:part_109
execute if score @s eg matches 110 run function energogrid:part_110
execute if score @s eg matches 115 run function energogrid:service_2
execute if score @s eg matches 116 run function energogrid:service_3
execute if score @s eg matches 117 run function energogrid:rule_117
execute if score @s eg matches 118 run function energogrid:rule_118
execute if score @s eg matches 119 run function energogrid:rule_119
execute if score @s eg matches 120 run function energogrid:rule_120
execute if score @s eg matches 121 run function energogrid:rule_121
execute if score @s eg matches 122 run function energogrid:rule_122
