---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Квантовый мост
  icon: quantum_ring
  position: 110
categories:
- network infrastructure
item_ids:
- ae2:quantum_link
- ae2:quantum_ring
---


<a name="the-quantum-network-bridge"/>

# Квантовый мост сети

![Сформированный квантовый мост сети](../assets/diagrams/quantum_bridge_demonstration.png)

Квантовые мосты сети могут расширять [сеть](../ae2-mechanics/me-network-connections.md) на бесконечные расстояния и даже между измерениями. Они могут передавать 32 канала в общей сложности (независимо от того, как кабели подключены к каждой грани), по сути действуя как беспроводной [плотный кабель](cables.md#dense-cable).

<GameScene zoom="4" background="transparent">
  <ImportStructure src="../assets/assemblies/quantum_bridge_internal_structure_1.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

<GameScene zoom="4" background="transparent">
  <ImportStructure src="../assets/assemblies/quantum_bridge_internal_structure_2.snbt" />

  <BoxAnnotation color="#33dd33" min="1 1 1" max="6 2 3">
        Воображаемый кабель между двумя конечными точками
  </BoxAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Примечательно, что **обе стороны должны быть загружены в чанке**, поэтому <ItemLink id="spatial_anchor" /> или другой загрузчик чанков должен использоваться, если две стороны находятся далеко друг от друга.



<a name="quantum-ring"/>

# Квантовое кольцо

<BlockImage id="quantum_ring" scale="8" />

Восемь этих блоков, размещённых вокруг <ItemLink id="quantum_link" />, создадут квантовый мост сети. Только 4 блока <ItemLink id="quantum_ring" />, смежные с <ItemLink id="quantum_link" />, будут принимать сетевые подключения, 4 угловых блока не могут подключаться к кабелям.



<a name="recipe"/>

## Рецепт

<RecipeFor id="quantum_ring" />



<a name="quantum-link-chamber"/>

# Камера квантовой связи

<BlockImage id="quantum_link" scale="8" />

Один из этих блоков, окружённый <ItemLink id="quantum_ring" />, создаст квантовый мост сети. Этот блок не подключается ни к каким кабелям и регистрируется как часть сети только при полном создании моста. Инвентарь этого блока может содержать только один <ItemLink id="quantum_entangled_singularity" /> и доступен для автоматизации.



<a name="recipe"/>

## Рецепт

<RecipeFor id="quantum_link" />
