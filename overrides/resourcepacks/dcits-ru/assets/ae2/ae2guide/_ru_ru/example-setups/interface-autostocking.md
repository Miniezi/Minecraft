---
navigation:
  parent: example-setups/example-setups-index.md
  title: Автоматическое пополнение интерфейса
  icon: interface
---


<a name="interface-autostocking"/>

# Автоматическое пополнение интерфейса

Может возникнуть вопрос: "Как поддерживать определённое количество различных предметов в наличии, создавая больше по мере необходимости?"

Одно из решений — использование <ItemLink id="interface" /> и <ItemLink id="crafting_card" /> для автоматического запроса новых предметов
из [автоматического крафта](../ae2-mechanics/autocrafting.md) вашей сети. Эта настройка больше подходит для поддержания небольшого количества широкого
разнообразия предметов.

Демонстрационная настройка укорочена, чтобы не быть слишком широкой, вероятно, наиболее оптимально использовать 4 <ItemLink id="interface" /> и 4 <ItemLink id="storage_bus" />,
чтобы использовать все 8 [каналов](../ae2-mechanics/channels.md) в обычном [кабеле](../items-blocks-machines/cables.md).

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/interface_autostocking.snbt" />

<BoxAnnotation color="#dddddd" min="0 0 0" max="2 1 1">
        (1) Интерфейсы: Настроены на хранение желаемых предметов в себе. У них есть Карты крафта.
        <ItemImage id="crafting_card" scale="2" />
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="0 1 0" max="2 1.3 1">
        (2) Шины хранения: Режим "Ввод/вывод" установлен на "Только извлечение".
  </BoxAnnotation>

<DiamondAnnotation pos="4 0.5 0.5" color="#00ff00">
        В основную сеть
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>


<a name="configurations"/>

## Конфигурации

* <ItemLink id="interface" /> (1) настроены на хранение желаемых предметов в себе, щелчком по желаемому предмету в их
   верхние слоты или перетаскиванием в верхние слоты из JEI, затем щелчком по значку гаечного ключа над слотами, чтобы установить количество. У них есть <ItemLink id="crafting_card" />.
* <ItemLink id="storage_bus" /> (2) настроены так, что режим "Ввод/вывод" установлен на "Только извлечение".


<a name="how-it-works"/>

## Как это работает

1. Если <ItemLink id="interface" /> не может извлечь достаточно настроенного предмета из [сетевого хранилища](../ae2-mechanics/import-export-storage.md),
   (и у него есть <ItemLink id="crafting_card" />), он запросит, чтобы [автоматический крафт](../ae2-mechanics/autocrafting.md) сети создал больше этого предмета.
2. <ItemLink id="storage_bus" /> позволяют сети получить доступ к содержимому интерфейсов.
