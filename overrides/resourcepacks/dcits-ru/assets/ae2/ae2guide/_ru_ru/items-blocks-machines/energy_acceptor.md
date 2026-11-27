---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Приёмщик энергии
  icon: energy_acceptor
  position: 110
categories:
- network infrastructure
item_ids:
- ae2:energy_acceptor
---


<a name="the-energy-acceptor"/>

# Приёмщик энергии

<Row gap="20">
<BlockImage id="energy_acceptor" scale="8" /> 

<GameScene zoom="8" background="transparent">
  <ImportStructure src="../assets/blocks/cable_energy_acceptor.snbt" />
</GameScene>
</Row>

Приёмщик энергии преобразует распространённые формы энергии из других технических модов во внутреннюю форму [энергии](../ae2-mechanics/energy.md) AE2, AE. Хотя <ItemLink id="controller" /> также может делать это, грани контроллера ценны, поэтому часто лучше использовать приёмщик энергии.


Коэффициенты преобразования Forge Energy и Techreborn Energy следующие:

*   2 FE = 1 AE (Forge)
*   1 E  = 2 AE (Fabric)


Скорость преобразования полностью зависит от того, сколько AE может хранить ваша сеть, по причинам, объяснённым на [этой странице](../ae2-mechanics/energy.md).


<a name="variants"/>

## Варианты

Приёмщики энергии бывают двух разных вариантов: обычные и плоские/[подчасти](../ae2-mechanics/cable-subparts.md). Это позволяет сделать некоторые установки более компактными.

Приёмщики энергии можно переключать между обычными и плоскими в сетке крафта.


<a name="recipe"/>

## Рецепт

<RecipeFor id="energy_acceptor" />

<RecipeFor id="cable_energy_acceptor" />
