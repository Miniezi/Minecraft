---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Плоскость формирования
  icon: formation_plane
  position: 210
categories:
- devices
item_ids:
- ae2:formation_plane
---


<a name="the-formation-plane"/>

# Плоскость формирования

<GameScene zoom="8" background="transparent">
  <ImportStructure src="../assets/blocks/formation_plane.snbt" />
</GameScene>

Плоскость формирования размещает блоки и сбрасывает предметы. Она работает аналогично <ItemLink id="storage_bus" />, работающей только на вставку, размещая/сбрасывая предметы, когда они «сохраняются» в ней с помощью [устройств](../ae2-mechanics/devices.md), вставляющих предметы в [сетевое хранилище](../ae2-mechanics/import-export-storage.md), такие как <ItemLink id="import_bus" /> и <ItemLink id="interface" />.

<GameScene zoom="8" interactive={true}>
  <ImportStructure src="../assets/assemblies/formation_plane_demonstration.snbt" />
  <IsometricCamera yaw="255" pitch="30" />
</GameScene>

Обратите внимание, что эти устройства похожи на трубопроводы «шина импорта -> шина хранения» и «интерфейс -> шина хранения» в [подсетях труб](../example-setups/pipe-subnet.md).

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/import_storage_pipe.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/interface_storage_pipe.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Это [устройство](../ae2-mechanics/devices.md) использует механики, применяемые шинами хранения в таких вещах, как [подсети труб](../example-setups/pipe-subnet.md), и может заменить шины хранения в этих установках, если вы хотите сбрасывать предметы/размещать блоки вместо транспортировки предметов.


Они являются [подчастями кабеля](../ae2-mechanics/cable-subparts.md).

**НЕ ЗАБУДЬТЕ ВКЛЮЧИТЬ ФЕЙКОВЫХ ИГРОКОВ В ПРЕТЕНЗИИ НА ЧАНК**



<a name="filtering"/>

## Фильтрация

По умолчанию плоскость будет размещать/сбрасывать всё. Предметы, вставленные в её слоты фильтра, будут действовать как белый список, разрешая размещать только эти конкретные предметы.

Предметы и жидкости можно перетаскивать в слоты из JEI/REI, даже если у вас на самом деле нет ни одного из этих предметов.

Щёлкните правой кнопкой мыши по контейнеру с жидкостью (например, по ведру или резервуару), чтобы установить эту жидкость в качестве фильтра вместо предмета ведра или резервуара.



<a name="priority"/>

## Приоритет

Приоритеты можно установить, щёлкнув по гаечному ключу в правом верхнем углу графического интерфейса. Предметы, поступающие в сеть, будут начинать с хранилища с наивысшим приоритетом.



<a name="settings"/>

## Настройки

*   Плоскость можно настроить на размещение блоков в мире или сброс предметов


<a name="upgrades"/>

## Улучшения

Плоскость формирования поддерживает следующие [улучшения](upgrade_cards.md):

*   <ItemLink id="capacity_card" /> увеличивает количество слотов фильтра
*   <ItemLink id="fuzzy_card" /> позволяет плоскости фильтровать по уровню повреждения и/или игнорировать NBT предметов
*   <ItemLink id="inverter_card" /> переключает фильтр с белого списка на чёрный


<a name="recipe"/>

## Рецепт

<RecipeFor id="formation_plane" />
