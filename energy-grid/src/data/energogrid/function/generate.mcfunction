execute unless function energogrid:connected run return run title @s actionbar {"text":"Простой: разрыв линии. Откройте диагностику терминала.","color":"red"}
scoreboard players remove @s eg_run 1
scoreboard players set @s eg_amount 400
scoreboard players operation @s eg_amount -= @s eg_run
scoreboard players operation @s eg_amount *= #rate eg_state
execute if score @s eg_run matches 0 run return run function energogrid:pay
title @s actionbar [{"text":"Счётчик: "},{"score":{"name":"@s","objective":"eg_amount"}},{"text":" / 20 000 ед. | Экспорт: 50 ед./тик"}]
