---
navigation:
  parent: example-setups/example-setups-index.md
  title: Заполнитель вёдер
  icon: minecraft:water_bucket
---


<a name="bucket-filler"/>

# Заполнитель вёдер

Также смотрите [Опустошитель вёдер](bucket-emptier.md).

Обратите внимание, что поскольку это использует <ItemLink id="pattern_provider" />, он предназначен для интеграции в вашу настройку [автоматического крафта](../ae2-mechanics/autocrafting.md).

Иногда жизнь неудобна, и вам нужны вёдра с жидкостью, а не сама жидкость. Иногда машина может сделать это за вас
(например, Fluid Transposer из Thermal Expansion), но у вас может не всегда быть мод, который делает это удобно для вас. К счастью,
в ванильном Minecraft есть немного менее удобный способ, <ItemLink id="minecraft:dispenser" />.

**Обратите внимание, что вам часто не нужно делать это, потому что Замещение жидкостей в
[Терминале кодирования шаблонов](../items-blocks-machines/terminals.md#pattern-encoding-terminal) позволяет вам использовать саму жидкость в
рецепте крафта вместо ведра.**

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/bucket_filler.snbt" />

<BoxAnnotation color="#dddddd" min="2 1 0" max="3 2 1">
        (1) Поставщик шаблонов: Настроен на блокировку крафта "Сигналом красного камня", с соответствующими шаблонами обработки.

        <Row>
        ![Шаблон заполнения](../assets/diagrams/water_fill_pattern_small.png)
        ![Шаблон заполнения](../assets/diagrams/lava_fill_pattern_small.png)
        </Row>
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="3 1.1 0.1" max="3.2 1.9 0.9">
        (2) Интерфейс: В конфигурации по умолчанию.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="3.1 1.1 0.8" max="3.9 1.9 1">
        (3) Шина хранения №1: В конфигурации по умолчанию.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="4.05 1.05 0.8" max="4.95 1.95 1">
        (4) Плоскость формирования: Отфильтрована на чёрный список вёдер, с использованием карты-инвертера.
        <Row><ItemImage id="minecraft:bucket" scale="2" /><ItemImage id="inverter_card" scale="2" /></Row>
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="3.2 2 1.2" max="3.8 2.2 1.8">
        (5) Шина импорта: Отфильтрована на чёрный список вёдер, с использованием карты-инвертера.
        <Row><ItemImage id="minecraft:bucket" scale="2" /><ItemImage id="inverter_card" scale="2" /></Row>
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="2.1 2 0.1" max="2.9 2.2 0.9">
        (6) Шина хранения №2: В конфигурации по умолчанию.
  </BoxAnnotation>

<DiamondAnnotation pos="0 1.5 0.5" color="#00ff00">
        В основную сеть
    </DiamondAnnotation>

  <IsometricCamera yaw="225" pitch="45" />
</GameScene>


<a name="configurations"/>

## Конфигурации

* <ItemLink id="pattern_provider" /> (1) настроен на блокировку крафта "Сигналом красного камня", с соответствующими <ItemLink id="processing_pattern" />.
  
    ![Шаблон зарядки](../assets/diagrams/water_fill_pattern.png)
    ![Шаблон зарядки](../assets/diagrams/lava_fill_pattern.png)

* <ItemLink id="interface" /> (2) находится в конфигурации по умолчанию.
* Первая <ItemLink id="storage_bus" /> (3) находится в конфигурации по умолчанию.
* <ItemLink id="formation_plane" /> (4) отфильтрована на чёрный список вёдер, с использованием карты-инвертера.
  <Row><ItemImage id="minecraft:bucket" scale="2" /><ItemImage id="inverter_card" scale="2" /></Row>
* <ItemLink id="import_bus" /> (5) отфильтрована на чёрный список вёдер, с использованием карты-инвертера.
  <Row><ItemImage id="minecraft:bucket" scale="2" /><ItemImage id="inverter_card" scale="2" /></Row>
* Вторая <ItemLink id="storage_bus" /> (6) находится в конфигурации по умолчанию.


<a name="how-it-works"/>

## Как это работает

1. <ItemLink id="pattern_provider" /> толкает ингредиенты в <ItemLink id="interface" />.
   (На самом деле, в качестве оптимизации, он толкает напрямую через шину хранения и плоскость формирования, как если бы они были продолжением граней поставщика. Предметы никогда не входят в интерфейс.)
2. Через механизмы, описанные в [подсетях труб](pipe-subnet.md#providing-to-multiple-places) и <ItemLink id="formation_plane" />,
   ведро оказывается в <ItemLink id="minecraft:dispenser" />, а жидкость размещается плоскостью формирования.
3. <ItemLink id="minecraft:comparator" /> обнаруживает ведро в раздатчике и таким образом одновременно подаёт питание на раздатчик и блокирует
   <ItemLink id="pattern_provider" />.
4. Раздатчик забирает жидкость ведром, теперь у него есть наполненное ведро внутри.
5. <ItemLink id="import_bus" /> вытаскивает наполненное ведро из раздатчика и сохраняет его через
   <ItemLink id="storage_bus" /> в поставщике шаблонов, возвращая его в основную сеть.
6. Компаратор видит, что раздатчик пуст, разблокируя поставщика.
