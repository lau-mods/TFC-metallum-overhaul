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
- M02のGameTestは登録参照、仕様値とheat、鋳造、金床・溶接、合金、Andesite Alloyを検証する。`./gradlew classes build runGameTestServer`でTFC単独とFirmaLife追加の両runを実行する。

## M03 metal forms

- 39金属すべてに`foil`、`gear`、`heavy_sheet`、`nail`、`ring`、`rivet`、`screw`、`stamen`、`wire`、`plate`、`nugget`、`dust`を常設し、IDは`tfcm:metal/<form>/<metal>`とする。
- FoilはSheet 1→2、GearはDouble Sheet 1→1、Nail/Rivet/ScrewはStamen 1→5、RingはStamen 1→2、Stamen/WireはRod 1→2、PlateはIngot 1→1を金属tierのAnvilで作る。Heavy SheetはDouble Sheet 1とSheet 1→1のWeldingだけで作る。
- Gearは`punch_last,hit_second_last,upset_third_last`、Nailは`punch_last,hit_not_last,bend_not_last`、Ringは`hit_last,draw_second_last,draw_third_last`、Rivetは`draw_not_last,bend_any,hit_any`、Screwは`hit_any,hit_any,draw_last`、Stamenは`bend_last,draw_second_last,draw_third_last`、それ以外のAnvil形状はTFC Sheet/Rodと同じ`hit_last,hit_second_last,hit_third_last`を使用する。TFC 4.2.12本体には拡張形状とPlateの同名レシピはないため、拡張形状の数値・操作列はTFC本体からは照合できない。
- NuggetはIngot 1→9、Nugget 9→Ingot 1のCrafting Grid加工。溶融量はNugget 1個10 mB、9個90 mBとし、100 mBへの補正を行わない。DustはIngot 1→Dust 1のTFC Quern加工、PlateとDustの溶融量は各100 mB。Dustは鉱石Powderと別アイテム・別タグにする。
- 金属別common item tagは`c:<form>s/<metal>`（Heavy Sheetは`c:heavy_sheets/<metal>`）を用い、集合タグ`c:<form>s`にも含める。各形状のHeating、item heat、size/weight、翻訳、modelとtextureを生成・登録する。
- `./gradlew runData`で生成物を同期し、`./gradlew classes build runGameTestServer`でTFC単独とFirmaLife追加の両GameTest runを検証する。M03 GameTestは9拡張形状の登録・加工・熱、Plate、Nugget、Dust、39金属への適用を確認する。

## M04 material parts

- 革、植物、竹、弓・弩、矢、工具組立用の部品は部品名のflat item IDを使用する。Plant Blockだけは`tfcm:plant_block`のblock/itemとして登録する。
- 草は素手でPlant Fiber 1個、Knifeで2個を落とす。Knifeを使う加工はTFC Advanced Shapeless Craftingの`damage_crafting_remainder`で1耐久を消費する。
- Plant Blockはrandom tickごとに`drying=0..3`を進め、その次のtickで`tfc:thatch`へ置換する。独自tierや効果は持たせない。
- 完成Bow、Crossbow、Arrowは独自完成品を登録せず、各中間部品からVanillaの完成品へ組み立てる。全素材部品とPlant BlockにTFC item size/weightを定義する。
- `MaterialDataProvider`がrecipe、grass/block loot、item size、model、blockstateを`src/generated/resources`へ生成する。M04 GameTestは登録と属性、植物・竹加工と乾燥、完成品組立とKnife損耗を検証し、`tests/e2e/material-parts.mjs`は全素材モデルを一画面で検証する。

## M05 equipment and anvils

- Invar、Titanium、Netherite、Tungsten Steelの工具・工具頭・武器・防具・補助装備は`tfcm:metal/<part>/<metal>`、金床は`tfcm:metal/anvil/<metal>`を使用する。工具tierは順にlevel `3/5/6/7`、耐久`2200/3300/6500/8125`、速度`8/11/12/13`、攻撃補正`4.75/7/9/10.5`、Enchantability `12/17/22/25`とする。
- 防具のBoots/Leggings/Chestplate/Helmet防御はInvar `1/4/5/2`、Titanium `2/5/6/2`、Netherite `3/6/8/3`、Tungsten Steel `4/7/9/4`。耐久は順に`429/495/528/370`、`650/750/800/550`、`884/1020/1010/715`、`1105/1275/1360/935`、Enchantabilityは`12/17/23/25`、Toughnessは`0/2/3/4`、Knockback抵抗は`0/0.05/0.10/0.15`とする。
- 工具組立にはF-006のHandle、Binding、Tool Riveting由来のCrossguard/Pommelと、F-004のRing・Rivet・Wireを使用する。Crossguard/Pommelは専用アートが確定するまで中立な小金属部品モデルを用いる。
- Tong PartとTongsは`tfchotornot`導入時だけ登録し、Tongsは同Modの公開`TongsItem`を使用する。`tfchotornot:tongs`へ統合するが`insulating`には追加せず、独自の耐熱倍率や保持効果を付けない。
- 4金属の金床tierは`3/5/6/7`。同金属のDouble Ingot 7個で製作し、Heatingで同金属1400 mBへ戻す。TFC Anvil block entityと`tfc:anvils`、pickaxe採掘tagへ統合する。
- `EquipmentDataProvider`が装備のAnvil/Welding/Crafting/Heating、item heat・size、tool/tag、model、金床のrecipe・loot・blockstateを`src/generated/resources`へ生成する。変更後は`./gradlew runData`、`./gradlew classes build runGameTestServer`、`node tests/e2e/equipment-anvils.mjs`で検証する。

## M06 ores and gems

- 独自鉱石は品位あり16種と品位なし3種を`Ore`で定義する。各TFC母岩、Netherrack、End Stone版は`tfcm:ore/<ore item name>/<host>`、Small Oreと地表sampleは`tfcm:ore/small_<ore>`を使用する。
- TFCの12×3品位鉱石と17品位なし鉱石にはNetherrack・End Stone版を追加し、元のTFC鉱石itemをdropする。FirmaLife Chromite 3品位×2版はFirmaLife導入時だけ登録し、生成データはmod-loaded conditionを付ける。
- Nether鉱石は硬度3・耐爆10、End鉱石は硬度3・耐爆9、TFC母岩版はTFCの母岩category hardnessを使用する。全鉱石blockを`c:ores`、`minecraft:mineable/pickaxe`、`tfc:prospectable`、`tfc:mineable/propick`へ追加する。探鉱表示は標準block translation key末尾の`.prospected`を使用する。
- QuartzとFluoriteはQuernで各Powder 4個にし、両PowderをTFC gem powder・bowl powder tagへ追加する。Cut QuartzはTFC Sandpaperの損耗を使い、Certus Quartzとは別itemとして登録する。
- `OreDataProvider`がblockstate、model、loot、tag、heating、quern、研磨を`src/generated/resources`へ生成し、langは`src/main/resources/assets/tfcm/lang/`で手動管理する。変更後は`./gradlew runData`と両GameTest runで機能を検証し、`node tests/e2e/ore-samples.mjs`で地表sampleの表示を検証する。

## M07 ore processing

- 35系列の中間item IDは`tfcm:ore_processing/<form>/<series>`（`rocky_chunks`、`chunks`、`dirty_dust`、`powder`、`pellet`、`briquet`）、7色の砂は`tfcm:ore_processing/sand_pile/<color>`を用いる。Dirty Pileはitem、recipe、loot、tag、lang、modelのいずれにも生成しない。
- `ProcessingSeries`をF-010表の正本とし、各行を`砂/主岩/副岩/Gem/副産物B/回収先`で表す。Native Copper=`red/basalt/andesite/opal/hematite/copper`、Native Gold=`yellow/quartzite/granite/quartz/native_silver/gold`、Hematite=`red/quartzite/shale/なし/sulfur/cast_iron`、Native Silver=`white/gneiss/granite/amethyst/galena/silver`、Cassiterite=`white/granite/gneiss/topaz/bismuthinite/tin`、Bismuthinite=`black/granite/schist/pyrite/galena/bismuth`、Garnierite=`green/gabbro/basalt/opal/limonite/nickel`、Malachite=`green/limestone/dolomite/opal/limonite/copper`、Magnetite=`black/gabbro/basalt/なし/hematite/cast_iron`、Limonite=`brown/claystone/shale/opal/sulfur/cast_iron`、Sphalerite=`brown/dolomite/limestone/amethyst/galena/zinc`、Tetrahedrite=`black/schist/dolomite/pyrite/sphalerite/copper`、Galena=`black/limestone/dolomite/amethyst/sphalerite/lead`、Magnesite=`white/dolomite/gabbro/opal/limonite/magnesium`、Bauxite=`red/claystone/shale/sapphire/hematite/aluminum`、Uraninite=`black/granite/gneiss/なし/galena/uranium`、Chromite=`black/gabbro/basalt/ruby/cobaltite/chromium`、Rutile=`red/gneiss/schist/sapphire/hematite/titanium`、Zircon=`brown/granite/gneiss/topaz/cassiterite/zirconium`、Cobaltite=`black/gabbro/schist/pyrite/sulfur/cobalt`、Stibnite=`black/shale/slate/amethyst/galena/antimony`、Native Platinum=`black/gabbro/basalt/なし/chromite/platinum`、Wolframite=`black/granite/gneiss/quartz/cassiterite/tungsten`、Thorianite=`black/granite/gneiss/なし/galena/thorium`、Native Iridium=`black/gabbro/basalt/なし/native_platinum/iridium`、Native Osmium=`black/gabbro/basalt/なし/native_platinum/osmium`、Spodumene=`white/granite/gneiss/topaz/cassiterite/lithium`、Carobbiite=`white/granite/gneiss/emerald/native_platinum/beryllium`、Native Naquadah=`black/gabbro/basalt/なし/native_osmium/naquadah`、Mithril Matrix=`white/gneiss/granite/amethyst/native_platinum/mithril`、Cinnabar=`red/quartzite/schist/ruby/sulfur/redstone_dust`、Cryolite=`white/granite/gneiss/topaz/spodumene/redstone_dust`、Graphite=`black/schist/phyllite/diamond/hematite/graphite_powder`、Sulfur=`yellow/tuff/basalt/pyrite/hematite/sulfur_powder`、Borax=`white/granite/gneiss/topaz/graphite/borax_powder`とする。Chromiteの加工dataだけはFirmaLife導入時に有効にする。
- Hammerは`#c:tools/hammer`をTFC remainderで1損耗させ、Small/Poor/Normal/Richを`1/3/5/7`個、品位なし金属を5個、非金属を7個のRocky Chunkへ加工する。一次SluiceはChunk 100%、主岩30%、副岩15%、定義済みGemだけ0.02%の独立pool、QuernはChunk 1→同系列Dirty Dust 1とする。
- 二次SluiceはPowder 100%、Sand Pile 15%、同系列Dirty Dust 5%、副産物BのDirty Dust 1%を独立poolで出す。Powder 4→Pellet 1、Pellet 4→Briquet 1と逆変換を用意し、金属系列のHeatingは5/20/80 mBとする。Borax Powderだけは5 mBのBoron回収へ接続し、ほかの非金属と非金属Pellet/Briquetに流体を定義しない。
- TFC 4.2.12の12系列とFirmaLife 3.0.15のChromiteについて、Poor/Normal/Richの既存Heating IDを同名のfalse conditionで無効化する。Small Oreの直接溶融は維持し、TFCM鉱石には元からPoor/Normal/Rich直接溶融を生成しない。
- 処理itemのmodelはM06の鉱石ごとの`textureSource`とTFC powderを意図的に再利用する。Rocky/Chunkおよび粉体の工程差は形状別modelで表し、新しい第三者textureは追加しない。
- 変更後は`./gradlew runData`で生成物を同期し、`./gradlew classes build runGameTestServer`でTFC単独とFirmaLife追加の両runを検証する。GameTestは独立した35行の期待表と生成loot JSONを照合する。

## M08 surface resources

- 4鉱石×21岩石のTFC既存Deposit lootだけは、TFC 4.2.12の同一IDを置換する互換データとして`data/tfc/loot_table/deposit/`へ生成する。その他のDeposit定義とlootは`tfcm`名前空間を使用する。
- Gravel、Sand、Rock PowderのDeposit定義は`data/tfcm/tfc/deposit/surface_resource/`、loot IDは`tfcm:surface_resource/...`とし、loot pool名は出力と確率を表す一意名（Sandだけは表順の`output_<index>`）を使用する。
- Deposit、Gravel、7色Sand、Rock Powderの各出力は確率を正規化せず、出力ごとに独立したloot poolを生成する。GravelとSandのPowder、およびRock PowderのDirty DustはM07の処理itemへ直接接続する。
- Gravel、Sand、Rock Powderは鉱石を含む見た目を示さないTFC 4.2.12の汎用Pan model stages `tfc:item/pan/full`、`half`、`empty`を使用する。
- Rock Powderは`tfcm:ore_processing/rock_powder`として登録し、item modelのtextureに`tfc:item/powder/flux`を再利用するが、製作・採取レシピは追加しない。変更後は`./gradlew runData`と両GameTest runで生成データとTFC Deposit登録を検証する。

## M09 範囲採掘工具

- Mining HammerとExcavatorは起点中央の3×3、Quarry Hammerは床・天井で起点中央の5×5、壁で起点の上3・下1・左右2の5×5を処理する。各工具の採掘速度は素材tierの40%とする。
- 追加候補ごとにmineable tagとtierを確認し、個別のBreak Eventと通常のサーバー破壊・loot処理を通す。Survivalでは1ブロックごとに耐久1を消費し、Creativeではdropと耐久消費を行わない。多層blockは通常破壊経路を通して1層だけ減らし、追加破壊を再帰起動しない。
- 3工具とHeadはTFCの工具金属9種とInvar、Titanium、Netherite、Tungsten Steelへ展開する。HeadはIngot 1個を金属tierの金床で加工し、Mining/Quarry Hammerは`punch_last,shrink_not_last`、Excavatorは`punch_last,hit_not_last`を使用する。鋳造可能な4金属は焼成済みMoldと100 mBで鋳造する。
- 未焼成MoldのKnapping patternはMining Hammer=`#####/     /     /## ##/#####`、Quarry Hammer=`#####/ # # /     /#   #/#####`、Excavator=`#####/ ### /  #  /  #  /     `とし、1399℃で焼成する。
- 完成工具・Headのitem heatは金属固有値を使う。サイズ/重量は完成工具が`very_large/very_heavy`、Headが`large/medium`、Moldが`small/light`とする。外観はTFCのHammer、Mace、Shovelと各Head/Mold modelを再利用する。
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
- 各YAMLは`veins`と`geodes`の配列を持つ。Veinは`id`、`type`、`ymin`、`ymax`、`blocks`が必須で、`type`は`tfc:cluster_vein`、`tfc:disc_vein`、`tfc:kaolin_disc_vein`、`tfc:pipe_vein`のいずれかとする。`blocks`は`block`と省略時1の正有限`weight`を持ち、`rocks`はOverworldで必須、Nether/Endでは省略時にそれぞれ`minecraft:netherrack`/`minecraft:end_stone`を使う。
- Veinの既定値は`rarity=1`、`density=1`、`size=1`、`height=1`、`radius=1`、`min_skew=max_skew=min_slant=max_slant=0`、`sign=0`、`project=false`、`project_offset=false`、`near_lava=false`。`tier`は品位名から正整数weightへのmapで、`{rock}`と`{tier}`を展開し、最終weightはblock weight×tier weightとする。`seed`指定を優先し、未指定時は`random_name`（既定は`id`）のMinecraft hash seedの上下64 bitをXORした値を使う。
- `indicator`は`block`が必須で、既定値は`rarity=1`、`depth=0`、`underground_rarity=1`、`underground_count=0`。Veinの`ymin`/`ymax`は必須で、type別の`density`、`size`、`height`、`radius`、`min_skew`、`max_skew`、`min_slant`、`max_slant`、`sign`と投影・溶岩条件をTFC configへ渡す。
- Geodeは`id`、`ymin`、`ymax`、`outer`、`middle`、`inner`、`filling`、`inner_placements`を持ち、`type`は省略時を含め`minecraft:geode`、`rarity`は省略時1とする。`inner`、`filling`、`inner_placements`は`{block, weight}`配列で、`outer`と`middle`は単一blockを指定する。
- Overworldに有効なYAML鉱脈があればTFCの鉱石鉱脈だけを置換し、なければTFC鉱脈を維持する。既存のGravel・Dike等は維持する。標準TFCM晶洞は除外してYAML晶洞だけを追加する。
- 置換対象は`tfc:vein/`配下のplaced featureのうち`vein/gravel`と`*_dike`以外とする。`GeodeDataProvider`がmodifier入口の`custom_worldgen.json`を生成する。
- Nether・Endは各YAMLに明示された鉱脈・晶洞だけを追加し、空設定では追加しない。未登録block、不正type・数値、重複IDは警告してエントリ単位で除外し、重複は先頭を採用する。
- M11 GameTestは3ディメンション分離、rock/tier template・weight・indicatorのTFC config展開、無効入力と空設定fallbackを検証する。

## M12 content toggles

- Common Configの`disabledMetals`は`Metal`の39 ID、`disabledOres`は`Ore`、`ProcessingSeries`、`TfcOre`のIDを受け付ける。指定可能な全IDは設定ファイルのコメントへ自動出力する。
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
- Asset source: published TFC Metallum Overhaul `tfcm-1.5.1-mc1.21.1.jar`, https://www.curseforge.com/minecraft/mc-mods/tfc-metallum-overhaul/files/8553056, MIT licence (https://www.curseforge.com/minecraft/mc-mods/tfc-metallum-overhaul/license). Imported paths are `assets/tfcm/textures/block/metal/`, `block/mineral/`, `block/ore/`, `entity/projectiles/`, `item/gem/`, `item/metal/`, `item/ore/`, `item/powder/`, and `models/armor/`. Neutronium textures and `.png.mcmeta` animation files are in the metal paths. The plate, nugget, dust, Mining Hammer, and Quarry Hammer textures are project-created derivatives of the imported metal palette.
- Run `./gradlew runData`, `./gradlew classes build runGameTestServer`, then `node tests/e2e/asset-integrity.mjs` with the client harness running.
