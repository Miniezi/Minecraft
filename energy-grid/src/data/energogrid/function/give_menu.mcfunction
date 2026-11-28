scoreboard players set @s eg_menu 1
execute unless items entity @s inventory.* minecraft:written_book[minecraft:custom_data~{eg_terminal:1b}] unless items entity @s hotbar.* minecraft:written_book[minecraft:custom_data~{eg_terminal:1b}] unless items entity @s weapon.offhand minecraft:written_book[minecraft:custom_data~{eg_terminal:1b}] run loot give @s loot energogrid:terminal_book
