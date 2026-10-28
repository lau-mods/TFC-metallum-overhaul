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

## Harness runtime

- 設定は `.harness-config.json`。ローカル生成物はGit管理外の `.harness-state/runtime/` に置く。
- MC Pilot `0.20.2` は `pilot/node_modules/.bin/mct`。`MCT_HOME` と `MCT_CACHE_DIR` はそれぞれruntime内の `mct-home` と `mct-cache` を指定する。
- Java 21、Minecraft `1.21.1`、NeoForge `21.1.265` をserver/client双方で使用する。
- serverは `server/`、接続先は `127.0.0.1:25583`、検証worldは `harness-world`。harnessは起動時にこのworldを作り直す。
- client名は `tfcm-a`、offline accountは `tfcm_a`。作成時のWebSocket portは `25584`。harness起動時には空きportへ割り当て直される。
- server/client双方にTFC `4.2.12` とPatchouli `1.21.1-93-NEOFORGE` を配置する。現状はFirmaLifeなしの基本構成。FirmaLife `3.0.15` の有無を比較する試験では別途双方へ配置・除去する。
- `node .harness/dist/cli/main.js doctor` と `validate` で構成を確認する。`plan` はClaude CLIへ仕様を送って計画を生成し、仕様・plan・progressをローカルGitへcommitする。
- runtime準備だけではTFCMの受入条件を検証したことにならない。Modの実装・build・GameTest・E2Eは後続の開発工程で行う。
