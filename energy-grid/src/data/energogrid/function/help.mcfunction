tellraw @s {"text":"Поставьте углегенератор (начальный) на золото, кабели Powah! — на оранжевые плитки. Откройте углегенератор (начальный) и положите уголь в топливный слот. Расчёт начинается при поступлении FE в магнетит.","color":"yellow"}
execute unless block 2 81 0 powah:furnator_starter run tellraw @s {"text":"Нет углегенератор (начальный) над золотой плиткой.","color":"red"}
execute unless block 3 81 0 powah:energy_cable_starter run tellraw @s {"text":"Нет кабеля на первой оранжевой плитке.","color":"red"}
execute unless block 4 81 0 powah:energy_cable_starter run tellraw @s {"text":"Нет кабеля на второй оранжевой плитке.","color":"red"}
execute unless block 5 81 0 powah:energy_cable_starter run tellraw @s {"text":"Нет кабеля на третьей оранжевой плитке.","color":"red"}
execute unless block 6 81 0 minecraft:lodestone run tellraw @s {"text":"Нет магнетита на отметке счётчика (6, 81, 0).","color":"red"}
