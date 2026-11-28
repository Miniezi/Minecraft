execute if score @s eg_job matches 1.. run return run tellraw @s {"text": "У вас уже есть заказ."}
scoreboard players set @s eg_job 3
scoreboard players set @s eg_work 0
tellraw @s {"text":"Заказ принят. Shift + ПКМ по своей стойке. Под ней нужен аккумулятор с выходом вверх."}
