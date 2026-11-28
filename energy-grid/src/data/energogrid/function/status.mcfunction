energogrid_internal balance
tellraw @s [{"text":"База $1 за 4 000 FE. Вечерний тариф ×2 при включённом рынке. Линия 200 FE/тик; после первой поставки 2 000 FE/тик, после приёмки турбины 100 000 FE/тик."}]
tellraw @s [{"text":"Выручка счётчика: $"},{"score":{"name":"@s","objective":"eg_last"}},{"text":" | Закупки: $"},{"score":{"name":"@s","objective":"eg_spend"}}]
tellraw @s [{"text":"[Главное меню]","color":"aqua","clickEvent":{"action":"run_command","value":"/trigger eg"}}]
