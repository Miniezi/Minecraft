---
navigation:
  title: "Фермер"
  icon: "steam_farmer"
  parent: extended_industrialization:machines.md
categories:
  - machines
item_ids:
  - extended_industrialization:steam_farmer
  - extended_industrialization:electric_farmer
---


<a name="farmer"/>

# Фермер

<Row>
	<RecipeFor id="extended_industrialization:steam_farmer" />
	<RecipeFor id="extended_industrialization:electric_farmer" />
</Row>

Фермер - не типичная машина, потому что он ведет рецепты, но вместо этого он заправляет и гидратирует почву, растения, удобрения,
и собирает урожаи, растения и деревья. Не все эти работы требуются для того, чтобы фермер работал,
Например, при работе с деревьями вы не захотите, чтобы
фермер, чтобы выращивать или гидратировать почву.

Фермер не будет работать, если ему не будет достаточной энергии. EU/t из паров,
и электрический фермер требует 64 EU/t.

Грязь, необходимая в составе многоблока фермера, может быть заменена любым блоком, который имеет этикетку
`#extended_industrialization:farmer_dirt`.


<a name="tasks"/>

## Задачи


<a name="tilling-soil"/>

### Уборка почвы

При включении в настройках формы мультиблока грязные блоки, которые являются частью мультиблока, будут преобразованы в
земледельческой, как если бы на ней была использована колыбель.


<a name="hydrating-soil"/>

### Увлажняющая почва

Когда фермер получает воду через входной люк, он гарантирует, что вся сельскохозяйственная земля
необходимость размещения воды рядом.


<a name="planting"/>

### Посадка

Когда фермер получает посадочные изделия в входном люке, он посадит их на любые подходящие блоки в своем помещении.
Площадь эксплуатации. могут быть посажены только предметы с тегом "`#extended_industrialization:farmer_plantable`".


<a name="fertilizing"/>

### Плодотворение

В случае предоставления приемлемого жидкого удобрения (как показано в EMI) в сельском хозяйстве электроэнергии.
входный люк, фермер будет применять костное мясо-подобный эффект к растениям в своей области эксплуатации.
Эффект может применяться к растениям, которые обычно не могут быть сделаны с костями, например, кактусам и сахарной тростнице.


<a name="harvesting"/>

### Сбор урожая

Если есть растение, которое достигло полной зрелости (например, полностью выращенная пшеница, или выросшее из кустарника дерево),
и в выходном люке есть достаточно места для выхода, он будет разбит и хранится.

Предметы с тегом `#extended_industrialization:farmer_voidable` имеют низкий приоритет для хранения в выходных люках, и
Если нет места для него, когда он будет выброшен с укорененной породы, он будет выбрасываться вместо этого.
Это <ItemLink id="minecraft:stick" />s и <ItemLink id="minecraft:apple" />s, что означает, что вы могли бы заблокировать
Выпускные слоты из вашего выпускного люка(es) просто журналы и саженцы, и не нужно беспокоиться о хранении этих элементов.


<a name="steam-farmer"/>

## Парный фермер

<GameScene zoom="1" interactive={true} fullWidth={true}>
    <MultiblockShape controller="extended_industrialization:steam_farmer" />
    <MultiblockShape controller="extended_industrialization:steam_farmer" useBigShape={true} x="-10" z="-10" />
</GameScene>


<a name="electric-farmer"/>

## Электрический фермер

<GameScene zoom="1" interactive={true} fullWidth={true}>
    <MultiblockShape controller="extended_industrialization:electric_farmer" />
    <MultiblockShape controller="extended_industrialization:electric_farmer" useBigShape={true} x="-12" z="-12" />
</GameScene>
