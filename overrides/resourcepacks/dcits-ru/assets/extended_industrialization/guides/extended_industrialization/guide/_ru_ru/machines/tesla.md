---
navigation:
  title: "Тесла"
  icon: "tesla_coil"
  parent: extended_industrialization:machines.md
categories:
  - machines
item_ids:
  - extended_industrialization:tesla_calibrator
  - extended_industrialization:tesla_handheld_receiver
  - extended_industrialization:tesla_interdimensional_upgrade
  - extended_industrialization:tesla_coil
  - extended_industrialization:tesla_receiver
  - extended_industrialization:lv_tesla_receiver_hatch
  - extended_industrialization:mv_tesla_receiver_hatch
  - extended_industrialization:hv_tesla_receiver_hatch
  - extended_industrialization:ev_tesla_receiver_hatch
  - extended_industrialization:superconductor_tesla_receiver_hatch
  - extended_industrialization:tesla_tower
  - extended_industrialization:aluminum_tesla_winding
  - extended_industrialization:annealed_copper_tesla_winding
  - extended_industrialization:copper_tesla_winding
  - extended_industrialization:electrum_tesla_winding
  - extended_industrialization:superconductor_tesla_winding
---


<a name="tesla"/>

# Тесла

Свинцовые катушки и приемники Tesla позволяют беспроводно передавать ЕС по цене.
(Tesla Coil или Tesla Tower), и не имеет внутреннего ограничения на приемники Tesla.
расходы на пропускную способность и пассивные расходы на отводную способность.


<a name="tesla-calibrator"/>

## Калибратор Теслы

Для подключения передатчика Tesla к приемникам сначала нажмите **<KeyBind id="key.sneak" />** + **<KeyBind id="key.use" />**
Затем просто нажмите на **<KeyBind id="key.use" />** с калибратором
на любой приемник, чтобы связать его.

<RecipeFor id="extended_industrialization:tesla_calibrator" />


<a name="tesla-transmitters"/>

## Передатчики Теслы

Передатчики Tesla являются источником всех сетей Tesla.

Передатчик не может передавать энергию в приемник, который не имеет точно такого же напряжения.
с Advanced Machine Hull не может передать Tesla Приемник без корпуса, но мог бы к тому, что также имеет
Усовершенствованная машинная корпус.

Пассивная стоимость загрузки EU/t катушки Tesla определяется напряжением корпуса (или отсутствием) внутри него.

<RecipeFor id="extended_industrialization:tesla_coil" />

Напряжение, передаваемое башней Теслы, определяется используемыми энергетическими шлюзами.

Пассивная стоимость загрузки EU/t, предел передачи и диапазон Tesla Tower определяются используемыми обмотками.
каждой из них описывается в их соответствующих инструментальных концах.

<Row>
	<GameScene zoom="0.75" interactive={true} fullWidth={false}>
		<MultiblockShape controller="extended_industrialization:tesla_tower" />
	</GameScene>
	<RecipeFor id="extended_industrialization:tesla_tower" />
</Row>


<a name="tesla-receivers"/>

## Приемники Теслы

Приемники Tesla - это направления для передачи энергии от вашего передатчика.

Приемник Tesla хранит полученную энергию и выбросает ее из выходной поверхности.
Используя кабели, как это делается с любым другим блоком выпуска энергии.

<RecipeFor id="extended_industrialization:tesla_receiver" />

Приемник Tesla получает энергию так же, как обычный Приемник Tesla, однако он функционирует как энергоноситель.
Вместо того, чтобы иметь приемник и затем также энергетический входный люк, чтобы выбросить в, это
объединяет оба в один люк.

<RecipeFor id="extended_industrialization:lv_tesla_receiver_hatch" />
