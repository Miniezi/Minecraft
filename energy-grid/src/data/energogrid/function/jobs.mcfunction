tellraw @s {"text": "Хостинг. Выплата стойки идёт на счёт Money. Наберите $1 или $5 дохода от хостинга после принятия заказа. Заказ без срока; при простое прогресс сохраняется."}
tellraw @s {"text": "Короткий заказ: премия $2", "color": "dark_green", "underlined": true, "clickEvent": {"action": "run_command", "value": "/trigger eg set 21"}}
tellraw @s {"text": "Крупный заказ: премия $10", "color": "dark_green", "underlined": true, "clickEvent": {"action": "run_command", "value": "/trigger eg set 22"}}
tellraw @s [{"text": "Заказ (цель в $): "}, {"score": {"name": "@s", "objective": "eg_job"}}, {"text": " | Зачтено центов: "}, {"score": {"name": "@s", "objective": "eg_host"}}]
tellraw @s {"text": "Хранение 45 с: 30 ГБ, 200 FE/с, выплата $12", "color": "green", "clickEvent": {"action": "run_command", "value": "/trigger eg set 115"}}
tellraw @s {"text": "Вычисления 90 с: 512 МБ, 600 FE/с, выплата $36", "color": "green", "clickEvent": {"action": "run_command", "value": "/trigger eg set 116"}}
tellraw @s {"text":"При простое время заказа приостанавливается. Та же энергия дала бы на фиксированном тарифе $2,25 за хранение или $13,50 за вычисления. Стойке также нужны питание и сеть."}
energogrid_internal diagnose
