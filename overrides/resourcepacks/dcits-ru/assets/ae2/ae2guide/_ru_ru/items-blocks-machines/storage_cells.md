---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Ячейки хранения
  icon: item_storage_cell_1k
  position: 410
categories:
- tools
item_ids:
- ae2:item_cell_housing
- ae2:fluid_cell_housing
- ae2:cell_component_1k
- ae2:cell_component_4k
- ae2:cell_component_16k
- ae2:cell_component_64k
- ae2:cell_component_256k
- ae2:item_storage_cell_1k
- ae2:item_storage_cell_4k
- ae2:item_storage_cell_16k
- ae2:item_storage_cell_64k
- ae2:item_storage_cell_256k
- ae2:fluid_storage_cell_1k
- ae2:fluid_storage_cell_4k
- ae2:fluid_storage_cell_16k
- ae2:fluid_storage_cell_64k
- ae2:fluid_storage_cell_256k
---


<a name="storage-cells"/>

# Ячейки хранения

<Column>
  <Row>
    <ItemImage id="item_storage_cell_1k" scale="4" />

    <ItemImage id="item_storage_cell_4k" scale="4" />

    <ItemImage id="item_storage_cell_16k" scale="4" />

    <ItemImage id="item_storage_cell_64k" scale="4" />

    <ItemImage id="item_storage_cell_256k" scale="4" />
  </Row>

  <Row>
    <ItemImage id="fluid_storage_cell_1k" scale="4" />

    <ItemImage id="fluid_storage_cell_4k" scale="4" />

    <ItemImage id="fluid_storage_cell_16k" scale="4" />

    <ItemImage id="fluid_storage_cell_64k" scale="4" />

    <ItemImage id="fluid_storage_cell_256k" scale="4" />
  </Row>
</Column>

Ячейки хранения являются одним из основных методов хранения в Applied Energistics. Они помещаются в <ItemLink id="drive" /> или <ItemLink id="chest" />.

См. [Байты и типы](../ae2-mechanics/bytes-and-types.md) для объяснения их ёмкости в байтах и типах.

Компоненты хранения можно удалить из корпуса, если ячейка пуста, с помощью Shift+ПКМ, держа ячейку в руке.

<Row>
    <Recipe id="upgrade/item_storage_cell_1k_to_4k" />

    Вы можете улучшать ячейки хранения до более высоких уровней, комбинируя их с компонентами хранения более высокого уровня в сетке крафта. Их содержимое будет сохранено, а компонент более низкого уровня будет возвращён.
</Row>


<a name="storage-capacity-with-varying-type-count"/>

## Ёмкость хранения с разным количеством типов

[Первоначальная стоимость типов](../ae2-mechanics/bytes-and-types.md) такова, что ячейка, хранящая 1 тип, может хранить в 2 раза больше, чем ячейка, использующая все 63 типа.

| Ячейка                                     | Общая ёмкость ячейки с использованием 1 типа | Общая ёмкость ячейки с использованием 63 типов |
| ---------------------------------------- | ----------------------------------------: | ------------------------------------------: |
| <ItemLink id="item_storage_cell_1k" />   |                                     8,128 |                                       4,160 |
| <ItemLink id="item_storage_cell_4k" />   |                                    32,512 |                                      16,640 |
| <ItemLink id="item_storage_cell_16k" />  |                                   130,048 |                                      66,560 |
| <ItemLink id="item_storage_cell_64k" />  |                                   520,192 |                                     266,240 |
| <ItemLink id="item_storage_cell_256k" /> |                                 2,080,768 |                                   1,064,960 |



<a name="partitioning"/>

## Разделение

Ячейки можно отфильтровать, чтобы они принимали только определённые предметы, аналогично тому, как можно фильтровать <ItemLink id="storage_bus" />. Это делается в <ItemLink id="cell_workbench" />.

Предметы можно перетаскивать в слоты из JEI/REI, даже если у вас на самом деле нет ни одного такого предмета.


<a name="upgrades"/>

## Улучшения

Ячейки хранения поддерживают следующие [улучшения](upgrade_cards.md), которые вставляются с помощью <ItemLink id="cell_workbench" />:

*   <ItemLink id="fuzzy_card" /> (недоступно для жидкостных ячеек) позволяет разделять ячейку по уровню прочности и/или игнорировать NBT предмета
*   <ItemLink id="inverter_card" /> переключает фильтр с белого списка на чёрный список
*   <ItemLink id="equal_distribution_card" /> выделяет одинаковое количество байт ячейки каждому типу, поэтому один тип не может заполнить всю ячейку
*   <ItemLink id="void_card" /> уничтожает вставленные предметы, если ячейка заполнена (или выделенное для этого конкретного типа место в случае карты равномерного распределения), что полезно для предотвращения заторов на фермах. Будьте осторожны и используйте разделение!
*   Портативные ячейки могут принимать <ItemLink id="energy_card" /> для увеличения ёмкости их батареи


<a name="coloring"/>

## Окрашивание

Портативные предметные и жидкостные ячейки можно окрашивать, как кожаную броню, комбинируя их с красителями.


<a name="housings"/>

# Корпуса

Ячейки можно создавать с помощью компонента хранения и корпуса или с помощью рецепта корпуса вокруг компонента хранения:

<Row>
  <Recipe id="network/cells/item_storage_cell_1k" />

  <Recipe id="network/cells/item_storage_cell_1k_storage" />
</Row>

Корпуса сами по себе создаются так:

<Row>
  <RecipeFor id="item_cell_housing" />

  <RecipeFor id="fluid_cell_housing" />
</Row>


<a name="storage-components"/>

# Компоненты хранения

Компоненты хранения являются основой всех ячеек AE2, определяя их ёмкость. Каждый уровень увеличивает ёмкость в 4 раза и стоит 3 компонента предыдущего уровня.

<Column>
  <Row>
    <RecipeFor id="cell_component_1k" />

    <RecipeFor id="cell_component_4k" />

    <RecipeFor id="cell_component_16k" />
  </Row>

  <Row>
    <RecipeFor id="cell_component_64k" />

    <RecipeFor id="cell_component_256k" />
  </Row>
</Column>


<a name="item-storage-cells"/>

# Ячейки хранения предметов

Ячейки хранения предметов могут содержать до 63 различных типов предметов и доступны во всех стандартных ёмкостях.

<Column>
  <Row>
    <Recipe id="network/cells/item_storage_cell_1k_storage" />

    <Recipe id="network/cells/item_storage_cell_4k_storage" />

    <Recipe id="network/cells/item_storage_cell_16k_storage" />
  </Row>

  <Row>
    <Recipe id="network/cells/item_storage_cell_64k_storage" />

    <Recipe id="network/cells/item_storage_cell_256k_storage" />
  </Row>
</Column>


<a name="portable-item-storage"/>

## Портативное хранение предметов

Эти ячейки действуют как крошечный <ItemLink id="chest" /> в вашем кармане или как форма рюкзака. Их можно заряжать в <ItemLink id="charger" />

В отличие от стандартных ячеек хранения, эти ячейки фактически *уменьшают* свою ёмкость по типам по мере увеличения ёмкости по байтам и имеют половину общей ёмкости по байтам.

В дополнение к картам улучшений, которые могут получать все ячейки, эти ячейки также принимают <ItemLink id="energy_card" /> для улучшения своих внутренних батарей.

<Column>
  <Row>
    <RecipeFor id="portable_item_cell_1k" />

    <RecipeFor id="portable_item_cell_4k" />

    <RecipeFor id="portable_item_cell_16k" />
  </Row>

  <Row>
    <RecipeFor id="portable_item_cell_64k" />

    <RecipeFor id="portable_item_cell_256k" />
  </Row>
</Column>


<a name="fluid-storage-cells"/>

# Ячейки хранения жидкостей

Ячейки хранения жидкостей могут содержать до 5 различных типов жидкостей и доступны во всех стандартных ёмкостях.

<Column>
  <Row>
    <Recipe id="network/cells/fluid_storage_cell_1k_storage" />

    <Recipe id="network/cells/fluid_storage_cell_4k_storage" />

    <Recipe id="network/cells/fluid_storage_cell_16k_storage" />
  </Row>

  <Row>
    <Recipe id="network/cells/fluid_storage_cell_64k_storage" />

    <Recipe id="network/cells/fluid_storage_cell_256k_storage" />
  </Row>
</Column>


<a name="portable-fluid-storage"/>

## Портативное хранение жидкостей

Эти ячейки действуют как крошечный <ItemLink id="chest" /> в вашем кармане или как форма рюкзака. Их можно заряжать в <ItemLink id="charger" />

В отличие от стандартных ячеек хранения, эти ячейки фактически *уменьшают* свою ёмкость по типам по мере увеличения ёмкости по байтам и имеют половину общей ёмкости по байтам.

В дополнение к картам улучшений, которые могут получать все ячейки, эти ячейки также принимают <ItemLink id="energy_card" /> для улучшения своих внутренних батарей.

<Column>
  <Row>
    <RecipeFor id="portable_fluid_cell_1k" />

    <RecipeFor id="portable_fluid_cell_4k" />

    <RecipeFor id="portable_fluid_cell_16k" />
  </Row>

  <Row>
    <RecipeFor id="portable_fluid_cell_64k" />

    <RecipeFor id="portable_fluid_cell_256k" />
  </Row>
</Column>


<a name="creative-storage-cell"/>

# Творческая ячейка хранения

<Row>
  <ItemImage id="creative_storage_cell" scale="2" />
</Row>

Творческие ячейки **не обеспечивают бесконечное хранение**. Вместо этого они действуют как бесконечные источники и стоки любого предмета или жидкости, на которые вы [разделяете](cell_workbench.md) их.
