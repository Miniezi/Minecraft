---
navigation:
  title: "Электрический маячок"
  icon: "electric_beacon"
  parent: extended_industrialization:machines.md
categories:
  - machines
item_ids:
  - extended_industrialization:electric_beacon
---


<a name="electric-beacon"/>

# Электрический маячок

<RecipeFor id="extended_industrialization:electric_beacon" />

Электрический Beacon потребляет напитки и обеспечивает эффект для всех близлежащих игроков по цене 256 EU/t за эффект.
Продолжительность действия напитка в маяк определяется кратчайшим эффектом, который затем сокращается вдвое.
Например, 8-минутный скоростный раствор может быть потреблен маяком, который
Затем он окажет эффект в районе в общей сложности 4 минуты стоимостью 256 EU/t. Аналогично, черепаха 40 секунд
Мастер-поция длится всего 20 секунд, но стоит 512 EU/t, поскольку она имеет два эффекта (медленность и сопротивление).
В случае, если лекарства потребляются маяком, рекомендуется создать какую-то автоматическую петлю с использованием
[Brewery](brewery.md) для бессрочной работы маяка.

Диапазон маяка больше, чем у обычного маяка, определяемого количеством слоев под ним,
за исключением стальной оболочки.

| Layers | Range |
|--------|-------|
| 1      | 30    |
| 2      | 50    |
| 3      | 70    |
| 4      | 90    |

<GameScene zoom="1.5" interactive={true} fullWidth={true}>
    <MultiblockShape controller="extended_industrialization:electric_beacon" />
    <MultiblockShape controller="extended_industrialization:electric_beacon" useBigShape={true} x="-9" y="2" z="-9" />
</GameScene>
