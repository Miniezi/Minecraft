---
navigation:
  parent: example-setups/example-setups-index.md
  title: Подсеть "труба" для предметов/жидкостей
  icon: storage_bus
---

# Подсеть "труба" для предметов/жидкостей

Простой метод эмуляции трубы для предметов и/или жидкостей с помощью [устройств](../ae2-mechanics/devices.md) AE2, полезный для всего, для чего вы использовали бы трубу для предметов или жидкостей.
Это включает возврат результата крафта в <ItemLink id="pattern_provider" />.

Обычно существует два разных метода достижения этого:

## Шина импорта -> Шина хранения

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/import_storage_pipe.snbt" />

<BoxAnnotation color="#dddddd" min="3.7 0 0" max="4 1 1">
        (1) Шина импорта: Может быть отфильтрована.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1 0 0" max="1.3 1 1">
        (2) Шина хранения: Может быть отфильтрована. Эта (и другие шины хранения, которые вы хотите сделать назначением)
        должна быть единственным хранилищем в сети.
  </BoxAnnotation>

<DiamondAnnotation pos="4.5 0.5 0.5" color="#00ff00">
        Источник
    </DiamondAnnotation>

<DiamondAnnotation pos="0.5 0.5 0.5" color="#00ff00">
        Назначение
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

<ItemLink id="import_bus" /> (1) в инвентаре источника импортирует предметы или жидкость и пытается сохранить их в [сетевое хранилище](../ae2-mechanics/import-export-storage.md).
Поскольку единственное хранилище в сети — это <ItemLink id="storage_bus" /> (2) (поэтому это подсеть, а не в основной сети), предметы или жидкость
помещаются в инвентарь назначения, таким образом передаваясь. Энергия подаётся через <ItemLink id="quartz_fiber" />.
Шину импорта и шину хранения можно отфильтровать, но настройка будет передавать всё, к чему она может получить доступ, если фильтры не применены.
Эта настройка также работает с несколькими шинами импорта и несколькими шинами хранения.

## Шина хранения -> Шина экспорта

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/storage_export_pipe.snbt" />

<BoxAnnotation color="#dddddd" min="3.7 0 0" max="4 1 1">
        (1) Шина хранения: Может быть отфильтрована. Эта (и другие шины хранения, которые вы хотите сделать источником)
        должна быть единственным хранилищем в сети.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1 0 0" max="1.3 1 1">
        (2) Шина экспорта: Должна быть отфильтрована.
  </BoxAnnotation>

<DiamondAnnotation pos="4.5 0.5 0.5" color="#00ff00">
        Источник
    </DiamondAnnotation>

<DiamondAnnotation pos="0.5 0.5 0.5" color="#00ff00">
        Назначение
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

<ItemLink id="export_bus" /> в инвентаре назначения пытается извлечь предметы в своём фильтре из [сетевого хранилища](../ae2-mechanics/import-export-storage.md).
Поскольку единственное хранилище в сети — это <ItemLink id="storage_bus" /> (поэтому это подсеть, а не в основной сети), предметы или жидкость
извлекаются из инвентаря источника, таким образом передаваясь. Энергия подаётся через <ItemLink id="quartz_fiber" />.
Поскольку шины экспорта должны быть отфильтрованы для функционирования, эта настройка работает только при фильтрации шины экспорта.
Эта настройка также работает с несколькими шинами хранения и несколькими шинами экспорта.

## Настройка, которая не работает (Шина импорта -> Шина экспорта)

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/import_export_pipe.snbt" />

<BoxAnnotation color="#dd3333" min="3.7 0 0" max="4 1 1">
        Шина импорта: Поскольку в сети нет хранилища, некуда импортировать.
  </BoxAnnotation>

<BoxAnnotation color="#dd3333" min="1 0 0" max="1.3 1 1">
        (2) Шина экспорта: Поскольку в сети нет хранилища, нечего экспортировать.
  </BoxAnnotation>

<DiamondAnnotation pos="4.5 0.5 0.5" color="#ff0000">
        Источник
    </DiamondAnnotation>

<DiamondAnnotation pos="0.5 0.5 0.5" color="#ff0000">
        Назначение
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Настройка только с шиной импорта и шиной экспорта не будет работать. Шина импорта попытается извлечь из инвентаря источника
и сохранить предметы или жидкость в сетевое хранилище. Шина экспорта попытается извлечь из сетевого хранилища и поместить
предметы или жидкость в инвентарь назначения. Однако, поскольку эта сеть **не имеет хранилища**, шина импорта не может импортировать,
а шина экспорта не может экспортировать, поэтому ничего не происходит.


<a name="inputting-and-outputting-through-1-face"/>

## Ввод и вывод через 1 грань

Скажем, у вас есть какая-то машина, которая может получать ввод и из которой можно извлекать вывод через 1 грань. (Например, <ItemLink id="charger" />)
Вы можете как толкать ингредиенты, так и извлекать результат, комбинируя 2 метода подсети труб:

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/import_storage_export_pipe.snbt" />

<BoxAnnotation color="#dddddd" min="4 1 1" max="5 1.3 2">
        (1) Шина импорта: Может быть отфильтрована.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="2 1 1" max="3 1.3 2">
        (2) Шина хранения: Может быть отфильтрована. Эта (и другие шины хранения, которые вы хотите толкать и извлекать предметы)
        должна быть единственным хранилищем в сети.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="2 0 1" max="3 1 2">
        (3) То, во что вы хотите толкать и из чего извлекать: В этом случае Зарядник.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="0 1 1" max="1 1.3 2">
        (4) Шина экспорта: Должна быть отфильтрована.
  </BoxAnnotation>

<DiamondAnnotation pos="4.5 0.5 1.5" color="#00ff00">
        Источник
    </DiamondAnnotation>

<DiamondAnnotation pos="0.5 0.5 1.5" color="#00ff00">
        Назначение
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>


<a name="interfaces"/>

## Интерфейсы

Оказывается, есть [устройства](../ae2-mechanics/devices.md), кроме шин импорта и экспорта, которые толкают предметы в
и извлекают предметы из [сетевого хранилища](../ae2-mechanics/import-export-storage.md)!
Здесь релевантен <ItemLink id="interface" />. Если вставлен предмет, который интерфейс не настроен хранить, интерфейс будет
толкать его в сетевое хранилище, что мы можем использовать аналогично трубе шина импорта -> шина хранения. Настройка интерфейса на
хранение некоторого предмета будет извлекать его из сетевого хранилища, аналогично трубе шина хранения -> шина экспорта. Интерфейсы можно настроить на
хранение некоторых вещей и не хранение других, позволяя вам удалённо толкать и извлекать через шины хранения, если вы по какой-то причине хотите это сделать.

<GameScene zoom="6" background="transparent">
<ImportStructure src="../assets/assemblies/interface_pipes.snbt" />

<BoxAnnotation color="#dddddd" min="3.7 0 0" max="4 1 1">
        Интерфейс
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1 0 0" max="1.3 1 1">
        Шина хранения
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="3.7 0 2" max="4 1 3">
        Шина хранения
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="0 1 2" max="1 1.3 3">
        Интерфейс
  </BoxAnnotation>

<IsometricCamera yaw="195" pitch="30" />
</GameScene>


<a name="one-to-many-and-many-to-one-(and-many-to-many)"/>

## Один ко многим и много к одному (и много ко многим)


Конечно, вы не обязаны использовать только одну <ItemLink id="import_bus" /> или <ItemLink id="export_bus" /> или <ItemLink id="storage_bus" />

<GameScene zoom="3" background="transparent">
<ImportStructure src="../assets/assemblies/many_to_many_pipe.snbt" />

<IsometricCamera yaw="185" pitch="30" />
</GameScene>


<a name="providing-to-multiple-places"/>

## Предоставление в несколько мест

Из всего этого мы можем вывести метод отправки ингредиентов с одной грани <ItemLink id="pattern_provider" /> в несколько разных
мест, например, в массив машин или на несколько разных граней одной машины.

Мы не хотим использовать трубу импорт -> хранение или хранение -> экспорт, потому что <ItemLink id="pattern_provider" /> никогда
фактически не содержит ингредиенты. Вместо этого поставщики *толкают* ингредиенты в соседние инвентари, поэтому нам нужно какое-то 
соседнее хранилище, которое также может импортировать предметы.

Это звучит как... <ItemLink id="interface" />!
Убедитесь, что поставщик находится в направленном или плоском режиме подчасти и/или интерфейс находится в плоском режиме подчасти, чтобы два не образовывали сетевое
соединение.

<GameScene zoom="6" background="transparent">
<ImportStructure src="../assets/assemblies/provider_interface_storage.snbt" />

<BoxAnnotation color="#dddddd" min="2.7 0 1" max="3 1 2">
        Интерфейс (должен быть плоским, не в виде полного блока)
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1 0 0" max="1.3 1 4">
        Шины хранения
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="0 0 0" max="1 1 4">
        Места, куда вы хотите предоставить шаблон (несколько машин или несколько граней одной машины)
  </BoxAnnotation>

<IsometricCamera yaw="185" pitch="30" />
</GameScene>
