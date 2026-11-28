tellraw @s {"text":"Скрафтите печь из 8 булыжников. Поставьте её на золотую плитку. На три оранжевые плитки между печью и магнетитом поставьте по блоку вощёной резной меди. Кабель идёт к востоку от печи.","color":"yellow"}
tellraw @s {"text":"Держите уголь в инвентаре. Книга «Терминал энергосети» → «Станция» → «Запустить цикл» забирает 1 уголь. Один цикл занимает 20 секунд при 20 TPS. Для следующего цикла купите уголь через терминал.","color":"white"}
execute unless block 2 81 0 minecraft:furnace run tellraw @s {"text":"Простой: печь отсутствует над золотым блоком (2, 81, 0).","color":"red"}
execute unless block 3 81 0 minecraft:cut_copper unless block 3 81 0 minecraft:waxed_cut_copper run tellraw @s {"text":"Простой: первый блок линии отсутствует или окислился (3, 81, 0).","color":"red"}
execute unless block 4 81 0 minecraft:cut_copper unless block 4 81 0 minecraft:waxed_cut_copper run tellraw @s {"text":"Простой: второй блок линии отсутствует или окислился (4, 81, 0).","color":"red"}
execute unless block 5 81 0 minecraft:cut_copper unless block 5 81 0 minecraft:waxed_cut_copper run tellraw @s {"text":"Простой: третий блок линии отсутствует или окислился (5, 81, 0).","color":"red"}
execute unless block 6 81 0 minecraft:lodestone run tellraw @s {"text":"Простой: отсутствует магнетит (6, 81, 0).","color":"red"}
tellraw @s [{"text":"[Главное меню]","color":"aqua","underlined":true,"clickEvent":{"action":"run_command","value":"/trigger eg set 1"}}]
