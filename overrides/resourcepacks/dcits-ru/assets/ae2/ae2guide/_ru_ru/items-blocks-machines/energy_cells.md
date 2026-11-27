---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Энергохранилища
  icon: energy_cell
  position: 110
categories:
- network infrastructure
item_ids:
- ae2:energy_cell
- ae2:dense_energy_cell
- ae2:creative_energy_cell
---


<a name="energy-cells"/>

# Энергохранилища

<Row gap="20">
  <BlockImage id="energy_cell" scale="8" p:fullness="4" />

  <BlockImage id="dense_energy_cell" scale="8" p:fullness="4" />

  <BlockImage id="creative_energy_cell" scale="8" />
</Row>

Энергохранилища увеличивают [энергетическое](../ae2-mechanics/energy.md) хранилище сети. Некоторое количество энергетического буфера помогает сгладить всплески потребления энергии при вставке или извлечении большого количества предметов, а большое количество энергетического хранилища позволяет сети работать, когда энергия не вырабатывается (например, ночью с солнечными панелями) или справляться с огромным мгновенным потреблением энергии [пространственного хранилища](../ae2-mechanics/spatial-io.md).


<a name="fill-bars"/>

## Индикаторы заполнения

<Row>
<BlockImage id="energy_cell" scale="4" p:fullness="0" />
<BlockImage id="energy_cell" scale="4" p:fullness="1" />
<BlockImage id="energy_cell" scale="4" p:fullness="2" />
<BlockImage id="energy_cell" scale="4" p:fullness="3" />
<BlockImage id="energy_cell" scale="4" p:fullness="4" />
</Row>

Полоски сбоку от ячейки соответствуют количеству энергии в ней.


*   0 при заряде ниже 25%
*   1 при заряде от 25% до 50%
*   2 при заряде от 50% до 75%
*   3 при заряде от 75% до 99%
*   4 при заряде выше 99%



<a name="types-of-cell"/>

## Типы ячеек

*   <ItemLink id="energy_cell" /> может хранить 200k AE, и одного должно быть достаточно для большинства случаев использования, легко справляясь с всплесками мощности при нормальном использовании сети.
*   <ItemLink id="dense_energy_cell" /> может хранить 1,6 М AE и используется, когда вы хотите запускать сеть от хранимой энергии или справляться с огромным мгновенным потреблением энергии при больших установках [пространственного хранилища](../ae2-mechanics/spatial-io.md).
*   <ItemLink id="creative_energy_cell" /> — это креативный предмет для тестирования, обеспечивающий БЕСКОНЕЧНУЮ МОЩЬ или что-то подобное.


<a name="recipes"/>

## Рецепты

<Row>
  <RecipeFor id="energy_cell" />

  <RecipeFor id="dense_energy_cell" />
</Row>
