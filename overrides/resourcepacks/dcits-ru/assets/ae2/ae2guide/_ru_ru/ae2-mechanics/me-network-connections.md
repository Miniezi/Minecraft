---
navigation:
  parent: ae2-mechanics/ae2-mechanics-index.md
  title: Соединения сети
  icon: fluix_glass_cable
---


<a name="network-connections"/>

# Соединения сети

## Что означает "Сеть"?

"Сеть" — это группа [устройств](../ae2-mechanics/devices.md), соединённых блоками, которые могут передавать [каналы](../ae2-mechanics/channels.md),
такими как [кабели](../items-blocks-machines/cables.md) или устройства и машины в виде полного блока [устройства](../ae2-mechanics/devices.md). 
(<ItemLink id="charger" />, <ItemLink id="interface" />, <ItemLink id="drive" />, и т.д.)
Технически один кабель — это сеть, на самом деле.


<a name="an-aside-on-device-positioning"/>

## Примечание о позиционировании устройств

Для [устройств](../ae2-mechanics/devices.md), которые имеют определённую сетевую функцию (например, <ItemLink id="interface" />
толкающий и тянущий из [сетевого хранилища](../ae2-mechanics/import-export-storage.md), <ItemLink id="level_emitter" />
считывающий содержимое сетевого хранилища, <ItemLink id="drive" /> являющийся сетевым хранилищем, и т.д.),
физическое положение устройства не имеет значения.

Еще раз, **физическое положение устройства не имеет значения**. Важно только, чтобы устройство было подключено к сети
(и, конечно, к какой сети оно подключено).


<a name="network-connections"/>

## Соединения сети

Простой способ определить, что подключено в сети, — использовать <ItemLink id="network_tool" />. Он покажет каждый
компонент в сети, поэтому если вы видите вещи, которых не должно быть, или не видите вещи, которые должны быть, у вас есть проблема.

Например, это 2 отдельные сети.

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/2_networks_1.snbt" />

  <BoxAnnotation color="#915dcd" min="0 0 0" max="1 2 2">
        Сеть 1
  </BoxAnnotation>

<BoxAnnotation color="#5CA7CD" min="2 0 0" max="3 2 2">
        Сеть 2
  </BoxAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Это также 2 отдельные сети, потому что <ItemLink id="quartz_fiber" /> обменивается [энергией](../ae2-mechanics/energy.md)
без предоставления сетевого соединения.

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/2_networks_2.snbt" />

  <BoxAnnotation color="#915dcd" min="0 0 0" max="1 2 2">
        Сеть 1
  </BoxAnnotation>

  <BoxAnnotation color="#5CA7CD" min="1.3 0 0" max="3 2 2">
        Сеть 2
  </BoxAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Однако, это всего лишь 1 сеть, а не 2 отдельные. [Квантовый мост](../items-blocks-machines/quantum_bridge.md) действует как
беспроводной [плотный кабель](../items-blocks-machines/cables.md#dense-cable), поэтому оба конца находятся в одной сети.

<GameScene zoom="4" background="transparent">
  <ImportStructure src="../assets/assemblies/actually_1_network.snbt" />

  <BoxAnnotation color="#915dcd" min="0 0 0" max="7 3 3">
        Всё одна сеть
  </BoxAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Это также всего лишь 1 сеть, поскольку цвет [кабеля](../items-blocks-machines/cables.md) не имеет отношения к сетевым соединениям, кроме того, что кабели разного ц��ета не
соединяются друг с другом. Все цвета соединяются с флюисовыми (или "бесцветными") кабелями.

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/actually_1_network_2.snbt" />

  <BoxAnnotation color="#915dcd" min="0 0 0" max="4 2 2">
        Вся одна сеть
  </BoxAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>


<a name="connections-in-the-context-of-subnets"/>

## Соединения в контексте подсетей

[Подсети](../ae2-mechanics/subnetworks.md) используют сетевые соединения (и конкретно **НЕ** быть подключёнными)
для ограничения того, к каким другим устройствам имеют доступ [устройства](../ae2-mechanics/devices.md).

По сути, подсеть — это просто отдельная сеть.

Например, возьмём [Автоматический улучшитель руд](../example-setups/ore-fortuner.md). Здесь 3 отдельные сети,
каждая из которых выполняет определённую функцию в настройке.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/ore_fortuner.snbt" />

  <BoxAnnotation color="#915dcd" min="0 0 2" max="3 1 3">
        Сеть 1, действует как подсеть труб, ограничивает доступ шины импорта, поэтому она "хранит" блоки руды через
        плоскости формирования.
  </BoxAnnotation>

  <BoxAnnotation color="#5CA7CD" min="0 0 0" max="3 1 1">
        Сеть 2, Действует как ещё одна подсеть труб, ограничивает доступ плоскостей уничтожения, поэтому они хранят
        улучшенные куски руды в бочке, а не в вашей основной сети. Также означает, что они не используют никаких каналов в
        основной сети.
  </BoxAnnotation>

  <BoxAnnotation color="#82CD5C" min="2 0 1" max="4 1 2">
        Сеть 3, Основная сеть со всем вашим хранилищем и крафтом на ней. Просто здесь для подачи энергии, на самом деле, и конкретно
        *не* подключена к 2 подсетям.
  </BoxAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>


<a name="connections-in-the-context-of-p2p"/>

## Соединения в контексте P2P

Один вариант [Тоннеля точка-точка](../items-blocks-machines/p2p_tunnels.md) перемещает [Каналы](channels.md) вместо предметов, жидкостей
или сигнала красного камня, и это почему-то сбивает людей с толку. Сеть, к которой подключён тоннель, не имеет никакого отношения к
сети, которую тоннель передаёт. они *могут* быть одной сетью, но не обязаны, и обычно не являются.

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/p2p_channels_network_connection.snbt" />

  <BoxAnnotation color="#915dcd" min="0 0 0" max="1.98 2 1">
        Сеть 1, сеть, которая передаётся (обычно ваша основная сеть)
  </BoxAnnotation>

  <BoxAnnotation color="#5CA7CD" min="2.02 0 0" max="3.98 1 1">
        Сеть 2, Сеть, управляющая тоннелями ME P2P (обычно *не* ваша основная сеть)
  </BoxAnnotation>

  <BoxAnnotation color="#915dcd" min="4.02 0 0" max="6 1 1">
        Сеть 1, сеть, которая передаётся (обычно ваша основная сеть)
  </BoxAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>


<a name="less-intuitive-connections"/>

## Менее интуитивные соединения

В этом случае это всего лишь 1 сеть, потому что <ItemLink id="pattern_provider" />, будучи устройством в виде полного блока, действует как
кабель, и <ItemLink id="inscriber" /> делает то же самое. Таким образом, сетевое соединение проходит через
поставщика и высекатель.

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/pattern_provider_network_connection_1.snbt" />

  <BoxAnnotation color="#915dcd" min="0 0 0" max="4 2 2">
        Вся одна сеть
  </BoxAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Чтобы предотвратить это (полезно для многих настроек автоматического крафта, включающих [подсети](../ae2-mechanics/subnetworks.md)),
вы можете щелкнуть по поставщику правой кнопкой мыши с помощью <ItemLink id="certus_quartz_wrench" />, чтобы сделать его направленным, в этом случае он не будет
передавать каналы через одну сторону.

<Row gap="40">
<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/pattern_provider_network_connection_2.snbt" />

  <BoxAnnotation color="#915dcd" min="0 0 0" max="1.98 2 2">
        Сеть 1
  </BoxAnnotation>

  <BoxAnnotation color="#5CA7CD" min="2.02 0 0" max="4 2 2">
        Сеть 2
  </BoxAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/pattern_provider_directional_connection.snbt" />

  <BoxAnnotation color="#ee3333" min="1 .3 .3" max="1.3 .7 .7">
        Обратите внимание, как кабель не соединяется
  </BoxAnnotation>

  <IsometricCamera yaw="255" pitch="30" />
</GameScene>
</Row>

Другие части, которые не обеспечивают направленные сетевые соединения, — это большинство [подчастей](../ae2-mechanics/cable-subparts.md)
[устройств](../ae2-mechanics/devices.md), таких как <ItemLink id="import_bus" />, <ItemLink id="storage_bus" /> и
<ItemLink id="cable_interface" />.
