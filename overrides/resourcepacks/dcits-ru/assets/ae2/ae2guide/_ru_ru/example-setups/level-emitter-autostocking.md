---
navigation:
  parent: example-setups/example-setups-index.md
  title: Автоматическое пополнение излучателем уровня
  icon: level_emitter
---


<a name="level-emitter-autostocking"/>

# Автоматическое пополнение излучателем уровня

Может возникнуть вопрос: "Как поддерживать определённое количество предмета в наличии, создавая больше по мере необходимости?"

Одно из решений — использование <ItemLink id="export_bus" />, <ItemLink id="level_emitter" /> и <ItemLink id="crafting_card" /> для автоматического запроса новых предметов
из [автоматического крафта](../ae2-mechanics/autocrafting.md) вашей сети. Эта настройка предназначена для поддержания большого количества одного предмета.

Вы, конечно, можете заставить вашу сеть постоянно создавать, опустив излучатель уровня и карту красного камня.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/level_emitter_autostocking.snbt" />

  <BoxAnnotation color="#dddddd" min="1 1 0" max="2 1.3 1">
        (1) Шина экспорта: Отфильтрована на желаемый предмет. Имеет Карту красного камня и Карту крафта. Режим красного камня установлен на
        "Активен при сигнале", Поведение крафта установлено на "Не использовать имеющиеся предметы".
        <Row><ItemImage id="redstone_card" scale="2" /> <ItemImage id="crafting_card" scale="2" /></Row>
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="0.7 1 0" max="1 2 1">
        (2) Излучатель уровня: Настроен на желаемый предмет и количество, установлен на "Излучать, когда уровни ниже предела".
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="1 0 0" max="2 1 1">
        (3) Интерфейс: В конфигурации по умолчанию.
  </BoxAnnotation>

<DiamondAnnotation pos="4 0.5 0.5" color="#00ff00">
        В основную сеть
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>


<a name="configurations"/>

## Конфигурации

* <ItemLink id="export_bus" /> (1) отфильтрована на желаемый предмет. У неё есть <ItemLink id="redstone_card" /> и <ItemLink id="crafting_card" />.
  Режим "Красный камень" установлен на "Активен при сигнале", Поведение "Крафт" установлено на "Не использовать имеющиеся предметы".
* <ItemLink id="level_emitter" /> (2) настроен на желаемый предмет и количество, и установлен на "Излучать, когда уровни ниже предела".
* <ItemLink id="interface" /> (3) находится в конфигурации по умолчанию.


<a name="how-it-works"/>

## Как это работает

1. Если количество желаемого предмета в [сетевом хранилище](../ae2-mechanics/import-export-storage.md) ниже количества, указанного в
   <ItemLink id="level_emitter" />, он будет излучать сигнал красного камня.
2. При получении сигнала красного камня (и из-за <ItemLink id="crafting_card" /> и установки на неиспользование имеющихся предметов),
   <ItemLink id="export_bus" /> запросит, чтобы [автоматический крафт](../ae2-mechanics/autocrafting.md) сети создал
   больше желаемого предмета, а затем экспортировал его.
3. При получении предмета (и не будучи настроенным на хранение чего-либо в своём внутреннем инвентаре) <ItemLink id="interface" /> толкнёт этот предмет в сетевое хранилище.
