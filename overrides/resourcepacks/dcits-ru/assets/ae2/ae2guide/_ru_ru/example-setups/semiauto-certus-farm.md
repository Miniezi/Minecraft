---
navigation:
  parent: example-setups/example-setups-index.md
  title: Полуавтоматическая ферма Истинного кварца
  icon: certus_quartz_crystal
  position: 115
---


<a name="semi-auto-certus-farm"/>

# Полуавтоматическая ферма Истинного кварца

К сожалению, [простая ферма Истинного кварца](simple-certus-farm.md) требует <ItemLink id="flawless_budding_quartz" />, чтобы работать полностью
автоматически. Для этого требуется либо [Пространственный ввод/вывод](../ae2-mechanics/spatial-io.md), либо постройка фермы на [метеорите](../ae2-mechanics/meteorites.md).

Однако, AE2 может размещать и разрушать блоки, так что, возможно,
стоит заставить вашу ферму *заменять цветущий кварц за вас*. (Вам нужно будет периодически вставлять <ItemLink id="flawed_budding_quartz" /> в входную бочку и извлекать <ItemLink id="quartz_block" /> из бочки с израсходованным
цветущим кварцем)

Чтобы сделать это полностью автоматически, см. [Расширенную ферму Истинного кварца](advanced-certus-farm.md).

Эта ферма немного сложнее [простой фермы Истинного кварца](simple-certus-farm.md), потому что на самом деле
она состоит из 3 отдельных установок, сжатых вместе.

Смотрите [Рост Истинного кварца](../ae2-mechanics/certus-growth.md) для примерных скоростей.

**ЭТО СЛОЖНАЯ СТРОЙКА С ЭЛЕМЕНТАМИ, СКРЫТЫМИ ЗА ДРУГИМИ ЭЛЕМЕНТАМИ, ОБОЙДИТЕ ВОКРУГ, ЧТОБЫ УВИДЕТЬ СО ВСЕХ СТОРОН**

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/semiauto_certus_farm.snbt" />

  <BoxAnnotation color="#ddaaaa" min="3.7 2 1" max="4 3 2">
        (1) Плоскость уничтожения №1: Нет интерфейса для настройки, но может быть зачарована с помощью Удачи.
  </BoxAnnotation>

  <BoxAnnotation color="#ddaaaa" min="2 2 1" max="2.3 3 2">
        (2) Шина хранения №1: Отфильтрована на Кристалл Истинного кварца.
        <ItemImage id="certus_quartz_crystal" scale="2" />
  </BoxAnnotation>

  <DiamondAnnotation pos="3 2.5 1.5" color="#ff0000">
    Подсеть разрушения кластеров
  </DiamondAnnotation>

  <BoxAnnotation color="#aaddaa" min="3.7 1 1" max="4 2 2">
        (3) Плоскость уничтожения №2: Нет интерфейса для настройки, но зачарована с помощью Шелкового касания.
  </BoxAnnotation>

  <BoxAnnotation color="#aaddaa" min="2 1 1" max="2.3 2 2">
        (4) Шина хранения №2: Отфильтрована на Блок Истинного кварца.
        <BlockImage id="quartz_block" scale="2" />
  </BoxAnnotation>

  <DiamondAnnotation pos="3 1.5 1.5" color="#00ff00">
    Подсеть разрушения блока Истинного кварца
  </DiamondAnnotation>

  <BoxAnnotation color="#ffddaa" min="4 0.7 1" max="5 1 2">
        (5) Плоскость формирования: В конфигурации по умолчанию.
  </BoxAnnotation>

  <BoxAnnotation color="#ffddaa" min="2 0 1" max="2.3 1 2">
        (6) Шина импорта: В конфигурации по умолчанию.
  </BoxAnnotation>

  <DiamondAnnotation pos="3 0.5 1.5" color="#ddcc00">
    Подсеть размещения цветущего блока
  </DiamondAnnotation>

  <BoxAnnotation color="#aaaadd" min="0.7 2 1" max="1 3 2">
        (7) Шина хранения №3: Отфильтрована на Кристалл Истинного кварца. Имеет приоритет выше, чем ваше основное хранилище.
        <ItemImage id="certus_quartz_crystal" scale="2" />
  </BoxAnnotation>

    <DiamondAnnotation pos="1.5 0.5 1.5" color="#00ff00">
        Вставьте вручную цветущий несовершенный блок Истинного кварца.
        <BlockImage id="flawed_budding_quartz" scale="2" />
    </DiamondAnnotation>

    <DiamondAnnotation pos="1.5 1.5 1.5" color="#00ff00">
        Вручную извлеките блок Истинного кварца.
        <BlockImage id="quartz_block" scale="2" />
    </DiamondAnnotation>

<DiamondAnnotation pos="0.5 0.5 0" color="#00ff00">
        В основную сеть
    </DiamondAnnotation>

  <IsometricCamera yaw="165" pitch="5" />
</GameScene>


<a name="configurations"/>

## Настройки

### Разрушение кластеров:

* Первая <ItemLink id="annihilation_plane" /> (1) не имеет интерфейса и не может быть настроена, но может быть зачарована с помощью Удачи.
* Первая <ItemLink id="storage_bus" /> (2) отфильтрована на <ItemLink id="certus_quartz_crystal" />.

### Разрушение блока Истинного кварца:

* Вторая <ItemLink id="annihilation_plane" /> (3) не имеет интерфейса и не может быть настроена, но должна быть зачарована с помощью Шелкового касания.
* Вторая <ItemLink id="storage_bus" /> (4) отфильтрована на <ItemLink id="quartz_block" />.

### Размещение цветущего блока:

* <ItemLink id="formation_plane" /> (5) находится в конфигурации по умолчанию.
* <ItemLink id="import_bus" /> (6) находится в конфигурации по умолчанию.

### На основной сети:

* Третья <ItemLink id="storage_bus" /> (7) отфильтрована на <ItemLink id="certus_quartz_crystal" />, и её
  [приоритет](../ae2-mechanics/import-export-storage.md#storage-priority) установлен выше, чем у вашего основного хранилища.


<a name="how-it-works"/>

## Как это работает

### Разрушение кластеров:

Подсеть разрушения кластеров работает очень похоже на подсеть в [простой ферме Истинного кварца](simple-certus-farm.md).

1. <ItemLink id="annihilation_plane" /> пытается разрушить то, что находится перед ней, но может разрушить только <ItemLink id="quartz_cluster" />
   потому что единственное хранилище в подсети — это <ItemLink id="storage_bus" />, отфильтрованное на <ItemLink id="certus_quartz_crystal" />.
2. <ItemLink id="storage_bus" /> сохраняет кристаллы Истинного кварца в бочке.


<a name="certus-block-breaker"/>

### Разрушение блока Истинного кварца

Подсеть разрушения блока Истинного кварца служит для разрушения израсходованного цветущего блока, как только он превращается в простой <ItemLink id="quartz_block" />.
Она работает похоже на разрушение кластеров.

1. <ItemLink id="annihilation_plane" /> пытается разрушить то, что находится перед ней, но может разрушить только <ItemLink id="quartz_block" />
   потому что единственное хранилище в подсети — это <ItemLink id="storage_bus" />, отфильтрованное на <ItemLink id="quartz_block" />.
   Плоскости нужно иметь шелковое касание, чтобы цветущий блок не деградировал при разрушении, и таким образом плоскость не разрушила его преждевременно.
2. <ItemLink id="storage_bus" /> сохраняет блок Истинного кварца в бочке с израсходованным
   цветущим кварцем, вам нужно будет вручную бросить его в воду с <ItemLink id="charged_certus_quartz_crystal" />, чтобы обновить его.


<a name="budding-block-placer"/>

### Размещение цветущего блока

Подсеть размещения цветущего блока служит для размещения нового <ItemLink id="flawed_budding_quartz" />, когда подсеть разрушения ломает старый израсходованный.

1. <ItemLink id="import_bus" /> импортирует цветущий блок из входной бочки.
2. Единственное хранилище в подсети — это <ItemLink id="formation_plane" />, которая размещает цветущий блок.


<a name="on-the-main-network"/>

### На основной сети

* <ItemLink id="storage_bus" /> предоставляет основной сети (а также [Автоматизации зарядки](charger-automation.md)) доступ ко всем кристаллам Истинного кварца в бочке. Он установлен на
  высокий [приоритет](../ae2-mechanics/import-export-storage.md#storage-priority), чтобы кристаллы Истинного кварца предпочтительно
  возвращались в бочку, а не в ваше основное хранилище.
