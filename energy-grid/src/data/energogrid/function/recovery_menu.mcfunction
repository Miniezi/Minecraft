tellraw @s [{"text":"Восстановление","color":"light_purple","bold":true}]
tellraw @s [{"text":"[Восстановить оборудование и топливо]","color":"light_purple","underlined":true,"clickEvent":{"action":"run_command","value":"/trigger eg set 4"}}]
tellraw @s [{"text":"[Заменить потерянную книгу]","color":"aqua","underlined":true,"clickEvent":{"action":"run_command","value":"/trigger eg set 10"}},{"text":"  "},{"text":"[Главное меню]","color":"aqua","underlined":true,"clickEvent":{"action":"run_command","value":"/trigger eg set 1"}}]
