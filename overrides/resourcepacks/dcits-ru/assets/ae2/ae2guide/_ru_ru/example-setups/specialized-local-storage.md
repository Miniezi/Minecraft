---
navigation:
  parent: example-setups/example-setups-index.md
  title: Специализированное локальное хранилище
  icon: drive
---


<a name="specialized-local-storage"/>

# Специализированное локальное хранилище

Используя одно из [специальных поведений интерфейса](../items-blocks-machines/interface.md#special-interactions), [подсеть](../ae2-mechanics/subnetworks.md) может показывать содержимое своего хранилища основной сети, не имея возможности видеть хранилище основной сети и занимая только 1 [канал](../ae2-mechanics/channels.md).

Это полезно для локального хранилища на какой-то ферме, чтобы предметы не переполняли основное хра��илище.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/local_storage.snbt" />

<BoxAnnotation color="#dddddd" min="4 0 0" max="5 2 1">
        (1) Некоторый метод импорта предметов (в данном случае интерфейс)
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="3 0 0" max="4 1 1">
        (2) Накопитель: В нём есть некоторые ячейки. Ячейки должны быть отфильтрованы на то, что выдаёт ферма.
        Ячейки могут иметь Карты равномерного распределения и Карты уничтожения переполнения.
        <Row><ItemImage id="item_storage_cell_4k" scale="2" /> <ItemImage id="equal_distribution_card" scale="2" /> <ItemImage id="void_card" scale="2" /></Row>
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="3 1 0" max="4 2 0.3">
        (3) Терминал крафта: Он может видеть содержимое накопителя на подсети, но не содержимое хранилища вашей основной сети.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="2 0 0" max="2.3 1 1">
        (4) Интерфейс №2: В конфигурации по умолчанию.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1.7 0 0" max="2 1 1">
        (5) Шина хранения: Имеет приоритет выше, чем основное хранилище, может быть отфильтрована на то, что выдаёт ферма.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1 1 0" max="2 2 0.3">
        Терминал крафта: Он может видеть содержимое хранилища основной сети *и* подсети.
  </BoxAnnotation>

<DiamondAnnotation pos="0 0.5 0.5" color="#00ff00">
        В основную сеть
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>


<a name="configurations"/>

## Конфигурации

* Первый <ItemLink id="interface" /> (1) просто принимает предметы от любой фермы, которую вы имеете, и толкает их в подсеть.
* <ItemLink id="drive" /> (2) имеет некоторые [ячейки](../items-blocks-machines/storage_cells.md) в себе. Ячейки должны быть
  [разделены](../items-blocks-machines/cell_workbench.md) на то, что выдаёт ферма.
  Ячейки могут иметь <ItemLink id="equal_distribution_card" /> и <ItemLink id="void_card" />.
* Второй <ItemLink id="interface" /> (4) находится в конфигурации по умолчанию.
* <ItemLink id="storage_bus" /> имеет свой [приоритет](../ae2-mechanics/import-export-storage.md#storage-priority) установленным
  выше, чем основное хранилище. Он может быть отфильтрован на то, что выдаёт ферма.


<a name="how-it-works"/>

## Как это работает

* <ItemLink id="interface" /> на подсети показывает <ItemLink id="storage_bus" /> на основной сети содержимое
<ItemLink id="drive" />. Это означает, что шина хранения может напрямую извлекать предметы из и помещать предметы в ячейки накопителя.
* Шина хранения установлена на высокий [приоритет](../ae2-mechanics/import-export-storage.md#storage-priority), чтобы предметы предпочтительно
  возвращались в подсеть, а не в основное хранилище.
* Важно, что если ячейки в подсети заполнятся, предметы не будут переполнять основную сеть. Если ферма такого типа,
что ломается при переполнении, <ItemLink id="void_card" /> могут использоваться для удаления избыточных предметов.
* Если ферма выдаёт несколько предметов, <ItemLink id="equal_distribution_card" /> могут помешать одному предмету заполнить все ячейки
и не дать другим предметам быть сохранёнными.
