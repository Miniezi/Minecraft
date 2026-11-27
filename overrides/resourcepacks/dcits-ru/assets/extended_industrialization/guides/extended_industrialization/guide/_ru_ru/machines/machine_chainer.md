---
navigation:
  title: "Машинная цепь"
  icon: "machine_chainer"
  parent: extended_industrialization:machines.md
categories:
  - machines
item_ids:
  - extended_industrialization:machine_chainer
  - extended_industrialization:machine_chainer_relay
---


<a name="machine-chainer"/>

# Машинная цепь

<GameScene zoom="2" interactive={true} fullWidth={true}>
	<ImportStructure src="machine_chainer_example.nbt" />
	<IsometricCamera yaw="180" pitch="30" />
</GameScene>

Machine Chainer может подключаться к многим машинам, бочкам или любому другому блоку инвентаря, помеченному как
`#extended_industrialization:machine_chainer/linkable` в прямой линии до 64 кварталов.
В цепочке можно ориентироваться в любом направлении, в том числе вверх.
и вниз.

При этом передача энергии ограничена в 3 раза быстрее, чем передача энергии.
одного кабеля соответствующего уровня и не может взаимодействовать с энергией связанных машин, не соответствующих его
напряжение.


<a name="machine-chainer-relay"/>

## Реле машины для цепи

Реле - это блок, к которому может быть привязан цепь, но не имеет запаса.
при этом не требуется, чтобы вы помещали машину в пустоту.

<RecipeFor id="extended_industrialization:machine_chainer_relay" />
