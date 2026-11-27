---
navigation:
  parent: example-setups/example-setups-index.md
  title: Автоматический улучшитель руд
  icon: minecraft:raw_iron
---


<a name="automation-of-ore-fortuning"/>

# Автоматизация улучшения руд

< ItemLink id="annihilation_plane" /> можно зачаровать любым зачарованием кирки, включая Удачу, поэтому очевидный вариант использования —
применить Удачу к нескольким, и заставить <ItemLink id="formation_plane" /> и <ItemLink id="annihilation_plane" /> быстро размещать и
разрушать руду.

Обратите внимание, что поскольку <ItemLink id="import_bus" /> "раскручиваются до скорости", настройка будет начинаться медленно, а затем достигнет полной скорости за несколько секунд.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/ore_fortuner.snbt" />

  <BoxAnnotation color="#dddddd" min="2.7 0 2" max="3 1 3">
        (1) Шина импорта: Имеет несколько карт ускорения в себе.
        <ItemImage id="speed_card" scale="2" />
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="0 0 2" max="2 1 2.3">
        (2) Плоскости формирования: В конфигурации по умолчанию.
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="0 0 0.7" max="2 1 1">
        (3) Плоскости уничтожения: Нет GUI для настройки, но зачарованы Удачей.
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="2.7 0 0" max="3 1 1">
        (4) Шина хранения: В конфигурации по умолчанию.
  </BoxAnnotation>

<DiamondAnnotation pos="3.5 0.5 2.5" color="#00ff00">
        Ввод
    </DiamondAnnotation>

<DiamondAnnotation pos="3.5 0.5 0.5" color="#00ff00">
        Вывод
    </DiamondAnnotation>

<DiamondAnnotation pos="4 0.5 1.5" color="#00ff00">
        В основную сеть
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>


<a name="configurations"/>

## Конфигурации

*   <ItemLink id="import_bus" /> (1) имеет несколько <ItemLink id="speed_card" /> в себе. Требуется больше карт по мере увеличения количества плоскостей формирования
    в массиве, так как они заставляют шину импорта извлекать больше предметов за раз.
*   <ItemLink id="formation_plane" /> (2) находятся в конфигурации по умолчанию.
*   <ItemLink id="annihilation_plane" /> (3) не имеют GUI и не могут быть настроены, но зачарованы Удачей.
*   <ItemLink id="storage_bus" /> (4) находится в конфигурации по умолчанию.


<a name="how-it-works"/>

## Как это работает

1.  <ItemLink id="import_bus" /> на зелёной подсети импортирует блоки из первого бочки в [сетевое хранилище](../ae2-mechanics/import-export-storage.md)
2.  Единственное хранилище на зелёной подсети — это <ItemLink id="formation_plane" />, которая размещает блоки.
3.  <ItemLink id="annihilation_plane" /> на оранжевой подсети разрушает блоки, применяя к ним Удачу.
4.  <ItemLink id="storage_bus" /> на оранжевой подсети сохраняет результаты разрушения во второй бочке.
