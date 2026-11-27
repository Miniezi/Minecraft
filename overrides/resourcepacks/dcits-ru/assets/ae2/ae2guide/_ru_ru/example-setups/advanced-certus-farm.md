---
navigation:
  parent: example-setups/example-setups-index.md
  title: Расширенная ферма Истинного кварца
  icon: certus_quartz_crystal
  position: 120
---


<a name="advanced-certus-farm"/>

# Расширенная ферма Истинного кварца

По сути, это просто [Полуавтоматическая ферма Истинного кварца](semiauto-certus-farm.md), но она полностью интегрирована в вашу
ME-сеть.

Вместо того, чтобы иметь большой запас блоков с цветущим истинным кварцем и вручную обновлять их время от времени,
эта установка использует [Автоматизацию зарядки](charger-automation.md) и [Автоматизацию погружения в воду](throw-in-water-automation.md)
для автоматического выполнения этих задач.

См. [Рост истинного кварца](../ae2-mechanics/certus-growth.md) для оценки скорости.

**ЭТО СЛОЖНАЯ СБОРКА С ЭЛЕМЕНТАМИ, СКРЫТЫМИ ЗА ДРУГИМИ ЭЛЕМЕНТАМИ, ОБОЙДИТЕ ВОКРУГ, ЧТОБЫ РАССМОТРЕТЬ ЕЁ СО ВСЕХ СТОРОН**

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/advanced_certus_farm.snbt" />

  <BoxAnnotation color="#ddaaaa" min="3.7 2 1" max="4 3 2">
        (1) Плоскость уничтожения №1: Не имеет интерфейса для настройки, но может быть зачарована на Удачу.
  </BoxAnnotation>

  <BoxAnnotation color="#ddaaaa" min="2 2 1.7" max="3 3 2">
        (2) Шина хранения №1: Отфильтрована на Кристалл Истинного кварца.
        <ItemImage id="certus_quartz_crystal" scale="2" />
  </BoxAnnotation>

  <DiamondAnnotation pos="3 2.5 1.5" color="#ff0000">
    Подсеть дробления кластера
  </DiamondAnnotation>

  <BoxAnnotation color="#aaddaa" min="3.7 1 1" max="4 2 2">
        (3) Плоскость уничтожения №2: Не имеет интерфейса для настройки, но зачарована на Шелковое касание.
  </BoxAnnotation>

  <BoxAnnotation color="#aaddaa" min="2 1 1.7" max="3 2 2">
        (4) Шина хранения №2: Отфильтрована на Блок Истинного кварца.
        <BlockImage id="quartz_block" scale="2" />
  </BoxAnnotation>

  <DiamondAnnotation pos="3 1.5 1.5" color="#00ff00">
    Подсеть дробления блока Истинного кварца
  </DiamondAnnotation>

  <BoxAnnotation color="#ffddaa" min="4 0.7 1" max="5 1 2">
        (5) Плоскость формирования: В конфигурации по умолчанию.
  </BoxAnnotation>

  <BoxAnnotation color="#ffddaa" min="2 0.7 2" max="3 1 3">
        (6) Шина импорта: Отфильтрована на цветущий несовершенный блок Истинного кварца.
        <BlockImage id="flawed_budding_quartz" scale="2" />
  </BoxAnnotation>

  <DiamondAnnotation pos="3 0.5 1.5" color="#ddcc00">
    Подсеть установки блоков с цветущим кварцем
  </DiamondAnnotation>

  <BoxAnnotation color="#aaaadd" min="1.7 2 2" max="2 3 3">
        (7) Шина хранения №3: Отфильтрована на Кристалл Истинного кварца. Имеет приоритет выше, чем основное хранилище.
        <ItemImage id="certus_quartz_crystal" scale="2" />
  </BoxAnnotation>

  <BoxAnnotation color="#aaaadd" min="2 1 2" max="3 2 3">
        (8) Интерфейс: Настроен на хранение 1 блока цветущего истинного кварца с дефектами, оснащен картой изготовления.
        <Row><BlockImage id="flawed_budding_quartz" scale="2" /> <ItemImage id="crafting_card" scale="2" /></Row>
  </BoxAnnotation>

<DiamondAnnotation pos="1.5 0.5 0" color="#00ff00">
        К основной сети, автоматизации зарядки и автоматизации погружения в воду
        <Row>
        <GameScene zoom="3" background="transparent">
          <ImportStructure src="../assets/assemblies/charger_automation.snbt" />
          <IsometricCamera yaw="195" pitch="30" />
        </GameScene>
        <GameScene zoom="3" background="transparent">
          <ImportStructure src="../assets/assemblies/throw_in_water.snbt" />
          <IsometricCamera yaw="195" pitch="30" />
        </GameScene>
        </Row>
    </DiamondAnnotation>

  <IsometricCamera yaw="165" pitch="5" />
</GameScene>


<a name="configurations"/>

## Настройки

### Дробление кластера:

* Первая <ItemLink id="annihilation_plane" /> (1) не имеет интерфейса и не может быть настроена, но может быть зачарована на Удачу.
* Первая <ItemLink id="storage_bus" /> (2) отфильтрована на <ItemLink id="certus_quartz_crystal" />.

### Дробление блока Истинного кварца:

* Вторая <ItemLink id="annihilation_plane" /> (3) не имеет интерфейса и не может быть настроена, но должна быть зачарована на Шелковое касание.
* Вторая <ItemLink id="storage_bus" /> (4) отфильтрована на <ItemLink id="quartz_block" />.

### Установка блоков с цветущим кварцем:

* <ItemLink id="formation_plane" /> (5) находится в конфигурации по умолчанию.
* <ItemLink id="import_bus" /> (6) отфильтрована на <ItemLink id="flawed_budding_quartz" />.

### На основной сети:

* Третья <ItemLink id="storage_bus" /> (7) отфильтрована на <ItemLink id="certus_quartz_crystal" />, и ее
  [приоритет](../ae2-mechanics/import-export-storage.md#storage-priority) установлен выше, чем у основного хранилища.
* <ItemLink id="interface" /> (8) настроен на хранение 1 блока цветущего истинного кварца с дефектами и оснащен <ItemLink id="crafting_card" />.


<a name="how-it-works"/>

## Как это работает

### Дробление кластера:

Подсеть дробления кластера работает очень похоже на подсеть в [простой ферме истинного кварца](simple-certus-farm.md).

1. <ItemLink id="annihilation_plane" /> пытается разрушить то, что находится перед ней, но может разрушить только <ItemLink id="quartz_cluster" />, так как единственным хранилищем в подсети является <ItemLink id="storage_bus" />, отфильтрованная по <ItemLink id="certus_quartz_crystal" />.
2. <ItemLink id="storage_bus" /> сохраняет кристаллы истинного кварца в бочке.


<a name="certus-block-breaker"/>

### Дробление блока Истинного кварца

Подсеть дробления блока Истинного кварца служит для разрушения исчерпанного блока с цветущим кварцем, когда он превращается в обычный <ItemLink id="quartz_block" />. Она работает аналогично дробилке кластера.

1. <ItemLink id="annihilation_plane" /> пытается разрушить то, что находится перед ней, но может разрушить только <ItemLink id="quartz_block" />, так как единственным хранилищем в подсети является <ItemLink id="storage_bus" />, отфильтрованная по <ItemLink id="quartz_block" />. Плоскости необходимо зачарование Шелковое касание, чтобы при разрушении блок не деградировал, и таким образом плоскость не разрушила его преждевременно.
2. <ItemLink id="storage_bus" /> сохраняет блок истинного кварца в <ItemLink id="interface" />, позволяя [Автоматизации погружения в воду](throw-in-water-automation.md) использовать его для создания нового <ItemLink id="flawed_budding_quartz" />.


<a name="budding-block-placer"/>

### Установка блоков с цветущим кварцем

Подсеть установки блоков с цветущим кварцем служит для установки нового <ItemLink id="flawed_budding_quartz" /> после того, как подсеть дробления разрушила старый, исчерпанный блок.

1. <ItemLink id="import_bus" /> импортирует блок с цветущим кварцем из <ItemLink id="interface" /> в [сетевое хранилище](../ae2-mechanics/import-export-storage.md)
2. Единственным хранилищем в подсети является <ItemLink id="formation_plane" />, которая и устанавливает блок.


<a name="on-the-main-network"/>

### На основной сети

* <ItemLink id="storage_bus" /> предоставляет основной сети (а также [Автоматизации зарядки](charger-automation.md)) доступ ко всем кристаллам истинного кварца в бочке. Она настроена на
  высокий [приоритет](../ae2-mechanics/import-export-storage.md#storage-priority), чтобы кристаллы истинного кварца предпочтительно
  возвращались обратно в бочку, а не в основное хранилище.
* <ItemLink id="interface" /> предоставляет подсети установки блоков с цветущим кварцем доступ к <ItemLink id="flawed_budding_quartz" />, и
    дает подсети дробления блока Истинного кварца способ вернуть исчерпанные блоки обратно в основную сеть. <ItemLink id="crafting_card" /> позволяет интерфейсу запрашивать новые блоки с цветущим кварцем из [автоматического крафта](../ae2-mechanics/autocrafting.md) основной сети.
