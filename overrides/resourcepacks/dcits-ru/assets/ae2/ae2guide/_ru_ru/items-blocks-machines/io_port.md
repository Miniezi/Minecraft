---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: МЭ порт ввода/вывода
  icon: io_port
  position: 210
categories:
- devices
item_ids:
- ae2:io_port
---


<a name="the-me-i/o-port"/>

# МЭ порт ввода/вывода

<BlockImage id="io_port" p:powered="true" scale="8" />

Порт ввода/вывода позволяет быстро заполнять или опорожнять [ячейки хранения](../items-blocks-machines/storage_cells.md) из [сетевого хранилища](../ae2-mechanics/import-export-storage.md) или в него.


Его можно поворачивать с помощью <ItemLink id="certus_quartz_wrench" />.



<a name="settings"/>

## Настройки

*   Порт ввода/вывода можно настроить на перемещение ячейки в выходные слоты, когда ячейка пуста, полна или когда работа выполнена.
*   Если вставлен <ItemLink id="redstone_card" />, появятся опции для различных условий красного камня
*   В центре графического интерфейса есть стрелка для установки направления передачи предметов: из ячейки в [сетевое хранилище](../ae2-mechanics/import-export-storage.md) или из хранилища в ячейку.



<a name="upgrades"/>

## Улучшения

Порт ввода/вывода поддерживает следующие [улучшения](upgrade_cards.md):


*   <ItemLink id="speed_card" /> увеличивает количество перемещаемых предметов за операцию
*   <ItemLink id="redstone_card" /> добавляет управление красным камнем, позволяя активировать при высоком сигнале, низком сигнале или один раз за импульс


<a name="recipe"/>

## Рецепт

<RecipeFor id="io_port" />
