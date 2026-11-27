---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Контроллер
  icon: controller
  position: 110
categories:
- network infrastructure
item_ids:
- ae2:controller
---


<a name="the-controller"/>

# Контроллер

<BlockImage id="controller" p:state="online" scale="8" />

Контроллер является центром маршрутизации [сети ME](../ae2-mechanics/me-network-connections.md). Без него сеть является «ad-hoc» и может иметь максимум 8 [устройств](../ae2-mechanics/devices.md), использующих каналы.

Невозможно иметь 2 контроллера в одной [сети ME](../ae2-mechanics/me-network-connections.md).

Контроллер предоставляет 32 [канала](../ae2-mechanics/channels.md) на каждую грань.

Контроллеру требуется 6 AE/t на каждый блок контроллера для функционирования. Каждый блок контроллера может хранить 8000 AE, поэтому более крупным сетям может потребоваться дополнительное хранилище энергии. Подробности см. в разделе [энергия](../ae2-mechanics/energy.md).

Многосекционные контроллеры можно строить довольно свободно.

<GameScene zoom="2" background="transparent">
  <ImportStructure src="../assets/assemblies/controllers.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Однако необходимо соблюдать несколько правил:

1.  Все блоки контроллера в [сети ME](../ae2-mechanics/me-network-connections.md) должны быть соединены; в противном случае блоки станут красными.
2.  Размер контроллера должен быть не более 7x7x7; в противном случае он станет красным.
3.  У контроллера может быть 2 смежных блока максимум по одной оси; если блок нарушает это правило, он будет отключён и станет красным.

<GameScene zoom="2" background="transparent">
  <ImportStructure src="../assets/assemblies/controller_rules.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Пока соблюдены все правила и подаётся энергия, контроллер должен светиться и переливаться цветами.

Вы можете щёлкнуть правой кнопкой мыши по контроллеру, чтобы получить тот же графический интерфейс, что и у <ItemLink id="network_tool" />.


<a name="recipe"/>

## Рецепт

<RecipeFor id="controller" />
