---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Шина переключения
  icon: toggle_bus
  position: 110
categories:
- network infrastructure
item_ids:
- ae2:toggle_bus
- ae2:inverted_toggle_bus
---


<a name="the-toggle-bus"/>

# Шина переключения

<GameScene zoom="8" background="transparent">
<ImportStructure src="../assets/assemblies/toggle_bus.snbt" />
<IsometricCamera yaw="195" pitch="30" />
</GameScene>

Шина, которая функционирует подобно <ItemLink id="fluix_glass_cable" /> или другим кабелям, но позволяет переключать состояние её соединения с помощью редстоуна. Это позволяет отключить секцию [сети ME](../ae2-mechanics/me-network-connections.md).

Когда подаётся сигнал редстоуна, часть включает соединение; <ItemLink id="inverted_toggle_bus" /> обеспечивает обратное поведение, отключая соединение.

Следует отметить, что переключение этих шин может вызвать перезагрузку сети и перерасчёт подключённых устройств.

Они являются [частями кабеля](../ae2-mechanics/cable-subparts.md).


<a name="recipes"/>

## Рецепты

<RecipeFor id="toggle_bus" />

<RecipeFor id="inverted_toggle_bus" />
