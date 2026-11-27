---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Кабели
  icon: fluix_glass_cable
  position: 110
categories:
- network infrastructure
item_ids:
- ae2:white_glass_cable
- ae2:orange_glass_cable
- ae2:magenta_glass_cable
- ae2:light_blue_glass_cable
- ae2:yellow_glass_cable
- ae2:lime_glass_cable
- ae2:pink_glass_cable
- ae2:gray_glass_cable
- ae2:light_gray_glass_cable
- ae2:cyan_glass_cable
- ae2:purple_glass_cable
- ae2:blue_glass_cable
- ae2:brown_glass_cable
- ae2:green_glass_cable
- ae2:red_glass_cable
- ae2:black_glass_cable
- ae2:fluix_glass_cable
- ae2:white_covered_cable
- ae2:orange_covered_cable
- ae2:magenta_covered_cable
- ae2:light_blue_covered_cable
- ae2:yellow_covered_cable
- ae2:lime_covered_cable
- ae2:pink_covered_cable
- ae2:gray_covered_cable
- ae2:light_gray_covered_cable
- ae2:cyan_covered_cable
- ae2:purple_covered_cable
- ae2:blue_covered_cable
- ae2:brown_covered_cable
- ae2:green_covered_cable
- ae2:red_covered_cable
- ae2:black_covered_cable
- ae2:fluix_covered_cable
- ae2:white_covered_dense_cable
- ae2:orange_covered_dense_cable
- ae2:magenta_covered_dense_cable
- ae2:light_blue_covered_dense_cable
- ae2:yellow_covered_dense_cable
- ae2:lime_covered_dense_cable
- ae2:pink_covered_dense_cable
- ae2:gray_covered_dense_cable
- ae2:light_gray_covered_dense_cable
- ae2:cyan_covered_dense_cable
- ae2:purple_covered_dense_cable
- ae2:blue_covered_dense_cable
- ae2:brown_covered_dense_cable
- ae2:green_covered_dense_cable
- ae2:red_covered_dense_cable
- ae2:black_covered_dense_cable
- ae2:fluix_covered_dense_cable
- ae2:white_smart_cable
- ae2:orange_smart_cable
- ae2:magenta_smart_cable
- ae2:light_blue_smart_cable
- ae2:yellow_smart_cable
- ae2:lime_smart_cable
- ae2:pink_smart_cable
- ae2:gray_smart_cable
- ae2:light_gray_smart_cable
- ae2:cyan_smart_cable
- ae2:purple_smart_cable
- ae2:blue_smart_cable
- ae2:brown_smart_cable
- ae2:green_smart_cable
- ae2:red_smart_cable
- ae2:black_smart_cable
- ae2:fluix_smart_cable
- ae2:white_smart_dense_cable
- ae2:orange_smart_dense_cable
- ae2:magenta_smart_dense_cable
- ae2:light_blue_smart_dense_cable
- ae2:yellow_smart_dense_cable
- ae2:lime_smart_dense_cable
- ae2:pink_smart_dense_cable
- ae2:gray_smart_dense_cable
- ae2:light_gray_smart_dense_cable
- ae2:cyan_smart_dense_cable
- ae2:purple_smart_dense_cable
- ae2:blue_smart_dense_cable
- ae2:brown_smart_dense_cable
- ae2:green_smart_dense_cable
- ae2:red_smart_dense_cable
- ae2:black_smart_dense_cable
- ae2:fluix_smart_dense_cable
---


<a name="cables"/>

# Кабели

<GameScene zoom="3" background="transparent">
  <ImportStructure src="../assets/assemblies/cables.snbt" />
  <IsometricCamera yaw="180" pitch="30" />
</GameScene>

Хотя сети ME также создаются с помощью смежных машин, поддерживающих ME, кабели являются основным способом расширения сети ME на более крупные области.

Кабели разного цвета можно использовать, чтобы убедиться, что смежные кабели не подключаются друг к другу, позволяя [каналам](../ae2-mechanics/channels.md) более эффективно распределяться. Они также влияют на цвет подключенных к ним терминалов, так что вам не нужно, чтобы все ваши терминалы были пурпурными. Кабели флюиса подключаются ко всем другим цветам.

Примечательно, что **КАНАЛЫ НИКАК НЕ СВЯЗАНЫ С ЦВЕТОМ КАБЕЛЕЙ**


<a name="an-important-note"/>

## Важное примечание

**Если вы новичок в AE2 и не знакомы с каналами, используйте умные кабели и плотные умные кабели, где только можно. Они покажут, как каналы проложены по вашей сети, что сделает их поведение более понятным.**


<a name="another-note"/>

## Другое примечание

**Это не трубы для предметов, жидкостей, энергии и т.д.** У них нет внутреннего инвентаря, поставщики шаблонов и машины не «вталкивают» в них предметы; всё, что они делают, — это соединяют [устройства](../ae2-mechanics/devices.md) AE2 в сеть.


<a name="glass-cable"/>

## Стеклянный кабель

<GameScene zoom="6" background="transparent">
<ImportStructure src="../assets/assemblies/fluix_glass_cable.snbt" />
<IsometricCamera yaw="195" pitch="30" />
</GameScene>

<ItemLink id="fluix_glass_cable" /> — самый простой кабель для создания, передаёт энергию и до 8 [каналов](../ae2-mechanics/channels.md). Он бывает 17 различных цветов, по умолчанию — флюисовый, и может быть окрашен в любой цвет с помощью любого из 16 красителей.

Чтобы создать окрашенные кабели, окружите краситель любого типа 8 кабелями одного типа (цвет кабелей не имеет значения, но они должны быть одного типа: стеклянные, умные и т.д.). Вы также можете покрасить кабели любым совместимым с Forge кистью для покраски в мире.

Вы можете создать любой окрашенный кабель с помощью ведра воды, чтобы удалить краску.

Вы можете покрыть кабель шерстью, чтобы создать <ItemLink id="fluix_covered_cable" />, и создать <ItemLink id="fluix_smart_cable" />, чтобы лучше понять, что происходит с вашими [каналами](../ae2-mechanics/channels.md).

<RecipeFor id="fluix_glass_cable" />

<RecipeFor id="blue_glass_cable" />


<a name="covered-cable"/>

## Покрытый кабель

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/fluix_covered_cable.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Вариант покрытого кабеля не даёт никаких игровых преимуществ по сравнению с <ItemLink id="fluix_glass_cable" />. Однако он может быть использован в качестве альтернативного эстетического выбора, если вам больше нравится внешний вид покрытого кабеля.

Может быть окрашен таким же образом, как и <ItemLink id="fluix_glass_cable" />. Четыре <ItemLink id="fluix_covered_cable" /> можно создать с красным камнем и светящимся камнем, чтобы получить <ItemLink id="fluix_covered_dense_cable" />.

<Recipe id="network/cables/covered_fluix" />

<RecipeFor id="blue_covered_cable" />


<a name="dense-cable"/>

## Плотный кабель

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/fluix_covered_dense_cable.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Кабель с более высокой пропускной способностью, может передавать 32 канала, в отличие от стандартного кабеля, который может передавать только 8, однако он не поддерживает шины, поэтому вы должны сначала перейти от плотного кабеля к более тонкому (например, <ItemLink id="fluix_glass_cable" /> или <ItemLink id="fluix_smart_cable" />), прежде чем использовать шины или панели.

Плотные кабели немного изменяют поведение «кратчайшего пути» каналов: каналы будут выбирать кратчайший путь к плотному кабелю, а затем кратчайший путь по этому плотному кабелю к контроллеру.

<Recipe id="network/cables/dense_covered_fluix" />

<RecipeFor id="blue_covered_dense_cable" />


<a name="smart-cable"/>

## Умный кабель

<Row>
<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/fluix_smart_cable.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>
<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/fluix_smart_dense_cable.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>
</Row>

Хотя они и похожи по внешнему виду на <ItemLink id="fluix_covered_cable" />, они предоставляют диагностическую функцию, визуализируя использование каналов на кабелях: каналы отображаются в виде светящихся цветных линий, идущих вдоль чёрной полосы на кабелях, что позволяет понять, как ваши каналы используются в вашей сети. Для обычных умных кабелей первые четыре канала отображаются в виде линий, соответствующих цвету кабеля, следующие четыре — в виде белых линий. Для плотного умного кабеля каждая полоса представляет 4 канала.

В сетях с <ItemLink id="controller" /> линии на кабелях показывают точный путь, по которому проходят каналы.

На сетях ad-hoc умные кабели вместо этого показывают общее количество используемых каналов во всей сети, а не количество каналов, проходящих через конкретный кабель.

Их также можно окрашивать таким же образом, как и <ItemLink id="fluix_glass_cable" />.

<Recipe id="network/cables/smart_fluix" />

<Recipe id="network/cables/dense_smart_fluix" />

<RecipeFor id="blue_smart_cable" />
