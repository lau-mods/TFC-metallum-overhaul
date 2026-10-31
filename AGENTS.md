# TFCM 開発ガイド

## 仕様と実装状態

- 新規実装の要求仕様は `PROJECT.md`。JavaコードとGradle設定にはM01の対応環境・依存関係基盤を実装済みであり、後続機能の実装済みを意味しない。
- GradleはNeoForge `21.1.265`、Mod IDは `tfcm`、基本パッケージは `net.claustra01.tfcm` を使用する。
- Minecraftは `[1.21.1]` に限定する。TFC `[4.2.12,)` とPatchouli `[1.21.1-93-NEOFORGE,)` は必須、FirmaLife `[3.0.15,)` は任意で、3 ModともTFCMより先に読み込む。

## M01 verification

- `gameTest` source setを使用し、`runGameTestServer` はTFC・Patchouliのみ、続けて`runGameTestServerFirmaLife` はFirmaLife 3.0.15を追加して実行する。
- 各runは`tfcm.gametest.report`と`tfcm.gametest.structures`を設定する。JUnit XMLはそれぞれ`build/test-results/gametest/`と`build/test-results/gametest-firmalife/`に出力する。
- `./gradlew classes build runGameTestServer`でcompile、build、両GameTest環境を検証する。`tests/e2e/foundation-boot.mjs`はTFC・Patchouliのみの専用サーバーとclient `tfcm-a`の接続を検証する。

## M02 metals

- 39金属の通常item IDは`tfcm:metal/<form>/<metal>`、block IDは`tfcm:metal/block/<metal>`、slab/stairsはblock ID末尾に`_slab`/`_stairs`を付ける。溶融流体は`tfcm:metal/<metal>`、bucketは`tfcm:bucket/metal/<metal>`を使用する。
- 高炭素中間素材は`tfcm:metal/ingot/high_carbon_tungsten_steel`、独立流体は`tfcm:molten_andesite`とする。金属別データには`tfcm:metal_enabled` load conditionを付ける。
- `MetalDataProvider`がcasting、anvil、welding、heating、alloy、fluid/item heat、item size、common item/block/fluid tag、採掘tag、block lootを`src/generated/resources`へ生成する。変更後は`./gradlew runData`で生成物を同期する。
- Double IngotとDouble SheetのWelding tierはTFCと同じく金属tierの1つ下（最小0）とし、同金属の金床を作る前に製作できるようにする。Metal Block、Slab、Stairsの表示名はTFCと同じ`<Metal> Plated Block/Slab/Stairs`とする。
- Metal BlockはTFCと同じくHammer損耗、Sheet 4個、`#minecraft:planks`から8個、Slabは3個から6個、Stairsは6個から8個を作る。High Carbon Tungsten SteelはTungsten IngotとBlack Steel Ingotのtier 6 Welding、Netherite IngotはNetherite ScrapとBlack Steel Ingotのtier 6 Weldingでも作り、Netherite ScrapにはNetherite Ingotと同じheatを定義する。
- M02のGameTestは登録参照、仕様値とheat、鋳造、金床・溶接、合金、Andesite Alloyを検証する。`./gradlew classes build runGameTestServer`でTFC単独とFirmaLife追加の両runを実行する。

## M03 metal forms

- 39金属すべてに`foil`、`gear`、`heavy_sheet`、`nail`、`ring`、`rivet`、`screw`、`stamen`、`wire`、`plate`、`nugget`、`dust`を常設し、IDは`tfcm:metal/<form>/<metal>`とする。
- FoilはSheet 1→2、GearはDouble Sheet 1→1、Nail/Rivet/ScrewはStamen 1→5、RingはStamen 1→2、Stamen/WireはRod 1→2、PlateはIngot 1→1を金属tierのAnvilで作る。Heavy SheetはDouble Sheet 1とSheet 1→1のWeldingだけで作る。
- Gearは`punch_last,hit_second_last,upset_third_last`、Nailは`punch_last,hit_not_last,bend_not_last`、Ringは`hit_last,draw_second_last,draw_third_last`、Rivetは`draw_not_last,bend_any,hit_any`、Screwは`hit_any,hit_any,draw_last`、Stamenは`bend_last,draw_second_last,draw_third_last`、それ以外のAnvil形状はTFC Sheet/Rodと同じ`hit_last,hit_second_last,hit_third_last`を使用する。TFC 4.2.12本体には拡張形状とPlateの同名レシピはないため、拡張形状の数値・操作列はTFC本体からは照合できない。
- NuggetはIngot 1→9、Nugget 9→Ingot 1のCrafting Grid加工。溶融量はNugget 1個10 mB、9個90 mBとし、100 mBへの補正を行わない。DustはIngot 1→Dust 1のTFC Quern加工、PlateとDustの溶融量は各100 mB。Dustは鉱石Powderと別アイテム・別タグにする。
- 金属別common item tagは`c:<form>s/<metal>`（Heavy Sheetは`c:heavy_sheets/<metal>`）を用い、集合タグ`c:<form>s`にも含める。各形状のHeating、item heat、size/weight、翻訳、modelとtextureを生成・登録する。
- TFC More Items・TFC Metal Tools相当として、SheetとRodを持つTFC 4.2.12の19金属（`TfcMetal`: Bismuth、Bismuth Bronze、Black Bronze、Black Steel、Blue Steel、Brass、Bronze、Cast Iron、Copper、Gold、Nickel、Red Steel、Rose Gold、Silver、Steel、Sterling Silver、Tin、Wrought Iron、Zinc）にもF-004・F-005の12形状を`tfcm:metal/<form>/<metal>`で常設する。加工規則、個数、溶融量、size/weightは上記の同形状と同じとし、Anvil・Welding tierはTFC本体の同金属Rod/Sheetレシピに合わせる（TFCがtierを持たない金属は省略）。熱特性はTFCの同金属Ingot item heatとfluid heat、溶融先は`tfc:metal/<metal>`、Nugget 9個はTFC Ingotへ戻す。`c:<form>s/<metal>`と集合タグへ含め、textureはInvarの同形状をTFCの同金属Ingot配色へ置き換えた派生物とする。
- FirmaLife 3.0.15のChromium、Stainless Steel（`TfcMetal`内）はFirmaLife導入時だけ上記12形状を登録する。tierは4、溶融先は`firmalife:metal/<metal>`、Nugget 9個はFirmaLifeのIngotへ戻し、熱特性はFirmaLifeの値で、recipeには`neoforge:mod_loaded`条件を付ける。TFCのitem heat/sizeとtagは読込条件を見ないため、heat/sizeは`c:<form>s/<metal>`経由、tagは任意entryとする。textureはInvarの同形状をFirmaLifeのIngot配色へ置き換えた派生物とする。
- `./gradlew runData`で生成物を同期し、`./gradlew classes build runGameTestServer`でTFC単独とFirmaLife追加の両GameTest runを検証する。M03 GameTestは9拡張形状の登録・加工・熱、Plate、Nugget、Dust、39金属への適用を確認する。

## M04 material parts

- Bow、Crossbow、Arrowは部品組立だけで作り、TFCの`minecraft:bow`、`minecraft:crossbow`、`tfc:crafting/arrow`とVanillaの火打石の`minecraft:arrow`は同一IDで無効化する。Iron Arrow Headの代わりにObsidian Shardを使う`tfcm:crafting/material/arrow_obsidian`を追加する。Plant String、Bamboo String、Leather Stringは`c:string`に加えてTFCの`c:strings`へ登録し、Leather Strapは入れない。
- 革、植物、竹、弓・弩、矢、工具組立用の部品は部品名のflat item IDを使用する。Plant Blockだけは`tfcm:plant_block`のblock/itemとして登録する。
- `c:plant/grass`（TFC More Items準拠でVanillaの草とTFCの草13種）を右クリックするとPlant Fiberを落とす。空の主手は1個を80%と追加1個を30%、Knifeは3個を90%と追加2個を50%の独立抽選とする。1回ごとに1秒のcooldown、灰のparticle、植付音を出し、40%で草を除去し、Knifeは耐久を1消費する。破壊時のdropはない。Knifeを使う加工はTFC Advanced Shapeless Craftingの`damage_crafting_remainder`で1耐久を消費する。
- Plant Blockはrandom tickごとに10%で`tfc:thatch`へ置換する。itemの三人称表示はTFC More Itemsの値を`thirdperson_righthand`へ適用する。独自tierや効果は持たせない。
- 完成Bow、Crossbow、Arrowは独自完成品を登録せず、各中間部品からVanillaの完成品へ組み立てる。全素材部品とPlant BlockにTFC item size/weightを定義する。
- `MaterialDataProvider`がrecipe、grass/block loot、item size、model、blockstateを`src/generated/resources`へ生成する。M04 GameTestは登録と属性、植物・竹加工と乾燥、完成品組立とKnife損耗を検証し、`tests/e2e/material-parts.mjs`は全素材モデルを一画面で検証する。

## M05 equipment and anvils

- Invar、Titanium、Netherite、Tungsten Steelの工具・工具頭・武器・防具・補助装備は`tfcm:metal/<part>/<metal>`、金床は`tfcm:metal/anvil/<metal>`を使用する。工具tierは順にlevel `3/5/6/7`、耐久`2200/3300/6500/8125`、速度`8/11/12/13`、攻撃補正`4.75/7/9/10.5`、Enchantability `12/17/22/25`とする。
- 防具のBoots/Leggings/Chestplate/Helmet防御はInvar `1/4/5/2`、Titanium `2/5/6/2`、Netherite `3/6/8/3`、Tungsten Steel `4/7/9/4`。耐久は順に`429/495/528/370`、`650/750/800/550`、`884/1020/1010/715`、`1105/1275/1360/935`、Enchantabilityは`12/17/23/25`、Toughnessは`0/2/3/4`、Knockback抵抗は`0/0.05/0.10/0.15`とする。
- 工具組立にはF-006のHandle、Binding、Tool Riveting由来のCrossguard/Pommelと、F-004のRing・Rivet・Wireを使用する。Crossguard/Pommelは専用アートが確定するまで中立な小金属部品モデルを用いる。
- Tong PartとTongsは`tfchotornot`導入時だけ登録し、Tongsは同Modの公開`TongsItem`を使用する。`tfchotornot:tongs`へ統合するが`insulating`には追加せず、独自の耐熱倍率や保持効果を付けない。
- Heavy SheetのWelding tierはDouble Sheetと同じ金属tierの1つ下とする。TFC More Itemsの一律tier 1は採らない。形状のAnvil tierと熱特性はF-002、TFCの同金属Sheet/Rodレシピと量比例の計算を正とし、参照元の値へ揃えない。Crossguard 25 mBとPommel 24 mBの熱容量も同じ計算とする。
- Fishing RodはTFCと同じ` S/ SL/SXL`（X=Fish Hook）で組み立てる。
- `TfcToolAssemblyDataProvider`はTFC Metal Tools 1.0.7の組立をTFCの工具金属9種へ適用する。13工具の完成品はTFCの同一ID `tfc:crafting/metal/<tool>/<metal>`を置換し、tier 2以下は`NX/HG`、tier 3以上は` NX/GHR/RL `を基本にする。CopperのPickaxe/Propickは`NX/HN`（`#c:string/plant`）、Sawは`GX/HG`（`#c:string`）の紐組立とする。工具ごとの例外は同classの表を正本とし、Headの鍛造bonusは`input_row`/`input_column`で引き継ぐ。Shearsは`tfcm:crafting/metal/shears/<metal>`を追加し、TFCのWeldingも残す。
- 9金属のCrossguardはRod 1→2のAnvil（`bend_last,draw_second_last,draw_third_last`、bonusあり）、PommelはRing 2個のtier 1 Weldingで作り、溶融量は25/24 mBとする。textureはTFC Metal Toolsの各金属の画像をそのまま使う（金属ごとに輪郭が異なる）。
- 4金属の金床tierは`3/5/6/7`。同金属のDouble Ingot 7個で製作し、Heatingで同金属1400 mBへ戻す。TFC Anvil block entityと`tfc:anvils`、pickaxe採掘tagへ統合する。
- `tfc:blast_furnace_tuyeres`、`tfc:trip_hammers`、`tfc:holds_large_fishing_bait`、`tfc:anvils`（item）、`tfc:tools/<metal>`、`tfc:metal_item/<metal>`へ該当品を追加する。`tfc:sheets`、`tfc:double_sheets`、`tfc:double_ingots`は旧TFCMの別名で`c:`の同名tagを含む。Anvil recipeの鍛造bonus指定はTFC 4.2.12のcodecどおり`apply_bonus`であり、`apply_forging_bonus`ではない。
- `EquipmentDataProvider`が装備のAnvil/Welding/Crafting/Heating、item heat・size、tool/tag、model、金床のrecipe・loot・blockstateを`src/generated/resources`へ生成する。変更後は`./gradlew runData`、`./gradlew classes build runGameTestServer`、`node tests/e2e/equipment-anvils.mjs`で検証する。

## M06 ores and gems

- 独自鉱石は品位あり16種と品位なし3種を`Ore`で定義する。各TFC母岩、Netherrack、End Stone版は`tfcm:ore/<ore item name>/<host>`、Small Oreと地表sampleは`tfcm:ore/small_<ore>`を使用する。
- TFCの12×3品位鉱石と17品位なし鉱石にはNetherrack・End Stone版を追加し、元のTFC鉱石itemをdropする。FirmaLife Chromite 3品位×2版はFirmaLife導入時だけ登録し、生成データはmod-loaded conditionを付ける。
- Nether鉱石は硬度3・耐爆10、End鉱石は硬度3・耐爆9、TFC母岩版はTFCの母岩category hardnessを使用する。全鉱石blockを`c:ores`、`minecraft:mineable/pickaxe`、`tfc:prospectable`、`tfc:mineable/propick`へ追加する。探鉱表示は標準block translation key末尾の`.prospected`を使用する。
- 鉱石blockの英語名はTFCと同じ`<Grade> <Host> <Ore>`（品位なしは`<Host> <Ore>`）、`.prospected`は品位と母岩を含まない鉱石名とする。
- Small OreのitemはTFCと同じく`tfcm:item/ore/small_<ore>`を貼った平面modelとし、地表の立体modelはblockだけに使う。
- 品位なし鉱石の地表sampleは`tfcm:ore/small_<ore>`のblockだけを登録し、BlockItemを作らない。対象はFluorite、Quartz、Carobbiite、`TfcOre.UNGRADED`の17種、Bituminous Coal、Halite、Lignite、Nether Quartzの計24種で、壊すと元のraw item（Nether Quartzは`minecraft:quartz`）を落とす。
- 鉱石itemを`tfc:metal_ores`、`tfc:ore_pieces`、`tfc:small_ore_pieces`、`c:ores`、`c:ores/<ore>`、`c:ores/<ore>/<grade>`、`c:ores/<metal>/<grade>`へ追加する。
- QuartzとFluoriteはQuernで各Powder 4個にし、両PowderをTFC gem powder・bowl powder tagへ追加する。Cut QuartzはTFC Sandpaperの損耗を使い、Certus Quartzとは別itemとして登録する。
- `OreDataProvider`がblockstate、model、loot、tag、heating、quern、研磨を`src/generated/resources`へ生成し、langは`src/main/resources/assets/tfcm/lang/`で手動管理する。変更後は`./gradlew runData`と両GameTest runで機能を検証し、`node tests/e2e/ore-samples.mjs`で地表sampleの表示を検証する。

## M07 ore processing

- 35系列の中間item IDは`tfcm:ore_processing/<form>/<series>`（`rocky_chunks`、`chunks`、`dirty_dust`、`powder`、`pellet`、`briquet`）、7色の砂は`tfcm:ore_processing/sand_pile/<color>`を用いる。Dirty Pileはitem、recipe、loot、tag、lang、modelのいずれにも生成しない。
- `ProcessingSeries`をF-010表の正本とし、各行を`砂/主岩/副岩/Gem/副産物B/回収先`で表す。Native Copper=`red/basalt/andesite/opal/hematite/copper`、Native Gold=`yellow/quartzite/granite/quartz/native_silver/gold`、Hematite=`red/quartzite/shale/なし/sulfur/cast_iron`、Native Silver=`white/gneiss/granite/amethyst/galena/silver`、Cassiterite=`white/granite/gneiss/topaz/bismuthinite/tin`、Bismuthinite=`black/granite/schist/pyrite/galena/bismuth`、Garnierite=`green/gabbro/basalt/opal/limonite/nickel`、Malachite=`green/limestone/dolomite/opal/limonite/copper`、Magnetite=`black/gabbro/basalt/なし/hematite/cast_iron`、Limonite=`brown/claystone/shale/opal/sulfur/cast_iron`、Sphalerite=`brown/dolomite/limestone/amethyst/galena/zinc`、Tetrahedrite=`black/schist/dolomite/pyrite/sphalerite/copper`、Galena=`black/limestone/dolomite/amethyst/sphalerite/lead`、Magnesite=`white/dolomite/gabbro/opal/limonite/magnesium`、Bauxite=`red/claystone/shale/sapphire/hematite/aluminum`、Uraninite=`black/granite/gneiss/なし/galena/uranium`、Chromite=`black/gabbro/basalt/ruby/cobaltite/chromium`、Rutile=`red/gneiss/schist/sapphire/hematite/titanium`、Zircon=`brown/granite/gneiss/topaz/cassiterite/zirconium`、Cobaltite=`black/gabbro/schist/pyrite/sulfur/cobalt`、Stibnite=`black/shale/slate/amethyst/galena/antimony`、Native Platinum=`black/gabbro/basalt/なし/chromite/platinum`、Wolframite=`black/granite/gneiss/quartz/cassiterite/tungsten`、Thorianite=`black/granite/gneiss/なし/galena/thorium`、Native Iridium=`black/gabbro/basalt/なし/native_platinum/iridium`、Native Osmium=`black/gabbro/basalt/なし/native_platinum/osmium`、Spodumene=`white/granite/gneiss/topaz/cassiterite/lithium`、Carobbiite=`white/granite/gneiss/emerald/native_platinum/beryllium`、Native Naquadah=`black/gabbro/basalt/なし/native_osmium/naquadah`、Mithril Matrix=`white/gneiss/granite/amethyst/native_platinum/mithril`、Cinnabar=`red/quartzite/schist/ruby/sulfur/redstone_dust`、Cryolite=`white/granite/gneiss/topaz/spodumene/redstone_dust`、Graphite=`black/schist/phyllite/diamond/hematite/graphite_powder`、Sulfur=`yellow/tuff/basalt/pyrite/hematite/sulfur_powder`、Borax=`white/granite/gneiss/topaz/graphite/borax_powder`とする。Chromiteの加工itemとdataはFirmaLife導入時だけ有効にする。TFCのitem heatとDepositは読込条件を見ないため、heatは`c:metal/<form>/chromite`経由で参照し、DepositはFirmaLife導入時に`TFCM`のreload listenerで追加し、Native Platinumの副産物poolにも導入条件を付ける。
- Hammerは`#c:tools/hammer`をTFC remainderで1損耗させ、Small/Poor/Normal/Richを`1/3/5/7`個、品位なし金属を5個、非金属を7個のRocky Chunkへ加工する。一次SluiceはChunk 100%、主岩30%、副岩15%、定義済みGemだけ0.02%の独立pool、QuernはChunk 1→同系列Dirty Dust 1とする。
- 二次SluiceはPowder 100%、Sand Pile 15%、同系列Dirty Dust 5%、副産物BのDirty Dust 1%を独立poolで出す。Powder 4→Pellet 1、Pellet 4→Briquet 1と逆変換を用意し、金属系列のHeatingは5/20/80 mBとする。Boraxも同じ5/20/80 mBでBoronへ回収する。溶融レシピのないCinnabar、Cryolite、Graphite、Sulfurは流体を定義せず、Pellet、Briquetも登録しない。
- TFC 4.2.12の12系列とFirmaLife 3.0.15のChromiteについて、Poor/Normal/Richの既存Heating IDを同名のfalse conditionで無効化する。Small Oreの直接溶融は維持し、TFCM鉱石には元からPoor/Normal/Rich直接溶融を生成しない。
- Sand Pile 4個とSand 1個は7色とも相互にクラフトできる。洗浄工程を迂回するTFCのQuern（品位あり12系列の4段階、Sulfur、Graphite、Redstone）は同一IDで無効化する。TFC 4.2.12に同名のPowderがある12系列、Graphite、Sulfurの洗浄結果は`tfc:powder/<系列>`そのもので、TFCMに同じPowderを登録しない。
- 中間itemを`c:metal/<form>`と`c:metal/<form>/<series>`へ追加し、`c:ores/clean_dusts`は`c:metal/powder`を含む。Dirty Pileのtagは作らない。
- 処理itemのmodelはM06の鉱石ごとの`textureSource`とTFC powderを意図的に再利用する。Rocky/Chunkおよび粉体の工程差は形状別modelで表し、新しい第三者textureは追加しない。
- 変更後は`./gradlew runData`で生成物を同期し、`./gradlew classes build runGameTestServer`でTFC単独とFirmaLife追加の両runを検証する。GameTestは独立した35行の期待表と生成loot JSONを照合する。

## M08 surface resources

- 4鉱石×21岩石のTFC既存Deposit lootだけは、TFC 4.2.12の同一IDを置換する互換データとして`data/tfc/loot_table/deposit/`へ生成する。その他のDeposit定義とlootは`tfcm`名前空間を使用する。
- Gravel、Sand、Rock PowderのDeposit定義は`data/tfcm/tfc/deposit/surface_resource/`、loot IDは`tfcm:surface_resource/...`とし、loot pool名は出力と確率を表す一意名（Sandだけは表順の`output_<index>`）を使用する。
- Deposit、Gravel、7色Sand、Rock Powderの各出力は確率を正規化せず、出力ごとに独立したloot poolを生成する。GravelとSandのPowder、およびRock PowderのDirty DustはM07の処理itemへ直接接続する。
- Deposit 99件のPan表示はmaterialごとの`tfcm:item/pan/<name>_full`、`_half`と共通の`tfcm:item/pan/empty`を使う。`<name>`は`gravel/<rock>`、`sand/<color>`、`rock_powder`、`rocky_chunks/<series>`、`dirty_dust/<series>`で、親はTFCの同名modelとする。materialは不透明なblock textureとし、Gravel/Sandは同じ素材、Rock Powderは`minecraft:block/gravel`、Rocky Chunkは系列の主岩のGravel、Dirty Dustは系列のSandを使う。TFCは自身の4鉱石分しかPan modelを登録しないため、`TFCMClient`が`models/item/pan`を`ModelEvent.RegisterAdditional`へ登録する。
- Sluiceの内容はJEI（任意、API `19.32.0.358`をcompileOnly）の`tfcm:sluice`カテゴリに表示する。レシピの起点はTFCの`tfc:sluices`タグの全アイテムで、Panは起点にしない。追加アドオンのSluiceもこのタグへ入れれば同じ扱いになる。loot tableはクライアントへ同期されないため、`SluiceRecipe.build`がサーバーのDeposit全件と読込済みloot tableから入力・出力・確率を作り、`OnDatapackSyncEvent`（ログインと`/reload`）で`tfcm:sluice_recipes`ペイロードとして送る。データパックの変更も表示へ反映され、TFCのDepositも含む。
- Rock Powderは`tfcm:ore_processing/rock_powder`として登録し、専用texture `tfcm:item/ore_processing/rock_powder`を使用するが、製作・採取レシピは追加しない。変更後は`./gradlew runData`と両GameTest runで生成データとTFC Deposit登録を検証する。

## M09 範囲採掘工具

- Mining HammerとExcavatorは起点中央の3×3、Quarry Hammerは床・天井で起点中央の5×5、壁で起点の上3・下1・左右2の5×5を処理する。各工具の採掘速度は素材tierの40%とする。
- 追加候補ごとにmineable tagとtierを確認し、個別のBreak Eventと通常のサーバー破壊・loot処理を通す。Survivalでは1ブロックごとに耐久1を消費し、Creativeではdropと耐久消費を行わない。多層blockは通常破壊経路を通して1層だけ減らし、追加破壊を再帰起動しない。
- 3工具の攻撃補正はMining/Quarry Hammer 1.0、Excavator 0.9、攻撃速度補正は-3.1で、属性はTFCの`ToolItem.productAttributes`で作る。工具とHeadのrarityは金属のrarityとする。Quarry Hammer HeadはMining Hammer HeadとHeavy Sheetの金属tier Welding（bonusは`copy_best`）または焼成済みMoldへの800 mB鋳造で作る。金属量はHead 800 mB、完成工具850 mB、Moldの容量は800 mBで、Mining HammerとExcavatorはHead 200 mB、完成工具250 mBとする。
- 3工具とHeadはTFCの工具金属9種とInvar、Titanium、Netherite、Tungsten Steelへ展開する。Mining Hammer HeadはDouble Ingot 1個から`punch_last,upset_second_last,upset_third_last`、Excavator HeadはSheet 1個から`bend_last,upset_second_last,draw_third_last`を金属tierの金床で加工する。鋳造可能な4金属は焼成済みMoldで鋳造する。
- 完成工具の組立はTFC Mining HammerのTFC Metal Tools版を常に使い、`crafting/metal/<tool>/<metal>`へ条件なしで生成する。tier 2以下は` NX/ HG`（`#c:rivets/<metal>`、Head、`#c:rods/<metal>`、`#c:glue`）、tier 3以上は` NX/GHR/RL `で`#c:rings/<metal>`とLeather（Mining/Quarry Hammerは`#c:leather/string`、Excavatorは`#c:leather/strip`）を加え、Headの鍛造bonusを引き継ぐ。
- 3工具の持ち手は同金属のHammer（TFC金属はTFC 4.2.12、TFCM金属はTFCM）の柄に合わせ、木材色は共通、Head直下の上3段と末端3段の金属部（キャップ・カラーの有無と形）だけを金属ごとに変える。
- 未焼成MoldのKnapping patternはMining Hammer=`  ###/    #/#   #/#    /###  `、Quarry Hammer=`##   /##   /     /   ##/   ##`、Excavator=`#####/#   #/#   #/#   #/#   #`とし、1399℃で焼成する。
- 完成工具・Headのitem heatは金属固有値を使う。サイズ/重量は完成工具が`very_large/very_heavy`、Headが`large/medium`、Moldが`small/light`とする。外観はTFCのHammer、Mace、Shovelと各Head/Mold modelを再利用する。
- 3工具を`c:tools`と`tfc:tools/<metal>`へ、Mining HammerとQuarry Hammerを`tfc:deals_crushing_damage`へ追加する。
- 範囲工具データ変更後は`./gradlew runData`で同期し、`./gradlew classes build runGameTestServer`と`node tests/e2e/area-tools.mjs`で検証する。

## M10 geodes

- Quartz、Amethyst、Certus Quartzは`mineral/<mineral>_block`、`mineral/budding_<mineral>`、`mineral/small_<mineral>_bud`、`mineral/medium_<mineral>_bud`、`mineral/large_<mineral>_bud`、`mineral/<mineral>_cluster`を使用する。
- 成熟ClusterはQuartzがCut Quartz 2個、AmethystがTFC Amethyst Gem 2個、Certus QuartzがCertus Quartz 2個を落とす。Budding BlockはSmall、Medium、Large、Clusterの順に成長させる。
- Budding Blockはrandom tickの1/5で6方向から1方向を等確率に選び、その方向の空気または水源に成長する。既存Budは向きを保ち、最初のSmall Budは選択方向を向く。
- Bud/Clusterの高さと底面幅はSmall=`3/4`、Medium=`4/3`、Large=`5/3`、Cluster=`7/3`。Amethyst系はVanilla Amethystのblock・bud・cluster textureを再利用し、全Bud/Cluster modelはcutoutで描画する。
- 3種の標準晶洞は外殻TFC Hardened Basalt、中間Raw Quartzite、内層Budding:鉱物:Raw Quartziteを2:5:1とする。層厚設定はfilling/inner/middle/outer=`1.7/2.2/3.2/4.2`。Bud配置はalternate層を必須とせず、内層候補に含まれるBudding Blockから成長できる。
- 配置はY -48..32、各300チャンクに平均1回、`underground_ores` stepで行う。TFC 4.2.12に全Overworld biome用tagがないため`tfcm:tfc_overworld`をTFC biome registryから生成し、`tfcm:geode_blocks`には6 block種すべてを含めてpickaxe採掘tagへ接続する。
- `GeodeDataProvider`がmodel、blockstate、loot、configured/placed feature、biome modifierを`src/generated/resources`へ生成する。変更後は`./gradlew runData`、`./gradlew classes build runGameTestServer`、`node tests/e2e/geodes.mjs`で検証する。

## M11 YAML worldgen

- Common Configの`enableCustomVeinGeneration`が有効な場合、`config/tfcm/overworld.yaml`、`nether.yaml`、`end.yaml`を正規パスとして読み込む。旧パス・旧構文は扱わない。
- 起動時に、存在しないファイルだけを同梱の`src/main/resources/tfcm/worldgen/*.yaml`からコピーし、既存ファイルは上書きしない。同梱Overworldは旧実装の鉱脈46件（FirmaLife未導入時は該当2件を警告で除外）と3種の標準晶洞、Nether/Endは空定義とする。旧実装の既定に鉱脈がある独自鉱石はBauxite、Galena、Uraninite、Carobbiiteだけで、他の独自鉱石は既定では生成しない。
- 各YAMLは`veins`と`geodes`の配列を持つ。Veinは`id`、`type`、`ymin`、`ymax`、`blocks`が必須で、`type`は`tfc:cluster_vein`、`tfc:disc_vein`、`tfc:kaolin_disc_vein`、`tfc:pipe_vein`のいずれかとする。`blocks`は`block`と省略時1の正有限`weight`を持ち、`rocks`はOverworldで必須、Nether/Endでは省略時にそれぞれ`minecraft:netherrack`/`minecraft:end_stone`を使う。
- Veinの既定値は`rarity=1`、`density=1`、`size=1`、`height=1`、`radius=1`、`min_skew=max_skew=min_slant=max_slant=0`、`sign=0`、`project=false`、`project_offset=false`、`near_lava=false`。`tier`は品位名から正整数weightへのmapで、`{rock}`と`{tier}`を展開し、最終weightはblock weight×tier weightとする。`seed`指定を優先し、未指定時は`random_name`（既定は`id`）のMinecraft hash seedの上下64 bitをXORした値を使う。
- `indicator`は`block`が必須で、`rarity`は0を許容し、既定値は`rarity=1`、`depth=0`、`underground_rarity=1`、`underground_count=0`。Veinの`ymin`/`ymax`は必須で、type別の`density`、`size`、`height`、`radius`、`min_skew`、`max_skew`、`min_slant`、`max_slant`、`sign`と投影・溶岩条件をTFC configへ渡す。
- Geodeは`id`、`ymin`、`ymax`、`outer`、`middle`、`inner`、`filling`、`inner_placements`を持ち、`type`は省略時を含め`minecraft:geode`、`rarity`は省略時1とする。`inner`、`filling`、`inner_placements`は`{block, weight}`配列で、`outer`と`middle`は単一blockを指定する。
- Overworldに有効なYAML鉱脈があればTFCの鉱石鉱脈だけを置換し、なければTFC鉱脈を維持する。既存のGravel・Dike等は維持する。標準TFCM晶洞は除外してYAML晶洞だけを追加する。
- 置換対象は`tfc:vein/`と`firmalife:vein/`配下のplaced featureのうち`vein/gravel`、`vein/kaolin_disc`、`*_dike`以外とする。`GeodeDataProvider`がmodifier入口の`custom_worldgen.json`を生成する。
- Nether・Endは各YAMLに明示された鉱脈・晶洞だけを追加し、空設定では追加しない。未登録block、不正type・数値、重複IDは警告してエントリ単位で除外し、重複は先頭を採用する。
- M11 GameTestは3ディメンション分離、rock/tier template・weight・indicatorのTFC config展開、無効入力と空設定fallbackを検証する。

## M12 content toggles

- Common Configの`disabledMetals`は`Metal`の39 ID、`disabledOres`は`Ore`、`ProcessingSeries`、`TfcOre`（地表sampleのみの4種を含む）のIDを受け付ける。指定可能な全IDは設定ファイルのコメントへ自動出力する。
- 未知名は無視する。設定はレジストリ登録前に読み込み、変更反映にはゲーム再起動が必要。
- 登録対象と生成データには`tfcm:metal_enabled`、`tfcm:ore_enabled`のload conditionを適用し、集合tagでは任意entryを用いる。参照元のloot poolや合金入力は該当entryだけを無効化する。
- `runGameTestServerContentConfig`でLead、Galena、未知名を検証し、続く`runGameTestServerFreshConfig`で新規設定のコメントと既定値を検証する。

## Harness runtime

- 設定は `.harness-config.json`。ローカル生成物はGit管理外の `.harness-state/runtime/` に置く。
- MC Pilot `0.20.2` は `pilot/node_modules/.bin/mct`。`MCT_HOME` と `MCT_CACHE_DIR` はそれぞれruntime内の `mct-home` と `mct-cache` を指定する。
- Java 21、Minecraft `1.21.1`、NeoForge `21.1.265` をserver/client双方で使用する。
- serverは `server/`、接続先は `127.0.0.1:25583`、検証worldは `harness-world`。harnessは起動時にこのworldを作り直す。
- client名は `tfcm-a`、offline accountは `tfcm_a`。作成時のWebSocket portは `25584`。harness起動時には空きportへ割り当て直される。
- server/client双方にTFC `4.2.12` とPatchouli `1.21.1-93-NEOFORGE` を配置する。現状はFirmaLifeなしの基本構成。FirmaLife `3.0.15` の有無を比較する試験では別途双方へ配置・除去する。
- `node .harness/dist/cli/main.js doctor` と `validate` で構成を確認する。`plan` はClaude CLIへ仕様を送って計画を生成し、仕様・plan・progressをローカルGitへcommitする。
- runtime準備だけではTFCMの受入条件を検証したことにならない。Modの実装・build・GameTest・E2Eは後続の開発工程で行う。

## M13 content audit and assets

- GameTest `TFCMAssetIntegrityGameTests` audits registered resources, tags, heat and size, and Neutronium animation metadata. Its JUnit cases are registered in `tests/acceptance.json`.
- Equipment and Excavator models inherit TFC parent geometry. Their texture overrides must use texture keys from the actual TFC 4.2.12 parent models; armor uses the `tfcm` armor layer namespace. Mining Hammer and Quarry Hammer use dedicated item and head textures. The hammer tools belong to `c:tools/mining_tool`.
- Licence: `LICENCE` separates textures and models, which keep the licence of their source work, from everything else, which is MIT. Heavy Sheet sprites of Bismuth, Bismuth Bronze, Black Bronze, Black Steel, Blue Steel, Brass, Bronze, Cast Iron, and Copper are TFC More Items' own. Bauxite, Galena, and Uraninite Rocky Chunk and Powder keep the TFC Metallum Overhaul 1.5.1 sprites shared by the 18 Metallum series, not TFC Ore Washing's.
- Asset source: published TFC Metallum Overhaul `tfcm-1.5.1-mc1.21.1.jar`, https://www.curseforge.com/minecraft/mc-mods/tfc-metallum-overhaul/files/8553056, MIT licence (https://www.curseforge.com/minecraft/mc-mods/tfc-metallum-overhaul/license). Imported paths are `assets/tfcm/textures/block/metal/`, `block/mineral/`, `block/ore/`, `entity/projectiles/`, `item/gem/`, `item/metal/`, `item/ore/`, `item/powder/`, and `models/armor/`. Neutronium textures and `.png.mcmeta` animation files are in the metal paths. The nugget, Mining Hammer, and Quarry Hammer textures are project-created derivatives of the imported metal palette. The plate textures are project-created: a riveted plate shaded from the same metal's sheet palette (for TFC and FirmaLife metals, their own sheet palette), frame by frame for Neutronium. The dust textures recolor the Quartz Powder shape, the same shape as Rock Powder, with that palette. Rock Powder (Quartz Powder shape) and Leather String (Bamboo String shape) are project-created recolors. `ja_jp` translates every name except the mod name; TFC terms come from TFC's own `ja_jp`.
- Run `./gradlew runData`, `./gradlew classes build runGameTestServer`, then `node tests/e2e/asset-integrity.mjs` with the client harness running.

## M14 integrated verification

- `TFCMProcessingGameTests.everySeriesCompletesFiveProcessingSteps` verifies every enabled series against the hammer output and damage, both Sluice inputs and independent loot pools, Quern output, compression and unpacking, and appropriate heating. The FirmaLife GameTest run includes Chromite; the TFC-only run excludes it. The test also checks Native Naquadah, Mithril Matrix, and Dirty Pile absence.
- Verify static resources by parsing all JSON under `src/main/resources` and `src/generated/resources`, comparing the 39 metal IDs and forms and 35 processing series against `PROJECT.md`, and checking generated Quern and Sluice resources. Run `./gradlew runData` and inspect the generated resource diff for determinism.
- Run `./gradlew classes build runGameTestServer`, followed by the FirmaLife GameTest task, dedicated server and client startup, TFC equipment interactions, three-dimension generation with sample YAML, and client E2E in both configurations. The F-029 entries in `tests/acceptance.json` identify the GameTest evidence.
- Verification status: JSON parsing, metal model coverage, and processing resource coverage passed by direct inspection of the repository. Four compression recipes per metal series and Borax, and models, were also checked statically. Gradle commands remain unverified in this restricted environment. The wrapper could not write its lock file in the default Gradle cache; a writable cache allowed Gradle to start, but Gradle's file-lock socket was denied. A fresh cache could not download Gradle because network access was unavailable. Build, data generation determinism, both GameTest runs, server and client startup, manual TFC equipment use, sample-YAML world generation, and client E2E therefore have no new M14 runtime result. Static checks do not substitute for those runs.
- Open Questions: None. No new gameplay values were introduced for M14.
