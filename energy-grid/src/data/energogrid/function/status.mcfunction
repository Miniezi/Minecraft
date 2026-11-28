energogrid_internal balance
tellraw @s [{"text":"Тариф: $1 за 4 000 FE | Лимит: 200 FE/тик | Уголь: $3"}]
tellraw @s [{"text":"Выручка счётчика: $"},{"score":{"name":"@s","objective":"eg_last"}},{"text":" | Закупки угля: $"},{"score":{"name":"@s","objective":"eg_spend"}}]
tellraw @s [{"text":"[Главное меню]","color":"aqua","clickEvent":{"action":"run_command","value":"/trigger eg"}}]
