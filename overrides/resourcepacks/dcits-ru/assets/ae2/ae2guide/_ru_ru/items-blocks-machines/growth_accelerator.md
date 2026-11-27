---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Ускоритель роста
  icon: growth_accelerator
  position: 310
categories:
- machines
item_ids:
- ae2:growth_accelerator
---


<a name="the-growth-accelerator"/>

# Ускоритель роста

<BlockImage id="growth_accelerator" p:powered="true" scale="8"/>

Ускоритель роста значительно ускоряет [рост](../ae2-mechanics/certus-growth.md) истинного кварца или аметиста, когда он установлен рядом с цветущим блоком. Любопытно, что он *также* может ускорять рост различных растений.

Он делает это, применяя «случайные тики» к смежным блокам, в дополнение к случайным тикам, которые происходят естественным образом. В теории это означает, что 1 ускоритель должен заставлять вещи расти примерно в 90 раз быстрее, чем обычно, и эффект суммируется.


<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/growth_accelerator.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Энергию можно подавать сверху или снизу через [кабели](cables.md) AE2 или кабели энергии других модов. Он может принимать как энергию AE2 (AE), так и Forge Energy (FE).

Чтобы подавать на него энергию вручную, поместите <ItemLink id="crank" /> на верхнюю или нижнюю часть и щелкните по нему правой кнопкой мыши.

Верхнюю и нижнюю части можно определить по розовым деталям потока на них.


<GameScene zoom="6" background="transparent">
<ImportStructure src="../assets/assemblies/accelerator_connections.snbt" />
<IsometricCamera yaw="195" pitch="30" />
</GameScene>



<a name="recipe"/>

## Рецепт

<RecipeFor id="growth_accelerator" />
