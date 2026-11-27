scoreboard players add @s eg_bank 5
scoreboard players add @s eg_sales 1
scoreboard players add @s eg_last 5
tellraw @s [{"text":"Оплачено: 20 000 ед. → +5 мон. | Баланс: ","color":"green"},{"score":{"name":"@s","objective":"eg_bank"}}]
execute if score @s eg_sales matches 1 run tellraw @s {"text":"Первый платёж получен. Купите уголь за 3 монеты и запустите второй цикл.","color":"gold"}
