---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Молекулярный сборщик
  icon: molecular_assembler
  position: 310
categories:
- machines
item_ids:
- ae2:molecular_assembler
---


<a name="the-molecular-assembler"/>

# Молекулярный сборщик

<BlockImage id="molecular_assembler" scale="8" />

Молекулярный сборщик принимает предметы и выполняет операцию, определённую смежным <ItemLink id="pattern_provider" />, или вставленным <ItemLink id="crafting_pattern" />, <ItemLink id="smithing_table_pattern" /> или <ItemLink id="stonecutting_pattern" />, затем помещает результат в смежные инвентари.


Этот сборщик имеет шаблон создания, который определяет рецепт 1 дубовый бревно = 4 дубовые доски. Когда дубовые брёвна подаются в верхний желоб, сборщик создаёт и выбрасывает дубовые доски в нижний желоб.


<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/standalone_assembler.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>



<a name="the-main-use-of-the-molecular-assembler"/>

## Основное использование молекулярного сборщика

Однако их основное использование — рядом с <ItemLink id="pattern_provider" />. Поставщики шаблонов ведут себя по-особенному в этом случае и будут отправлять информацию о соответствующем шаблоне вместе с ингредиентами в смежные сборщики. Поскольку сборщики автоматически выбрасывают результаты создания в смежные инвентари (и, таким образом, в возвратные слоты поставщика шаблонов), для автоматизации шаблонов создания достаточно сборщика на поставщике шаблонов.


<GameScene zoom="4" background="transparent">
  <ImportStructure src="../assets/assemblies/assembler_tower.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>



<a name="upgrades"/>

## Улучшения

Молекулярный сборщик поддерживает следующие [улучшения](upgrade_cards.md):

*   <ItemLink id="speed_card" />


<a name="recipe"/>

## Рецепт

<RecipeFor id="molecular_assembler" />


<a name="note"/>

## Примечание

Optifine ломает функцию «вталкивания в смежные инвентари», поэтому большинство установок для создания со сборщиками не будут работать.
