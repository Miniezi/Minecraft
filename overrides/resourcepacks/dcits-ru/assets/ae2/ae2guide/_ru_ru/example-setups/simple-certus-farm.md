---
navigation:
  parent: example-setups/example-setups-index.md
  title: Простая ферма Истинного кварца
  icon: certus_quartz_crystal
  position: 110
---


<a name="simple-certus-farm"/>

# Простая ферма Истинного кварца

Как упоминалось в [Рост Истинного кварца](../ae2-mechanics/certus-growth.md), автоматизация сбора <ItemLink id="certus_quartz_crystal" />
включает в себя <ItemLink id="annihilation_plane" /> и <ItemLink id="storage_bus" />. 
<ItemLink id="growth_accelerator" /> используются для значительного ускорения роста бутонов Истинного кварца, а затем плоскости
разрушают полностью выросшие <ItemLink id="quartz_cluster" />. Они фильтруются за счёт использования подозрительно удачного свойства, что незрелые
бутонов Истинного кварца падают в виде <ItemLink id="certus_quartz_dust" /> вместо того, чтобы не давать ничего.

Эта ферма работает полностью автоматически с <ItemLink id="flawless_budding_quartz" />, но с несовершенными, потрескавшимися и повреждёнными
цветущими блоками Истинного кварца вам придётся вручную заменять цветущий блок. Или, как описано в [Полуавтоматической ферме Истинного кварца](semiauto-certus-farm.md)
и [Расширенной ферме Истинного кварца](advanced-certus-farm.md), автоматически.

Смотрите [Рост Истинного кварца](../ae2-mechanics/certus-growth.md) для примерных скоростей.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/simple_certus_farm.snbt" />

  <BoxAnnotation color="#dddddd" min="3.7 1 1" max="4 2 2">
        (1) Плоскость уничтожения: Нет интерфейса для настройки, но может быть зачарована с помощью Удачи.
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="3 1 1" max="3.3 2 2">
        (2) Шина хранения №1: Отфильтрована на Кристалл Истинного кварца.
        <ItemImage id="certus_quartz_crystal" scale="2" />
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="3 1 .7" max="2 2 1">
        (3) Шина хранения №2: Отфильтрована на Кристалл Истинного кварца. Имеет приоритет выше, чем основное хранилище.
        <ItemImage id="certus_quartz_crystal" scale="2" />
  </BoxAnnotation>

<DiamondAnnotation pos="1 0.5 0.5" color="#00ff00">
        В основную сеть
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>


<a name="configurations"/>

## Настройки

* Первая <ItemLink id="annihilation_plane" /> (1) не имеет интерфейса и не может быть настроена, но может быть зачарована с помощью Удачи.
* Первый <ItemLink id="storage_bus" /> (2) отфильтрован на <ItemLink id="certus_quartz_crystal" />.
* Второй <ItemLink id="storage_bus" /> (3) отфильтрован на <ItemLink id="certus_quartz_crystal" />, и его
  [приоритет](../ae2-mechanics/import-export-storage.md#storage-priority) установлен выше, чем у основного хранилища.


<a name="how-it-works"/>

## Как это работает

1. <ItemLink id="annihilation_plane" /> пытается разрушить то, что находится перед ней, но может разрушить только <ItemLink id="quartz_cluster" />
   потому что единственное хранилище в подсети — это <ItemLink id="storage_bus" />, отфильтрованное на <ItemLink id="certus_quartz_crystal" />.
4. Первый <ItemLink id="storage_bus" /> сохраняет кристаллы Истинного кварца в бочке.
5. Второй <ItemLink id="storage_bus" /> предоставляет основной сети доступ ко всем кристаллам Истинного кварца в бочке. Он установлен на
   высокий [приоритет](../ae2-mechanics/import-export-storage.md#storage-priority), чтобы кристаллы Истинного кварца предпочтительно
   возвращались в бочку, а не в ваше основное хранилище.
