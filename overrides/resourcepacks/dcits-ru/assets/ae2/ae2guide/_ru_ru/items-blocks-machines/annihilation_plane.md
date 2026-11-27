---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Плоскость уничтожения
  icon: annihilation_plane
  position: 210
categories:
- devices
item_ids:
- ae2:annihilation_plane
---


<a name="the-annihilation-plane"/>

# Плоскость уничтожения

<GameScene zoom="8" background="transparent">
<ImportStructure src="../assets/blocks/annihilation_plane.snbt" />
</GameScene>

Плоскость уничтожения разрушает блоки и подбирает предметы. Она работает аналогично <ItemLink id="import_bus" />, помещая вещи в [сетевое хранилище](../ae2-mechanics/import-export-storage.md). Чтобы предметы подбирались, они должны сталкиваться с гранью плоскости; она не подбирает предметы из определённой области.

Плоскости уничтожения можно зачаровывать любыми зачарованиями для кирки, так что, да, вы можете поставить безумные уровни зачарования Удачи и [автоматизировать обработку руды](../example-setups/ore-fortuner.md), если ваш модпак это позволяет. Кроме того, Шелковое касание делает то, чт�� и ожидается, Эффективность снижает затраты энергии на разрушение блока, а Неразрушимость даёт шанс не использовать энергию вообще.

Они являются [подчастями кабеля](../ae2-mechanics/cable-subparts.md).

**НЕ ЗАБУДЬТЕ ВКЛЮЧИТЬ ФЕЙКОВЫХ ИГРОКОВ В ПРЕТЕНЗИИ НА ЧАНК**


<a name="filtering"/>

## Фильтрация

Плоскость уничтожения будет разрушать блок или подбирать предмет только в том случае, если она может сохранить результат (выпадающие предметы) в своей сети. Это означает, что для фильтрации *необходимо ограничить то, что может храниться в её сети*, скорее всего, поместив её в [подсеть](../ae2-mechanics/subnetworks.md). <ItemLink id="storage_bus" /> или [ячейка](../items-blocks-machines/storage_cells.md) могут быть [разделены](cell_workbench.md) для достижения этого.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/annihilation_filtering.snbt" />

  <DiamondAnnotation pos="1 0.5 0.5" color="#00ff00">
        Отфильтровано по любому, что выпадает из вещи, которую вы хотите разрушить.
  </DiamondAnnotation>

  <DiamondAnnotation pos=".5 0.5 2.5" color="#00ff00">
        Разделено по любому, что выпадает из вещи, которую вы хотите разрушить.
  </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Опять же, она фильтрует *по выпадающим предметам*, поэтому, например, если вы хотите фильтровать разрушение <ItemLink id="minecraft:amethyst_cluster" />, вам нужна плоскость, зачарованная Шелковым касанием, иначе на каждом предыдущем этапе роста ничего не выпадает, и плоскость будет разрушать их в любом случае, так как сеть всегда может хранить «ничего».


<a name="recipe"/>

## Рецепт

<RecipeFor id="annihilation_plane" />
