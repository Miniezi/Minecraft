---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Зарядник
  icon: charger
  position: 310
categories:
- machines
item_ids:
- ae2:charger
---


<a name="the-charger"/>

# Зарядник

<BlockImage id="charger" scale="8" />

Зарядник предоставляет способ зарядки поддерживаемых инструментов и <ItemLink id="certus_quartz_crystal" />.

Энергия может подаваться сверху или снизу, через [кабели](cables.md) AE2 или кабели энергии других модов. Он может принимать как энергию AE2 (AE), так и Forge Energy (FE). Предметы можно вставлять или извлекать с любой стороны. Можно извлекать только результаты, поэтому нет необходимости в фильтрах, чтобы предотвратить извлечение кристаллов истинного кварца вместо заряженных. Может быть повернут <ItemLink id="certus_quartz_wrench" />, чтобы облегчить автоматизацию.

Может использоваться для создания <ItemLink id="charged_certus_quartz_crystal" /> из <ItemLink id="certus_quartz_crystal" />, и <ItemLink id="meteorite_compass" /> из <ItemLink id="minecraft:compass" />.

Чтобы вручную подавать на него энергию, поместите <ItemLink id="crank" /> на верхнюю или нижнюю часть и щелкните по нему правой кнопкой мыши, пока предмет не будет заряжен.

Он также выступает в роли рабочего места для [Исследователя флюкса](fluix_researcher.md).


<a name="simple-automation"/>

## Простая автоматизация

В качестве примера, возможность поворота позволяет полусаматизировать зарядники следующим образом:

<GameScene zoom="4" background="transparent">
  <ImportStructure src="../assets/assemblies/charger_hopper.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>


<a name="recipe"/>

## Рецепт

<RecipeFor id="charger" />
