---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Цветущий истинный кварц
  icon: flawless_budding_quartz
  position: 010
categories:
- misc ingredients blocks
item_ids:
- ae2:flawless_budding_quartz
- ae2:flawed_budding_quartz
- ae2:chipped_budding_quartz
- ae2:damaged_budding_quartz
- ae2:small_quartz_bud
- ae2:medium_quartz_bud
- ae2:large_quartz_bud
- ae2:quartz_cluster
---


<a name="budding-certus-quartz"/>

# Цветущий истинный кварц

(также см. [Рост истинного кварца](../ae2-mechanics/certus-growth.md))

<GameScene zoom="4" background="transparent">
  <ImportStructure src="../assets/assemblies/budding_blocks.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Кварцевые бутоны будут появляться из цветущих блоков истинного кварца, аналогично аметисту. Эти блоки находятся в [метеоритах](../ae2-mechanics/meteorites.md). Существует 4 уровня цветущего кварца: Безупречный, Несовершенный, Потрескавшийся и Повреждённый. Их можно легко определить с помощью мода, такого как HWYLA, Jade, The One Probe и т.д. (или экрана f3).

У Несовершенного, Потрескавшегося и Повреждённого цветущего кварца каждый раз, когда бутон переходит на следующий этап роста, цветущий блок имеет шанс понизиться на один уровень, в конечном итоге превратившись в обычный <ItemLink id="quartz_block" />.

Безупречный цветущий кварц не будет понижаться при росте бутонов и служит бесконечным источником.

Если разрушить цветущие блоки кварца обычной киркой, они понизятся на 1 уровень. Если разрушить их киркой, зачарованной Шелковым касанием, они не понизятся, если только это не был Безупречный. **Это означает, что Безупречные цветущие блоки кварца нельзя подобрать и переместить с помощью кирки**. Вместо этого для перемещения Безупречных цветущих блоков можно использовать [Пространственное хранилище](../ae2-mechanics/spatial-io.md).


<a name="recipes"/>

## Рецепты

Несовершенный, Потрескавшийся и Повреждённый цветущий кварц можно создать, бросив цветущий блок предыдущего уровня (или <ItemLink id="quartz_block" />) в воду с одним или несколькими <ItemLink id="charged_certus_quartz_crystal" />.

Безупречный цветущий кварц нельзя создать, его можно только найти в мире.

<Row>
  <RecipeFor id="damaged_budding_quartz" />

  <RecipeFor id="chipped_budding_quartz" />

  <RecipeFor id="flawed_budding_quartz" />
</Row>
