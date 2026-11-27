---
navigation:
  parent: ae2wtlib/ae2wtlib-index.md
  title: Беспроводные терминалы AE2WTLib
  icon: ae2wtlib:wireless_universal_terminal
  position: 10
categories:
- ae2wtlib
item_ids:
  - ae2wtlib:wireless_pattern_encoding_terminal
  - ae2wtlib:wireless_pattern_access_terminal
---


<a name="wireless-terminals"/>

# Беспроводные терминалы

<ItemGrid>
  <ItemIcon id="ae2wtlib:wireless_universal_terminal" />
  <ItemIcon id="ae2:wireless_crafting_terminal" />
  <ItemIcon id="ae2wtlib:wireless_pattern_encoding_terminal" />
  <ItemIcon id="ae2wtlib:wireless_pattern_access_terminal" />
</ItemGrid>

Помимо <ItemLink id="ae2:energy_card" />, все беспроводные терминалы AE2WTLib могут быть улучшены с помощью <ItemLink id="ae2wtlib:quantum_bridge_card" />
и объединены в <ItemLink id="ae2wtlib:wireless_universal_terminal" />.

Как и <ItemLink id="ae2:wireless_terminal" /> из AE2, к ним можно получить доступ с помощью клавиши быстрого доступа и поместить в слот аксессуара
(если установлен мод, реализующий API Curios).

Беспроводные терминалы также могут быть привязаны, как обычный <ItemLink id="ae2:wireless_terminal" />, с помощью <ItemLink id="ae2:wireless_access_point" />


<a name="wireless-universal-terminal"/>

## Универсальный беспроводной терминал

<ItemImage id="ae2wtlib:wireless_universal_terminal" scale="3" />

<ItemLink id="ae2wtlib:wireless_universal_terminal" /> — это объединение нескольких беспроводных терминалов в один предмет.


<a name="wireless-crafting-terminal"/>

## Беспроводной терминал крафта

<ItemImage id="ae2:wireless_crafting_terminal" scale="3" />

<ItemLink id="ae2:wireless_crafting_terminal" /> — беспроводная версия терминала крафта.
Он имеет [дополнительные функции](wireless_crafting_terminal.md) по сравнению с обычным AE2.


<a name="wireless-pattern-encoding-terminal"/>

## Беспроводной терминал кодирования шаблонов

<ItemImage id="ae2wtlib:wireless_pattern_encoding_terminal" scale="3" />

Беспроводная версия <ItemLink id="ae2:pattern_encoding_terminal" />

<RecipeFor id="ae2wtlib:wireless_pattern_encoding_terminal" />


<a name="wireless-pattern-access-terminal"/>

## Беспроводной терминал доступа к шаблонам

<ItemImage id="ae2wtlib:wireless_pattern_access_terminal" scale="3" />

Беспроводная версия <ItemLink id="ae2:pattern_access_terminal" />

<RecipeFor id="ae2wtlib:wireless_pattern_access_terminal" />


<a name="addon-terminals"/>

## Терминалы аддонов

Большинство беспроводных терминалов из других аддонов также работают с <ItemLink id="ae2wtlib:wireless_universal_terminal" />.

## Терминалы, которые не работают в универсальном терминале

<ItemLink id="ae2:wireless_terminal" /> нельзя использовать в <ItemLink id="ae2wtlib:wireless_universal_terminal" />,
поскольку он не даёт никаких преимуществ по сравнению с <ItemLink id="ae2:wireless_crafting_terminal" />.
Также он не может использовать <ItemLink id="ae2wtlib:quantum_bridge_card" />
