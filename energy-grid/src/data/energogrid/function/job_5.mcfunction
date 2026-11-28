execute if score @s eg_job matches 1.. run return run tellraw @s {"text": "У вас уже есть заказ."}
scoreboard players set @s eg_job 5
scoreboard players set @s eg_host 0
tellraw @s {"text": "Заказ принят. Рабочая стойка должна заработать $5."}
