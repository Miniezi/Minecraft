---
navigation:
  title: "Многоблоки с партией"
  icon: "processing_array"
  parent: extended_industrialization:machines.md
categories:
  - machines
item_ids:
  - extended_industrialization:large_steam_furnace
  - extended_industrialization:large_steam_macerator
  - extended_industrialization:large_electric_furnace
  - extended_industrialization:large_electric_macerator
  - extended_industrialization:processing_array
---


<a name="batching-multiblocks"/>

# Многоблоки с партией

Некоторые мультиблоки способны функционировать как нормальная машина своего типа, но с применением мультипликатора к
Это означает, что мультиблок, который способен выполнять данный рецепт
Вы можете использовать множество входов одновременно, и затем получить результаты.
Количество EU/t, потребляемое машиной, также умножится на количество
партий, которые в настоящее время работают.

Количество партий, на которых может работать многоблок партии, варьируется.
Машина.

Точно так же, как работают другие машины, они не могут выполнять более одного рецепта одновременно.


<a name="large-furnace"/>

## Большой печь

Количество партий, которые может работать Большая электрическая печь, определяется катушками, используемыми в многоблоке, аналогичными
Для размеров партий, обеспечиваемых катушкой в печке, см.
инструментальный кончик на данной катушке.

<Row>
	<RecipeFor id="extended_industrialization:large_steam_furnace" />
	<RecipeFor id="extended_industrialization:large_electric_furnace" />
</Row>

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="extended_industrialization:large_steam_furnace" />
    <MultiblockShape controller="extended_industrialization:large_electric_furnace" x="-6" y="-1" z="-6" />
</GameScene>


<a name="large-macerator"/>

## Большой мацератор

<Row>
	<RecipeFor id="extended_industrialization:large_steam_macerator" />
	<RecipeFor id="extended_industrialization:large_electric_macerator" />
</Row>

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="extended_industrialization:large_steam_macerator" />
    <MultiblockShape controller="extended_industrialization:large_electric_macerator" x="-6" z="-6" />
</GameScene>


<a name="processing-array"/>

## Сборник обработки

Перерабатывающий массив способен управлять партиями любой не многоблоковой электрической машины, которая поставляется в
Размеры партий, на которых может работать обрабатывающий массив, ограничены его размером и количеством машин.
поместили в него.

<RecipeFor id="extended_industrialization:processing_array" />

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="extended_industrialization:processing_array" />
    <MultiblockShape controller="extended_industrialization:processing_array" useBigShape={true} x="-6" z="-8" />
</GameScene>
