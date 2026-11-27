---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Резонирующая камера
  icon: vibration_chamber
  position: 110
categories:
- network infrastructure
item_ids:
- ae2:vibration_chamber
---


<a name="the-vibration-chamber"/>

# Резонирующая камера

<BlockImage id="vibration_chamber" p:active="true" scale="8" />

Хотя основным предназначаемым методом подачи [энергии](../ae2-mechanics/energy.md) в вашу сеть является <ItemLink id="energy_acceptor" />, резонирующая камера может напрямую генерировать небольшие до средних количеств AE.

По умолчанию (без [улучшений](upgrade_cards.md) и с настройками по умолчанию) она производит 40 AE/t.

Когда [энергия](../ae2-mechanics/energy.md) в хранилище сети заполнена, резонирующая камера снижает свою мощность для экономии топлива, но не может полностью выключиться.


<a name="settings"/>

## Настройки

*   Резонирующая камера предоставляет доступ к глобальной настройке для отображения энергии в AE или E/FE.


<a name="upgrades"/>

## Улучшения

Резонирующая камера поддерживает следующие [улучшения](upgrade_cards.md):

*   <ItemLink id="energy_card" /> увеличивает эффективность камеры на +50%, максимум до +150% или 250% от базовой эффективности.
*   <ItemLink id="speed_card" /> увеличивает скорость сжигания камеры на +50%, максимум до +150% или 250% от базовой выходной мощности.


<a name="config"/>

## Конфигурация

Свойства резонирующей камеры можно изменить в common.json в папке ae2 в папке config в каталоге .minecraft.

*   baseEnergyPerFuelTick устанавливает базовую, неулучшенную эффективность резонирующей камеры.
*   minEnergyPerGameTick устанавливает самый низкий возможный уровень генерации энергии (камера всегда будет медленно использовать некоторое топливо, даже если сети не требуется энергия).
*   maxEnergyPerGameTick устанавливает максимальный неулучшенный выход (и скорость) резонирующей камеры.


<a name="recipe"/>

## Рецепт

<RecipeFor id="vibration_chamber" />
