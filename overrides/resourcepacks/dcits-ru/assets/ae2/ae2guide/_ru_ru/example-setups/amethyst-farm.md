---
navigation:
  parent: example-setups/example-setups-index.md
  title: Ферма аметиста
  icon: minecraft:amethyst_shard
---


<a name="farming-of-amethyst"/>

# Выращивание аметиста

Хотя <ItemLink id="growth_accelerator" /> работает и с аметистом, обычные методы фильтрации [бутонов истинного кварца](../items-blocks-machines/budding_certus.md)
с помощью <ItemLink id="annihilation_plane" /> не работают с бутонами аметиста. В отличие от незрелых бутонов истинного кварца, которые падают в виде
<ItemLink id="certus_quartz_dust" />, незрелые бутоны аметиста не падают вообще, поэтому плоскость уничтожения всегда будет их разрушать,
так как сеть всегда может хранить "ничего".

Обход этого решения заключается в зачаровании плоскости уничтожения на Шелковое касание. Тогда незрелые бутоны аметиста *действительно* падают что-то
(различные стадии физических блоков бутонов), и, таким образом, могут быть отфильтрованы.

Затем <ItemLink id="minecraft:amethyst_cluster" /> необходимо снова установить с помощью <ItemLink id="formation_plane" />, чтобы затем снова
разрушить его с помощью <ItemLink id="annihilation_plane" /> без зачарования Шелковое касание, для получения <ItemLink id="minecraft:amethyst_shard" />.

Обратите внимание, что из-за направленности кластера напротив плоскости формирования должна находиться твердая грань блока.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/amethyst_farm.snbt" />

  <BoxAnnotation color="#dddddd" min="2.7 1 1" max="3 2 2">
        (1) Плоскость уничтожения №1: Не имеет интерфейса для настройки, но зачарована на Шелковое касание.
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="2 1 1" max="2.3 2 2">
        (2) Плоскость формирования: Отфильтрована по кластеру аметиста.
        <ItemImage id="minecraft:amethyst_cluster" scale="2" />
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="1.3 0.7 1" max="2 1 2">
        (3) Плоскость уничтожения №2: Не имеет интерфейса для настройки, но может быть зачарована на Удачу.
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="1 0 1" max="1.3 1 2">
        (4) Шина хранения №1: Отфильтрована по осколку аметиста.
        <ItemImage id="minecraft:amethyst_shard" scale="2" />
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="0 0 .7" max="1 1 1">
        (5) Шина хранения №2: Отфильтрована по осколку аметиста. Имеет приоритет выше, чем основное хранилище.
        <ItemImage id="minecraft:amethyst_shard" scale="2" />
  </BoxAnnotation>

<DiamondAnnotation pos="0 0.5 0.5" color="#00ff00">
        К основной сети
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>


<a name="configurations"/>

## Настройки

* Первая <ItemLink id="annihilation_plane" /> (1) не имеет интерфейса и не может быть настроена, но должна быть зачарована на Шелковое касание.
* <ItemLink id="formation_plane" /> (2) отфильтрована по <ItemLink id="minecraft:amethyst_cluster" />.
* Вторая <ItemLink id="annihilation_plane" /> (3) не имеет интерфейса и не может быть настроена, но может быть зачарована на Удачу.
* Первая <ItemLink id="storage_bus" /> (4) отфильтрована по <ItemLink id="minecraft:amethyst_shard" />.
* Вторая <ItemLink id="storage_bus" /> (5) отфильтрована по <ItemLink id="minecraft:amethyst_shard" />, и ее
  [приоритет](../ae2-mechanics/import-export-storage.md#storage-priority) установлен выше, чем у основного хранилища.


<a name="how-it-works"/>

## Как это работает

1. Первая <ItemLink id="annihilation_plane" /> пытается разрушить то, что находится перед ней, но может разрушить только <ItemLink id="minecraft:amethyst_cluster" />, так как единственным хранилищем в подсети является <ItemLink id="formation_plane" />, отфильтрованная по кластеру аметиста. Это работает только потому, что плоскость зачарована на Шелковое касание, в противном случае она могла бы разрушать незрелые бутоны, так как они не падают ничего.
2. <ItemLink id="formation_plane" /> устанавливает кластер на блок, расположенный напротив нее.
3. Вторая <ItemLink id="annihilation_plane" /> разрушает кластер, производя <ItemLink id="minecraft:amethyst_shard" />.
4. Первая <ItemLink id="storage_bus" /> сохраняет осколки в бочке. Технически в фильтрации нет необходимости, так как вторая плоскость уничтожения должна сталкиваться только с полностью выросшими кластерами.
5. Вторая <ItemLink id="storage_bus" /> предоставляет основной сети доступ ко всем осколкам аметиста в бочке. Она настроена на высокий [приоритет](../ae2-mechanics/import-export-storage.md#storage-priority), чтобы осколки аметиста предпочтительно возвращались обратно в бочку, а не в основное хранилище.
