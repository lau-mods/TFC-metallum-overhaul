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

## Harness runtime

- 設定は `.harness-config.json`。ローカル生成物はGit管理外の `.harness-state/runtime/` に置く。
- MC Pilot `0.20.2` は `pilot/node_modules/.bin/mct`。`MCT_HOME` と `MCT_CACHE_DIR` はそれぞれruntime内の `mct-home` と `mct-cache` を指定する。
- Java 21、Minecraft `1.21.1`、NeoForge `21.1.265` をserver/client双方で使用する。
- serverは `server/`、接続先は `127.0.0.1:25583`、検証worldは `harness-world`。harnessは起動時にこのworldを作り直す。
- client名は `tfcm-a`、offline accountは `tfcm_a`。作成時のWebSocket portは `25584`。harness起動時には空きportへ割り当て直される。
- server/client双方にTFC `4.2.12` とPatchouli `1.21.1-93-NEOFORGE` を配置する。現状はFirmaLifeなしの基本構成。FirmaLife `3.0.15` の有無を比較する試験では別途双方へ配置・除去する。
- `node .harness/dist/cli/main.js doctor` と `validate` で構成を確認する。`plan` はClaude CLIへ仕様を送って計画を生成し、仕様・plan・progressをローカルGitへcommitする。
- runtime準備だけではTFCMの受入条件を検証したことにならない。Modの実装・build・GameTest・E2Eは後続の開発工程で行う。
