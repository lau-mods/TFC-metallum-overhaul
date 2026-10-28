# Project
Status: active  
Project ID: tfc-metallum-overhaul
Mod ID: tfcm

## Platform
Minecraft: 1.21.1  
NeoForge: 21.1.265  
Java: 21

## Purpose

TerraFirmaCraft（以下「TFC」）の冶金・鉱山・工具製作・資源採取体系を拡張する、Minecraft 1.21.1専用の単一Modを開発する。金属、鉱石、加工形状、金属工具の部品組立、鉱石洗浄、範囲採掘、晶洞、およびワールド生成を `tfcm` 内で完結させる。

本書は**新規実装の要求仕様**である。現行版からは利用許諾上使用してよいアセットを取得するが、現行版のソースコード、内部設計、既存レジストリ構成、非公開レシピ実装を知っていることを前提にしない。必要なゲーム上の動作は本書に記載する。記載のない数値・加工手順・効果・入手方法は創作しない。

規範上の優先順位は、本書の明示仕様、TFC 4.2.12の公開されたゲーム上の規則、FirmaLife 3.0.15の公開されたゲーム上の規則の順とする。後方互換性は要求しない。特に指定のない機能は本書に列挙した範囲をすべて実装する。コードの基本パッケージは `net.claustra01.tfcm` とする。

## Features

### F-001: 対応環境と依存関係
#### Description

Minecraft 1.21.1 / NeoForge / Java 21専用とする。2026-10-28時点の実装・検証基準は、NeoForge 21.1.265、TFC 4.2.12、FirmaLife 3.0.15、Patchouli 1.21.1-93-NEOFORGEである。TFCは必須、FirmaLifeは追加鉱石・金属との連携時のみ必要とする。PatchouliはTFCの必須依存として環境に含める。TFCM自身が独立して必要とする追加の実行時依存は設けない。Tongsの登録のみ、対応機能を提供するModが導入されている場合に限る。

TFCMはクライアント・専用サーバー双方で動作し、共通処理からクライアント専用クラスを参照しない。対象Minecraft版以外のビルド、旧版のデータ変換、旧IDへのalias、ワールド移行機能は実装しない。

#### Acceptance Criteria
##### AC-F001-001: 単独導入
Preconditions: Minecraft 1.21.1、対応NeoForge、TFC 4.2.12およびTFCが要求するPatchouliを導入した環境がある。  
Action: TFCMを追加し、クライアントと専用サーバーを起動する。  
Expected Result: 必要な登録、レシピ読込、ワールド作成が正常に完了する。任意の追加Modを導入しなくても本書の基本機能が動作する。

##### AC-F001-002: FirmaLife対応
Preconditions: 上記環境にFirmaLife 3.0.15を追加した環境と、追加しない環境を用意する。  
Action: 両環境を起動し、鉱石登録と関連レシピを確認する。  
Expected Result: 導入時のみ対応するChromite・金属処理が有効になり、未導入時もTFCM単体の機能がすべて正常に動作する。

##### AC-F001-003: 対応版限定
Preconditions: Minecraft 1.21.1以外の環境、または必須依存が欠けた環境を用意する。  
Action: TFCMをロードする。  
Expected Result: 依存関係の不一致として明確に拒否される。旧形式を読み込むための移行処理は行われない。

### F-002: 独自金属39種
#### Description

以下39種の独自金属を登録する。表の鍛造tierと融点は今回の設計上の基準値である。全金属に熱容量・温度・流体・色・表示名・タグを持たせ、TFCの金属取り扱いに統合する。工具対象欄の「○」だけにF-007の完全な工具・防具体系を付与する。

| 金属ID | 鍛造tier | 融点 ℃ | 工具対象 |
|---|---:|---:|:---:|
| `compressed_iron` | 3 | 1535 | — |
| `platinum` | 3 | 1730 | — |
| `naquadah` | 7 | 2000 | — |
| `iridium` | 3 | 1535 | — |
| `osmium` | 3 | 1535 | — |
| `osmiridium` | 3 | 1500 | — |
| `mithril` | 2 | 940 | — |
| `arcane` | 2 | 940 | — |
| `refined_glowstone` | 2 | 940 | — |
| `refined_obsidian` | 2 | 940 | — |
| `antimony` | 1 | 630 | — |
| `titanium` | 5 | 1700 | ○ |
| `cobalt` | 3 | 1700 | — |
| `lithium` | 3 | 181 | — |
| `aluminum` | 3 | 1740 | — |
| `constantan` | 1 | 1250 | — |
| `invar` | 3 | 1250 | ○ |
| `electrum` | 1 | 1000 | — |
| `lead` | 1 | 330 | — |
| `uranium` | 3 | 1130 | — |
| `tungsten` | 3 | 1535 | — |
| `solder` | 3 | 400 | — |
| `tungsten_steel` | 7 | 1535 | ○ |
| `netherite` | 6 | 1535 | ○ |
| `dawnstone` | 1 | 1000 | — |
| `andesite_alloy` | 2 | 520 | — |
| `draconium` | 7 | 2000 | — |
| `awakened_draconium` | 7 | 2000 | — |
| `boron` | 3 | 2076 | — |
| `thorium` | 3 | 1750 | — |
| `magnesium` | 2 | 650 | — |
| `beryllium` | 3 | 1287 | — |
| `zirconium` | 2 | 1855 | — |
| `neutronium` | 7 | 3000 | — |
| `ferroboron` | 5 | 1500 | — |
| `tough_alloy` | 5 | 1600 | — |
| `zircaloy` | 5 | 1855 | — |
| `hsla_steel` | 5 | 1450 | — |
| `super_alloy` | 5 | 1800 | — |

全金属の基本形状は `ingot`、`double_ingot`、`sheet`、`double_sheet`、`rod`、metal block、block slab、block stairsの8種とする。通常のアイテムIDは `tfcm:metal/<form>/<metal>`、ブロックIDは `tfcm:metal/block/<metal>`、スラブと階段は `<metal>_slab` と `<metal>_stairs` を使用する。高炭素中間素材 `tfcm:metal/ingot/high_carbon_tungsten_steel` を別途登録する。

#### Acceptance Criteria
##### AC-F002-001: 金属と形状の完全登録
Preconditions: 内容無効化設定が空の新規環境がある。  
Action: 登録済みの金属・形状・block・fluidを列挙する。  
Expected Result: 表の39金属と各8形状、固有の溶融流体がすべて存在し、重複IDや未登録参照がない。

##### AC-F002-002: 金属固有値
Preconditions: 39金属の仕様値を読み込んだ環境がある。  
Action: 融点、鍛造tier、工具提供対象を一覧表と照合する。  
Expected Result: 各値が一致し、工具対象外の金属に架空の完全工具セットを登録しない。

##### AC-F002-003: 高炭素中間素材
Preconditions: Tungsten Steelが有効な環境がある。  
Action: 高炭素Tungsten Steelインゴットを確認する。  
Expected Result: 通常の39金属とは別の中間アイテムとして存在し、F-003の鋼加工に使用される。

### F-003: 金属の鋳造・鍛造・溶接・合金
#### Description

TFC標準のCrucible、鋳型、Anvil、Welding、Heatingを用いる。各金属に通常のIngot MoldとFire Ingot Moldによる鋳造を実装し、鋳型破損率はそれぞれ10%、1%とする。金床加工でsheet・rod、溶接でdouble ingot・double sheetを製造する。一般金属は融点の60%を鍛造可能、80%を溶接可能の基準温度とし、TFCの熱・鍛冶判定を用いる。Draconium系は鍛造1200℃、溶接1600℃とする。

合金配合は次のとおりとする。パーセンテージは溶融金属の割合であり、示された範囲内で合金化する。

| 生成合金 | 配合 |
|---|---|
| Constantan | Copper 40–60%、Nickel 40–60% |
| Electrum | Gold 40–60%、Silver 40–60% |
| Invar | Wrought Iron 60–70%、Nickel 30–40% |
| Osmiridium | Osmium 60–80%、Iridium 20–40% |
| Solder | Antimony 40–60%、Bismuth 20–40%、Tin 20–40% |
| Andesite Alloy | Molten Andesite 89–91%、Zinc 9–11%、またはTin 9–11% |
| Ferroboron | Boron 50%、Steel 50% |
| Tough Alloy | Ferroboron 50%、Lithium 50% |
| Zircaloy | Zirconium 87.5%、Tin 12.5% |
| HSLA Steel | Iron 93.75%、Steel 6.25% |
| Super Alloy | Tough Alloy 45–55%、Titanium 45–55% |

Molten Andesiteを独立流体として登録し、TFCのLoose Andesite 1個を1300℃で90 mBに溶融する。これをZincまたはTin 10 mBと混合してAndesite Alloy 100 mBを得る。表に合金配合のない特殊金属について、独自の合成レシピは追加しない。

金属固有のHeatingは、インゴット、ダブルインゴット、sheet、double sheet、rod、metal blockおよびF-004・F-005で追加する金属形状を対象とする。流体量は投入アイテムに含まれる金属量と整合させる。ただしF-005のNuggetクラフトは明示的な数量例外とする。

#### Acceptance Criteria
##### AC-F003-001: 鋳造
Preconditions: 対応金属の溶融流体と2種類の鋳型がある。  
Action: それぞれの鋳型でIngotを鋳造する。  
Expected Result: 両鋳造経路が機能し、鋳型破損率が10%と1%に設定される。

##### AC-F003-002: 金床・溶接
Preconditions: 対象tierを満たす金床、材料、熱源がある。  
Action: Sheet・Rod・Double Ingot・Double Sheetの製造と溶融を行う。  
Expected Result: TFC方式で製造でき、金属の温度条件と材料量を正しく扱う。

##### AC-F003-003: 合金
Preconditions: 合金表に示す溶融材料がある。  
Action: 有効な比率と無効な比率で合金化を試す。  
Expected Result: 有効な配合のみが所定の合金になり、他の配合や未定義の特殊金属製造を受け入れない。

##### AC-F003-004: Andesite Alloy
Preconditions: Loose Andesite、ZincまたはTinとTFC加熱設備がある。  
Action: Andesiteの溶融、合金化を行う。  
Expected Result: Andesite 1個から90 mB、追加金属10 mBからAndesite Alloy 100 mBを得る。

### F-004: 拡張金属形状
#### Description

全39金属に `foil`、`gear`、`heavy_sheet`、`nail`、`ring`、`rivet`、`screw`、`stamen`、`wire` の9形状を常設する。IDは `tfcm:metal/<form>/<metal>` とする。Foil・Gear・Nail・Ring・Rivet・Screw・Stamen・WireはTFC Anvil加工、Heavy SheetはWeldingで製造する。各形状の素材投入量、完成個数、鍛造操作列は基準となるTFC加工ルールと一致させる。根拠のない独自の消費量は設定しない。

加工形状すべてにTFC Heating、item size/weight、金属別および形状別common tag、翻訳、アイコン、モデルを用意する。

#### Acceptance Criteria
##### AC-F004-001: 9形状の登録
Preconditions: 全39金属が有効な環境がある。  
Action: 拡張形状を列挙する。  
Expected Result: 39金属それぞれに9形状があり、特定の追加Modの有無に依存しない。

##### AC-F004-002: TFC加工と熱
Preconditions: 必要な金属材料とTFC Anvil・Welding設備がある。  
Action: 拡張形状を製作・再加熱・再溶融する。  
Expected Result: 加工方法が形状ごとに定義され、熱・量・タグが欠落しない。

### F-005: Plate・Nugget・Dust
#### Description

各金属に次の3形状を追加する。Plateは既存のSheetとは別アイテムであり、既存Sheetの製作・用途は変更しない。

| 形状 | アイテムID | 入力と加工 | 出力 | 溶融量 |
|---|---|---|---|---:|
| Plate | `tfcm:metal/plate/<metal>` | Ingot ×1、TFC Anvil | Plate ×1 | 100 mB |
| Nugget | `tfcm:metal/nugget/<metal>` | Ingot ×1、Crafting Grid | Nugget ×9 | 1個10 mB |
| Nugget逆変換 | 同上 | Nugget ×9、Crafting Grid | Ingot ×1 | Ingotは100 mB |
| Dust | `tfcm:metal/dust/<metal>` | Ingot ×1、TFC Quern | Dust ×1 | 100 mB |

Plateの金床tierは対象金属の鍛造tierに一致させる。金床操作はTFCの通常の金属成形規則を用いる。Dustは金属インゴットを粉砕した粉であり、鉱石由来のDirty Dust・Powderとは識別する。

Nuggetの「9個でIngot 1個」と「Nugget 1個の溶融量10 mB」は**併存する確定仕様**である。Nuggetを9個溶かした場合は90 mBとなる。これを自動的に100 mBへ補正しない。

#### Acceptance Criteria
##### AC-F005-001: Plate
Preconditions: 対象金属のIngot、適正tierのTFC Anvilがある。  
Action: Ingot 1個をPlateへ加工し、加熱・溶融する。  
Expected Result: Plate 1個が生成され、同金属100 mBへ溶融する。Sheetとは異なるIDである。

##### AC-F005-002: Nugget相互クラフト
Preconditions: 対象金属のIngotとCrafting Gridがある。  
Action: Ingotを9個のNuggetに変換し、9個をIngotへ戻す。  
Expected Result: 9個単位で相互クラフトできる。Nugget 1個を溶かしたときは10 mB、9個なら90 mBとなる。

##### AC-F005-003: Dust
Preconditions: 対象金属のIngotとTFC Quernがある。  
Action: Ingotを粉砕し、生成物を溶融する。  
Expected Result: Dustが1個生成され、同金属100 mBへ溶融する。鉱石のPowderへ混同・変換されない。

##### AC-F005-004: 全金属への適用
Preconditions: 39金属を有効にした環境がある。  
Action: Plate、Nugget、Dustの登録・レシピ・熱・タグ・アセットを検査する。  
Expected Result: 対象金属間で欠落がなく、追加形状の参照に未登録IDを使用しない。

### F-006: 革・植物・竹・弓矢の素材加工
#### Description

次の独立した素材・部品と、そのTFC環境で成立する加工・組立を本体に実装する。

| 系統 | 対象 |
|---|---|
| 革 | String、Strap、Strip |
| 植物 | Plant Fiber、Plant String、Plant Fabric、Plant Block、Plant Clothing |
| 竹 | Bamboo Fiber、Bamboo String |
| 弓・弩 | Bow Limb、Bow Chordおよび完成Bow/Crossbowに必要な部品 |
| 矢 | Tread、Fletch、ShaftおよびArrowの組立部品 |
| 工具補助 | Handle、Binding、RivetingなどF-007の工具組立で必要な部品 |

植物繊維は草を手またはKnifeで処理して得る。Plant Blockは設置後に乾燥してThatchになる。竹、革、弓、弩、矢の加工ではTFCの工具と通常のクラフト・加熱・損耗処理を使用する。レシピの材料数・加工個数・工具損耗はTFCの対応レシピに準じ、独自素材の性能を創作しない。新たな技術段階や効果は追加しない。

#### Acceptance Criteria
##### AC-F006-001: 素材登録
Preconditions: TFCが動作する環境がある。  
Action: 表の素材・部品を確認する。  
Expected Result: 必要なアイテム、名称、モデル、サイズ、重量が登録される。

##### AC-F006-002: 植物と竹
Preconditions: 草、竹、適切な工具がある。  
Action: 繊維を採取・加工し、Plant Blockを設置する。  
Expected Result: 草と竹から対応素材を製造でき、Plant Blockが乾燥してThatchに変化する。

##### AC-F006-003: 弓矢と工具部品
Preconditions: 必要素材と工具がある。  
Action: Bow、Crossbow、Arrowおよび工具用部品を製作する。  
Expected Result: 中間部品が完成品へ組み込まれ、Crafting RemainderとKnife損耗が正常に処理される。

### F-007: 工具・武器・防具と部品組立
#### Description

`invar`、`titanium`、`netherite`、`tungsten_steel` の4金属で、TFCが持つ金属製工具、tool head、武器、Shears、Tuyere、Fishing Rod、Shield、Horse Armor、防具、中間防具を展開する。SwordのCrossguardとPommel、その他の工具に必要なHandle・Ring・Rivet・Binding・Wire等を組み立てに使用する。単なるHeadとStickだけに戻さない。TongsとTong PartはTFC Hot or Not導入時に限り同じ4金属へ登録する。TFC単独環境ではアイテムの登録自体を行わず、独自の保持操作・耐熱効果は追加しない。

工具tierの値は次のとおりとする。

| 金属 | Level | 耐久 | 採掘速度 | 攻撃補正 | Enchantability |
|---|---:|---:|---:|---:|---:|
| Invar | 3 | 2200 | 8 | 4.75 | 12 |
| Titanium | 5 | 3300 | 11 | 7 | 17 |
| Netherite | 6 | 6500 | 12 | 9 | 22 |
| Tungsten Steel | 7 | 8125 | 13 | 10.5 | 25 |

防具はTFC相当の4部位・騎乗用装備・中間防具を用いる。素材の設定値は次を満たす。

| 金属 | Boots防御 | Leggings防御 | Chest防御 | Helmet防御 | Toughness | Knockback抵抗 |
|---|---:|---:|---:|---:|---:|---:|
| Invar | 1 | 4 | 5 | 2 | 0 | 0 |
| Titanium | 2 | 5 | 6 | 2 | 2 | 0.05 |
| Netherite | 3 | 6 | 8 | 3 | 3 | 0.10 |
| Tungsten Steel | 4 | 7 | 9 | 4 | 4 | 0.15 |

防具の耐久・エンチャント適性は次の値とする。列の順序はBoots、Leggings、Chestplate、Helmetである。

| 金属 | Boots耐久 | Leggings耐久 | Chestplate耐久 | Helmet耐久 | 防具Enchantability |
|---|---:|---:|---:|---:|---:|
| Invar | 429 | 495 | 528 | 370 | 12 |
| Titanium | 650 | 750 | 800 | 550 | 17 |
| Netherite | 884 | 1020 | 1010 | 715 | 23 |
| Tungsten Steel | 1105 | 1275 | 1360 | 935 | 25 |

部品組立はTFCのCrafting、Anvil、Welding、Heatingを利用する。金属量や部品数を独自に増減させない。

#### Acceptance Criteria
##### AC-F007-001: 4金属の工具群
Preconditions: 各金属とTFC標準の工具カテゴリが存在する。  
Action: 工具、工具頭、武器、防具、補助装備を列挙し使用する。  
Expected Result: 4金属に所要の全カテゴリが実装され、表のtierと防具性能を満たす。

##### AC-F007-002: 部品組立
Preconditions: 未完成工具のHead、Handle、Crossguard、Pommel等がある。  
Action: 工具とSwordを組み立てる。  
Expected Result: 対応部品が必要で、完成品が指定の金属tierと修理材料を持つ。

##### AC-F007-003: 修理と熱
Preconditions: 損耗・加熱した工具と対応金属材料がある。  
Action: TFCの修理・熱処理を行う。  
Expected Result: 修理材料、Heat、TFCの収納・耐久・カテゴリ設定が矛盾しない。

##### AC-F007-004: TongsとTong Part
Preconditions: TFC単独環境と、TFC Hot or Notを追加した環境がある。  
Action: 両環境でTongsとTong Partのレジストリ登録を確認する。  
Expected Result: TFC単独環境では両アイテムが登録されない。導入時は対象4金属の両アイテムが登録され、独自の耐熱倍率・操作効果を付与しない。

### F-008: 独自金床
#### Description

Invar tier 3、Titanium tier 5、Netherite tier 6、Tungsten Steel tier 7のTFC金床を提供する。各金床は同金属のDouble Ingot 7個で製作し、加熱・溶融では1400 mBへ戻す。金床の配置、UI、作業tier、Anvil/Welding recipe、Block Entity、looting、tagをTFC金床と同じ規則に統合する。

#### Acceptance Criteria
##### AC-F008-001: 金床の製作
Preconditions: 対応金属のDouble Ingot 7個がある。  
Action: 金床を製造・設置する。  
Expected Result: 金属ごとに1種類の金床が完成し、TFCの金床UIとtier判定を使用する。

##### AC-F008-002: 金床の溶融とレシピ
Preconditions: 4種類の金床がある。  
Action: 加熱・溶融し、金床加工・溶接レシピを確認する。  
Expected Result: それぞれ対応金属1400 mBへ戻り、`tfc:anvils` 系tagから作業設備として認識される。

### F-009: 独自鉱石と品位・母岩
#### Description

独自鉱石は19種類とする。品位あり16種類はPoor、Normal、Richの3品位とSmall Oreを持つ。品位なし3種類は単一の鉱石アイテムを持つ。

| 品位あり16鉱石 | 品位なし3鉱石 |
|---|---|
| `native_platinum`、`native_naquadah`、`native_iridium`、`native_osmium`、`rutile`、`cobaltite`、`spodumene`、`bauxite`、`galena`、`uraninite`、`mithril_matrix`、`stibnite`、`wolframite`、`thorianite`、`magnesite`、`zircon` | `fluorite`、`quartz`、`carobbiite` |

全鉱石にTFCの各母岩に埋まったブロックとNetherrack・End Stone版を登録する。品位あり鉱石のアイテムIDは `tfcm:ore/<grade>_<ore>`、品位なしアイテムは `tfcm:ore/<ore>`、母岩ブロックは末尾に `/<rock>` を付ける。Small Oreは `tfcm:ore/small_<ore>` を使用する。Fluorite・Quartzには地表の小鉱石表現を用意し、単独の取得用BlockItemを作らない。

Nether鉱石は硬度3・爆発耐性10、End鉱石は硬度3・爆発耐性9とする。TFC母岩版は母岩のTFC硬度規則を用いる。

#### Acceptance Criteria
##### AC-F009-001: 鉱石の登録
Preconditions: 無効化設定が空の環境がある。  
Action: 全鉱石と品位を列挙する。  
Expected Result: 品位あり16種、品位なし3種が指定のIDで登録される。

##### AC-F009-002: 母岩と異世界のブロック
Preconditions: 各種TFC母岩、Netherrack、End Stoneがある。  
Action: それぞれの鉱石バリアントを配置・採掘する。  
Expected Result: 鉱石の種類と品位を正しく保持し、硬度・耐爆性能が指定どおりである。

##### AC-F009-003: 地表サンプル
Preconditions: Small Oreまたは小鉱石groundcoverがある。  
Action: ワールドに配置し、採掘・入手挙動を確認する。  
Expected Result: 品位あり鉱石のSmall Oreと、品位なし鉱石の小鉱石表示が区別される。

### F-010: 鉱石処理定義表
#### Description

鉱石の粉砕・洗浄は、金属鉱石30種類と非金属鉱物5種類の計35系列で構成する。表の「砂」は二次洗浄で排出するSand Pileの色、「主岩」「副岩」は一次洗浄で出るLoose Rock、「宝石」は一次洗浄の極低確率出力、「副産物B」は二次洗浄で得る別鉱石のDirty Dustを示す。Gemが「なし」の系列ではGem抽選を設定しない。

金属鉱石は一次粉砕後の `rocky_chunks`、`chunks`、`dirty_dust`、`powder` と最終の対応金属を持つ。非金属鉱物は同じ洗浄工程を持つが、最終出力を表のPowder等とし、金属流体の生成は個別に定義されている場合だけ行う。

| 対象鉱石 | 砂 | 主岩 | 副岩 | 宝石 | 副産物B | 最終の金属・鉱物 |
|---|---|---|---|---|---|---|
| Native Copper | Red | Basalt | Andesite | Opal | Hematite | Copper |
| Native Gold | Yellow | Quartzite | Granite | Quartz | Native Silver | Gold |
| Hematite | Red | Quartzite | Shale | なし | Sulfur | Cast Iron |
| Native Silver | White | Gneiss | Granite | Amethyst | Galena | Silver |
| Cassiterite | White | Granite | Gneiss | Topaz | Bismuthinite | Tin |
| Bismuthinite | Black | Granite | Schist | Pyrite | Galena | Bismuth |
| Garnierite | Green | Gabbro | Basalt | Opal | Limonite | Nickel |
| Malachite | Green | Limestone | Dolomite | Opal | Limonite | Copper |
| Magnetite | Black | Gabbro | Basalt | なし | Hematite | Cast Iron |
| Limonite | Brown | Claystone | Shale | Opal | Sulfur | Cast Iron |
| Sphalerite | Brown | Dolomite | Limestone | Amethyst | Galena | Zinc |
| Tetrahedrite | Black | Schist | Dolomite | Pyrite | Sphalerite | Copper |
| Galena | Black | Limestone | Dolomite | Amethyst | Sphalerite | Lead |
| Magnesite | White | Dolomite | Gabbro | Opal | Limonite | Magnesium |
| Bauxite | Red | Claystone | Shale | Sapphire | Hematite | Aluminum |
| Uraninite | Black | Granite | Gneiss | なし | Galena | Uranium |
| Chromite | Black | Gabbro | Basalt | Ruby | Cobaltite | Chromium |
| Rutile | Red | Gneiss | Schist | Sapphire | Hematite | Titanium |
| Zircon | Brown | Granite | Gneiss | Topaz | Cassiterite | Zirconium |
| Cobaltite | Black | Gabbro | Schist | Pyrite | Sulfur | Cobalt |
| Stibnite | Black | Shale | Slate | Amethyst | Galena | Antimony |
| Native Platinum | Black | Gabbro | Basalt | なし | Chromite | Platinum |
| Wolframite | Black | Granite | Gneiss | Quartz | Cassiterite | Tungsten |
| Thorianite | Black | Granite | Gneiss | なし | Galena | Thorium |
| Native Iridium | Black | Gabbro | Basalt | なし | Native Platinum | Iridium |
| Native Osmium | Black | Gabbro | Basalt | なし | Native Platinum | Osmium |
| Spodumene | White | Granite | Gneiss | Topaz | Cassiterite | Lithium |
| Carobbiite | White | Granite | Gneiss | Emerald | Native Platinum | Beryllium |
| **Native Naquadah** | **Black** | **Gabbro** | **Basalt** | **なし** | **Native Osmium** | **Naquadah** |
| **Mithril Matrix** | **White** | **Gneiss** | **Granite** | **Amethyst** | **Native Platinum** | **Mithril** |
| Cinnabar | Red | Quartzite | Schist | Ruby | Sulfur | Redstone Dust |
| Cryolite | White | Granite | Gneiss | Topaz | Spodumene | Redstone Dust |
| Graphite | Black | Schist | Phyllite | Diamond | Hematite | Graphite Powder |
| Sulfur | Yellow | Tuff | Basalt | Pyrite | Hematite | Sulfur Powder |
| Borax | White | Granite | Gneiss | Topaz | Graphite | Borax Powder |

Native NaquadahとMithril Matrixの行は**本プロジェクトで確定した固有仕様**とする。これらも他の品位あり鉱石と同じ4段階（Small、Poor、Normal、Rich）のハンマー粉砕規則と二段階のSluice処理を使用する。宝石欄の「なし」はゼロ確率ではなく、抽選枠そのものを作らない意味である。

本表は**鉱石の加工定義**であり、鉱脈の生成ディメンションや生成高度を指定するものではない。採掘時のワールド配置はF-024の生成設定で決まる。

#### Acceptance Criteria
##### AC-F010-001: 35系列の処理
Preconditions: TFCMとTFCが動作し、FirmaLifeを必要とするChromite系列についてはFirmaLifeも導入されている。  
Action: 鉱石処理定義を列挙し、入力と最終出力を表と照合する。  
Expected Result: 金属鉱石30系列、非金属鉱物5系列が存在し、表にない系列を生成しない。

##### AC-F010-002: NaquadahとMithril Matrix
Preconditions: Native Naquadah、Mithril Matrixの鉱石と加工設備がある。  
Action: 両系列をそれぞれ一次・二次洗浄する。  
Expected Result: NaquadahはBlack/Gabbro/Basalt/Gemなし/Native Osmium、Mithril MatrixはWhite/Gneiss/Granite/Amethyst/Native Platinumの設定を使用し、粉末は対応金属へつながる。

##### AC-F010-003: 定義に基づく出力
Preconditions: Gemのある鉱石とGemのない鉱石がある。  
Action: 同じ処理を行いloot tableを調べる。  
Expected Result: 主岩・副岩・砂・宝石・副産物Bが表に一致し、未指定の宝石や副産物Aを追加しない。

### F-011: Step 1 — 鉱石のHammer粉砕
#### Description

`#c:tools/hammer` を材料と共に使用するTFCのAdvanced Shapeless Craftingを実装する。Hammerは消費せず、TFCのCrafting Remainderで損耗させる。入力鉱石1個当たりのRocky Chunk出力は以下のとおりとする。

| 原料の区分 | Rocky Chunk |
|---|---:|
| Small Ore | 1 |
| Poor Ore | 3 |
| Normal Ore | 5 |
| Rich Ore | 7 |
| 品位なし金属鉱石 | 5 |
| 品位なし非金属鉱物 | 7 |

#### Acceptance Criteria
##### AC-F011-001: 品位別粉砕
Preconditions: 同じ鉱石のSmall、Poor、Normal、Richがある。  
Action: TFC Hammerで各1個を加工する。  
Expected Result: Rocky Chunkが順に1、3、5、7個得られ、工具の耐久がTFCの規則で減る。

##### AC-F011-002: 品位なし鉱石と非金属鉱物
Preconditions: Carobbiiteと非金属鉱物がある。  
Action: Hammer粉砕する。  
Expected Result: Carobbiiteから5個、非金属鉱物から7個のRocky Chunkを得る。

### F-012: Step 2 — Sluice一次洗浄
#### Description

Rocky Chunk 1個のTFC Sluice処理に対し、それぞれ独立したloot poolで以下を抽選する。一次洗浄にはTFCのSluice機構のみを使用する。

| 出力 | 個数 | 確率 |
|---|---:|---:|
| 対象鉱石のChunk | 1 | 100% |
| F-010の主岩に対応するLoose Rock | 1 | 30% |
| F-010の副岩に対応するLoose Rock | 1 | 15% |
| F-010のGem | 1 | 0.02%（存在する場合のみ） |

#### Acceptance Criteria
##### AC-F012-001: 主産物
Preconditions: Rocky ChunkをSluiceへ投入する。  
Action: 一次洗浄を完了する。  
Expected Result: 対応するChunkを1個必ず得る。

##### AC-F012-002: 副産物の確率
Preconditions: F-010で岩種とGemが定義された鉱石がある。  
Action: loot tableの確率値と独立poolを検査する。  
Expected Result: 主岩30%、副岩15%、Gem0.02%であり、Gemなしの系列ではGem poolが存在しない。

### F-013: Step 3 — Quern粉砕
#### Description

TFC QuernでChunkを粉砕し、同系列のDirty Dustを1個確定で得る。この段階では別鉱石・砂・宝石などの副産物を発生させない。

#### Acceptance Criteria
##### AC-F013-001: Quern粉砕
Preconditions: 対象鉱石のChunkとTFC Quernがある。  
Action: Chunk 1個を粉砕する。  
Expected Result: 同系列のDirty Dust 1個が生成され、他の確率出力は発生しない。

### F-014: Step 4 — Sluice二次洗浄とDirty Pile廃止
#### Description

Dirty Dust 1個をTFC Sluiceで洗浄する。処理結果は以下の独立したloot poolによる。F-010の副産物Bは、**別系列のDirty Dust**として直接得る。

| 出力 | 個数 | 確率 |
|---|---:|---:|
| 対象系列のPowder | 1 | 100% |
| F-010の色のSand Pile | 1 | 15% |
| 入力と同系列のDirty Dust | 1 | 5% |
| F-010の副産物BのDirty Dust | 1 | 1% |

Dirty Pileは一切登録しない。Dirty Pileを使用する加工、圧縮、展開、loot、item tag、translation、モデルを作成しない。名称が似ていても**Sand Pileは維持**する。F-016の素材採取でDirty Pileに相当する出力がある場合も、指定された確率を変更せずDirty Dustへ直接置換する。

#### Acceptance Criteria
##### AC-F014-001: 二次洗浄
Preconditions: 対象鉱石のDirty Dustがある。  
Action: Sluiceで処理する。  
Expected Result: 対応Powderは100%生成され、Sand Pile15%、同種Dirty Dust5%、副産物B Dirty Dust1%の独立抽選が行われる。

##### AC-F014-002: Dirty Pileの不在
Preconditions: 全系列の登録とデータ生成が完了している。  
Action: Item Registry、loot、recipe、item tag、creative tab、翻訳、resourceの参照先を検索する。  
Expected Result: Dirty Pileアイテム・出力・入力が存在せず、Sand Pileは正常に使用される。

##### AC-F014-003: 主産物と副産物の区別
Preconditions: Native NaquadahとMithril MatrixのDirty Dustがある。  
Action: 両系列の二次洗浄を検証する。  
Expected Result: それぞれNaquadahとMithrilのPowderを確定で得るとともに、Native OsmiumとNative PlatinumのDirty Dustが各1%で副産物になる。

### F-015: Powder・Pellet・Briquetと最終加熱
#### Description

鉱石洗浄で得られたPowderを、金属回収のためTFC Heatingへ渡す。対象金属の溶融流体を5 mB/個生成する。粉末の圧縮加工としてPellet（20 mB相当）とBriquet（80 mB相当）を登録する。Powder 4個 → Pellet 1個、Pellet 4個 → Briquet 1個として加工し、逆向きにはBriquet 1個 → Pellet 4個、Pellet 1個 → Powder 4個へ解体する。BriquetはPowder 16個相当である。Heatingには鉱石系列に対応する金属の溶融・処理温度を使用する。

非金属鉱物ではF-010の最終粉末アイテムを生成する。Borax系列はBorax Powderを得た後、既定のBoron回収処理へ接続する。金属以外の粉末から根拠のない溶融流体を追加しない。

Poor・Normal・Richの原鉱石を直接溶融するレシピは設けず、Step 1〜4を経由してから加熱する。Small Oreの直接溶融は維持する。品位なし鉱石は本書の粉砕・洗浄経路に従う。

TFCで製造が成立する金属について、外部の機械や化学工程を要求せずに最終金属を入手できるようにする。金属ごとの固有温度はF-002に従い、鉱石の還元温度が異なる場合はTFCの対応レシピ規則に準じた個別のTFC Heatingデータで明示し、独自の化学工程は追加しない。採取・精製・溶融の工程と、F-005の金属Ingot由来Dustを混同しない。

#### Acceptance Criteria
##### AC-F015-001: Powder加熱
Preconditions: 金属鉱石のPowder、対応TFC加熱設備がある。  
Action: Powder 1個を加熱する。  
Expected Result: 対象金属の溶融流体5 mBを得る。Native NaquadahはNaquadah、Mithril MatrixはMithrilを生成する。

##### AC-F015-002: 圧縮と解体
Preconditions: 同系列のPowderが16個ある。  
Action: PelletとBriquetを製造・解体・加熱する。  
Expected Result: 1 Pelletが20 mB、1 Briquetが80 mBに相当し、圧縮と解体が材料量で一致する。

##### AC-F015-003: 非金属鉱物
Preconditions: Cinnabar、Cryolite、Graphite、Sulfur、BoraxのPowderがある。  
Action: 各製品のレシピと最終出力を確認する。  
Expected Result: F-010の定義された粉末を得て、金属以外から未定義の流体を作成しない。Boraxの金属回収系列は別途保持する。

##### AC-F015-004: 原鉱石の直接溶融制御
Preconditions: 同一金属のSmall、Poor、Normal、Rich鉱石がある。  
Action: それぞれのTFC Heatingレシピを検査する。  
Expected Result: Small Oreの直接溶融は存在し、Poor・Normal・Richの直接溶融は存在せず、洗浄後のPowderから回収できる。

### F-016: Pan・Sluiceによる地表資源採取
#### Description

TFCのPan・Sluiceによる堆積物、岩石Gravel、Sand、Rock Powderのlootを追加する。TFC本体の仕組みを使用し、独自の選別機械を追加しない。

**Deposit**：Native Copper、Native Gold、Native Silver、Cassiteriteの4種について、下表21岩石のDepositを対象とする。Deposit 1個につきSmall Oreを50%と10%の独立抽選、同岩種のLoose Rockを50%、下表の第一素材を25%、第二素材を5.33%で抽選する。

**Gravel**：21種類のTFC Rock Gravelを対象とし、Native Copper Powderを50%・25%の独立抽選、同岩種のLoose Rockを50%、Flintを15%、Boneを10%で抽選する。Gold Nuggetの生成は行わない。

**岩種ごとのDeposit副産物**

| 母岩 | 第一素材 | 第二素材 |
|---|---|---|
| Andesite | Pumice | Pyrite |
| Basalt | Basalt Loose Rock | Pyrite |
| Chalk | Chalk Loose Rock | Sapphire |
| Chert | Chert Loose Rock | Ruby |
| Claystone | Claystone Loose Rock | Amethyst |
| Conglomerate | Conglomerate Loose Rock | Lignite |
| Dacite | Pumice | Pyrite |
| Diorite | Diorite Loose Rock | Emerald |
| Dolomite | Dolomite Loose Rock | Amethyst |
| Gabbro | Gabbro Loose Rock | Diamond |
| Gneiss | Gneiss Loose Rock | Gypsum |
| Granite | Granite Loose Rock | Topaz |
| Limestone | Limestone Loose Rock | Lapis Lazuli |
| Marble | Marble Loose Rock | Lapis Lazuli |
| Phyllite | Phyllite Loose Rock | Pyrite |
| Quartzite | Quartzite Loose Rock | Opal |
| Rhyolite | Pumice | Pyrite |
| Schist | Schist Loose Rock | Pyrite |
| Shale | Shale Loose Rock | Borax |
| Slate | Slate Loose Rock | Pyrite |
| Tuff | Tuff Loose Rock | Pyrite |

**Sand**：Black、Brown、Green、Pink、Red、White、Yellowの7色について、次表の出力をそれぞれ独立に抽選する。Sand PileはTFCM自身のアイテムとして登録する。

| 入力Sand | 独立確率出力 |
|---|---|
| Black | White Sand Pile 100%、Black Sand Pile 30%、Galena Dirty Dust 5%、Charcoal Powder 5%、Bone 3%、Sulfur Powder 1%、Sapphire 0.1% |
| Brown | White Sand Pile 100%、Brown Sand Pile 30%、Gypsum 5%、Sylvite 5%、Bone 3%、Topaz 0.1% |
| Green | White Sand Pile 100%、Green Sand Pile 30%、Malachite Powder 5%、Bismuthinite Powder 5%、Garnierite Powder 2%、Bone 3%、Emerald 0.1% |
| Pink | White Sand Pile 100%、Pink Sand Pile 30%、Redstone Dust 4%、Bauxite Powder 4%、Bone 3%、Ruby 0.1% |
| Red | White Sand Pile 100%、Red Sand Pile 30%、Redstone Dust 4%、Hematite Powder 4%、Bone 3%、Ruby 0.1% |
| White | White Sand Pile 100%、Bone Meal 15%、Sphalerite Powder 1.5%、Native Silver Powder 1%、Quartz 1%、Bone 2.5%、Opal 0.05% |
| Yellow | White Sand Pile 100%、Yellow Sand Pile 30%、Limonite Powder 8%、Native Gold Powder 3%、Bone 3%、Pyrite 0.1% |

**Rock Powder**：Rock PowderをPan・Sluiceで処理すると、Black Sand Pile 15%、Flint 5%、Native Copper Dirty Dust 3%、Sphalerite Dirty Dust 1%、Native Gold Dirty Dust 1%、Native Silver Dirty Dust 1%、Magnetite Dirty Dust 0.5%を独立抽選する。旧Dirty Pile経由の出力はすべてDirty Dustに置き換える。Rock Powderそのものの製作・採取レシピは新規に追加しない。

#### Acceptance Criteria
##### AC-F016-001: Deposit
Preconditions: 4種×21岩種のDepositがある。  
Action: 各DepositをPan・Sluiceで処理しloot定義を検査する。  
Expected Result: Small Ore 50%と10%、Loose Rock 50%、岩種固有素材25%と5.33%の独立抽選が一致する。

##### AC-F016-002: Gravel
Preconditions: TFCの21岩種のGravelがある。  
Action: Pan・Sluiceで処理する。  
Expected Result: Copper Powderが50%・25%、Loose Rock 50%、Flint 15%、Bone 10%で抽選され、Gold Nuggetを出さない。

##### AC-F016-003: SandとRock Powder
Preconditions: 7色のSandとRock Powderがある。  
Action: Pan・Sluiceのlootを検査する。  
Expected Result: 表の個別確率が一致し、必要なSand Pileが登録され、Dirty Pileが出力されない。

### F-017: Mining Hammer — 3×3範囲採掘
#### Description

Mining Hammerを、Pickaxe対象ブロックに対する3×3範囲採掘工具として登録する。起点ブロックを採掘面の中央 `(0, 0)` とし、垂直面では上下左右1、水平面では前後左右1の領域を対象とする。起点は通常の破壊処理が担い、追加破壊は最大8ブロックとする。対象金属はTFCでPickaxeを製造できる金属と、TFCMで工具が有効なInvar、Titanium、Netherite、Tungsten Steelとする。完成工具とTool Headをそれぞれ登録する。

Mining Hammerの採掘速度は素材tierの40%とする。その他の破壊挙動はF-020で規定する。正式名称・表示名は **Mining Hammer** を使用する。

#### Acceptance Criteria
##### AC-F017-001: 3×3採掘面
Preconditions: Mining Hammerを装備し、採掘可能な壁・床・天井が存在する。  
Action: 各面の起点ブロックを採掘する。  
Expected Result: 起点を中心とする3×3だけが対象となり、追加破壊は最大8ブロックである。

##### AC-F017-002: 適正toolと素材tier
Preconditions: Pickaxe対象、Shovel対象、採掘tier不足のブロックが混在する。  
Action: Mining Hammerで起点を採掘する。  
Expected Result: Pickaxe対象かつ素材tierで採掘できるものだけを処理し、適正外のブロックを破壊しない。

##### AC-F017-003: 工具一式
Preconditions: TFCのPickaxe対応金属およびTFCMの4対象金属がある。  
Action: Mining HammerとTool Headを確認する。  
Expected Result: 対象金属で登録され、耐久・修理材料は対象tierに従う。

### F-018: Quarry Hammer — 5×5範囲採掘
#### Description

Quarry HammerをMining Hammerと独立した工具として登録し、Pickaxe対象の5×5範囲採掘を行う。採掘速度・tool tier・追加破壊の耐久消費・保護event・lootはMining Hammerと共通とし、範囲計算だけを変更する。起点を含めた25位置のうち、追加破壊は最大24ブロックとする。対象金属はMining Hammerと同じとする。

採掘面にはワールド座標から変換したローカル2次元座標 `(u, v)` を使用し、`-2 ≤ u ≤ 2`、`-2 ≤ v ≤ 2` を処理領域とする。垂直面では `v` の正方向をワールドの上方向とする。

**垂直面**：プレイヤーが視線を合わせて破壊した起点は `(0, -1)`。したがって起点の左右2ブロック、上3ブロック、下1ブロックを含む5×5である。

```text
 v=+2    . . . . .
 v=+1    . . . . .
 v= 0    . . . . .
 v=-1    . . X . .    X = 起点
 v=-2    . . . . .
          -2 -1 0 +1 +2  (u)
```

**水平面**：床・天井では起点を `(0, 0)` とする。前後左右2ブロックの中央揃え5×5である。

```text
 . . . . .
 . . . . .
 . . X . .
 . . . . .
 . . . . .
```

`u` は面内の左右、`v` は面内の上下または前後を表す。プレイヤーの向きと命中面の法線からローカル座標をワールド座標へ写す。方向反転によって「上3・下1」が逆になってはならない。

Quarry Hammerには専用のHeadとHead Moldを登録する。Headの金属使用量、鋳型・金床加工、工具組立レシピはMining Hammerと同一の規則・数量を使用し、完成品だけQuarry Hammerへ置き換える。独自の耐久・攻撃力・採掘tierは設けない。

#### Acceptance Criteria
##### AC-F018-001: 垂直5×5
Preconditions: 5×5以上の平坦な壁とQuarry Hammerがある。  
Action: 壁面の任意ブロックを起点に採掘する。  
Expected Result: 起点 `(0,-1)` を含む `u=-2..2`、`v=-2..2` の25位置を判定し、起点の上3・下1・左右2、追加最大24ブロックとなる。

##### AC-F018-002: 水平5×5
Preconditions: 平坦な床と天井がある。  
Action: それぞれをQuarry Hammerで採掘する。  
Expected Result: 起点 `(0,0)` を中心とする5×5を判定し、追加最大24ブロックとなる。

##### AC-F018-003: 方向と保護
Preconditions: 東西南北の壁、保護対象ブロック、採掘不適正ブロックがある。  
Action: 各方位でQuarry Hammerを使用する。  
Expected Result: 全方位で上3・下1の配置が一定であり、F-020の個別保護・ドロップ・耐久処理を適用する。

##### AC-F018-004: 工具tier
Preconditions: 同じ金属のMining HammerとQuarry Hammerがある。  
Action: 耐久、採掘速度、修理材料、採掘tierを比較する。  
Expected Result: 範囲以外に独自の倍率・追加能力が設定されていない。

### F-019: Excavator — 3×3範囲掘削
#### Description

ExcavatorはShovelで適正に採掘できるブロックだけを対象とする3×3範囲工具である。採掘面はMining Hammerと同じく起点を中心とし、追加破壊は最大8ブロックとする。TFCでShovelを製造できる金属およびTFCMの4工具対応金属に、完成工具とHeadを登録する。採掘速度は素材tierの40%とする。

#### Acceptance Criteria
##### AC-F019-001: 掘削対象
Preconditions: Shovel適正ブロックとPickaxe適正ブロックが混在する。  
Action: Excavatorで掘削する。  
Expected Result: Shovel適正ブロックだけが起点中央の3×3範囲で処理され、追加破壊は最大8である。

##### AC-F019-002: TFC工具体系
Preconditions: Shovel対応金属と必要な工具部品がある。  
Action: ExcavatorとHeadの登録、修理、熱・サイズ設定を確認する。  
Expected Result: 対象金属のtier・修理材料を用い、専用の新しいtierを導入しない。

### F-020: 範囲採掘の共通破壊規則
#### Description

Mining Hammer、Quarry Hammer、Excavatorは以下の破壊処理を共用する。

1. 通常のMinecraft処理で起点が破壊可能であり、対応する正しい工具である場合だけ追加範囲処理を開始する。
2. 起点以外の候補を1ブロックずつ検査する。候補ごとにPickaxe/Shovelの適正、採掘tier、保護を判定する。
3. 追加破壊ごとにNeoForgeのBlock Break Eventを通知し、cancelされた場合はブロックも耐久もドロップも変更しない。
4. Survivalではブロックの通常lootを、Block Entityを含む正しいloot contextで生成する。追加破壊1ブロックにつき工具耐久を1消費し、工具が破損したら処理を中止する。
5. Creativeでは追加dropと耐久消費を行わない。
6. `LAYERS` propertyを持つブロックは1回で1層を減らす。最後の層でブロックを除去する。
7. 追加破壊から範囲採掘を再帰起動しない。ワールドの変更・アイテム付与はサーバー側で確定する。

#### Acceptance Criteria
##### AC-F020-001: イベントと保護
Preconditions: 範囲内にBreak Eventがcancelされるブロックがある。  
Action: 3種類の工具で採掘する。  
Expected Result: cancelされた候補は不変であり、それ以外の有効候補のみ破壊される。

##### AC-F020-002: LootとBlock Entity
Preconditions: 追加破壊範囲内にLoot TableまたはBlock Entityを持つブロックがある。  
Action: Survivalで採掘する。  
Expected Result: 正規loot contextに従うドロップを生成し、複製・消失を起こさない。

##### AC-F020-003: 耐久とCreative
Preconditions: 耐久が少ない工具およびCreativeとSurvivalの環境がある。  
Action: 複数ブロックを範囲採掘する。  
Expected Result: Survivalでは追加破壊1ブロックにつき耐久1を消費し、破損時点で停止する。Creativeでは追加drop・耐久消費がない。

##### AC-F020-004: 多層ブロックと再帰抑止
Preconditions: Layers付きブロックと追加採掘対象がある。  
Action: 範囲採掘する。  
Expected Result: Layersは1層ずつ減り、追加破壊の連鎖で範囲が増殖しない。

### F-021: 採掘工具Head・鋳型・組立
#### Description

Mining HammerおよびExcavatorに対応する完成工具、Tool Head、未焼成・焼成済みの陶製Head Moldを用意する。MoldはTFCのKnapping、Heating、Castingに統合する。Headは対象金属のTFC加工・工具組立に使用し、tool heat、size、rack、tool tagを定義する。

Quarry Hammerにも専用のTool Headと未焼成・焼成済みHead Moldを登録する。Knapping、Heating、Casting、Head加工、工具組立はMining Hammerと同じ材料数量・加工規則を適用する。

#### Acceptance Criteria
##### AC-F021-001: Mining Hammer・Excavatorの鋳型
Preconditions: Knapping素材、未焼成鋳型、焼成設備、対象金属がある。  
Action: Moldを作成・焼成・鋳造する。  
Expected Result: Head MoldとTool HeadがTFC方式で作成され、工具組立に使用される。

##### AC-F021-002: 工具としての属性
Preconditions: 完成したMining Hammer、Quarry Hammer、Excavatorを取得した。  
Action: サイズ、熱、工具ラック、damage category、修理材料を確認する。  
Expected Result: 対象金属に適合するTFC属性を持ち、未登録の中間素材を要求しない。

##### AC-F021-003: Quarry Hammerの製作
Preconditions: Mining HammerのHead・Mold・組立に必要な素材と加工設備がある。  
Action: 同じ加工規則と材料数量でQuarry HammerのHead・Moldを製造し、完成工具を組み立てる。  
Expected Result: Quarry Hammerが製作でき、Mining Hammerとの違いはF-018の採掘範囲だけである。

### F-022: Gem・Quartz・Fluorite
#### Description

Raw Quartz `tfcm:ore/quartz`、Cut Quartz `tfcm:gem/cut_quartz`、Certus Quartz `tfcm:gem/certus_quartz` を登録する。QuartzとFluoriteはQuernで各4個の対応Powderへ粉砕する。Quartzの研磨はTFCの既存宝石加工レシピに準じ、同じ材料・製作手順・工具損耗規則を用いる。特定の別Modを必要とする道具を要求しない。Cut Quartz・Certus Quartzをcommon gem tagで扱う。Fluorite・QuartzのPowderはTFCのgem powderおよびbowl powderとして登録する。

#### Acceptance Criteria
##### AC-F022-001: Gemの登録と研磨
Preconditions: Raw QuartzとTFCのGem加工設備・材料がある。  
Action: Quartzを加工する。  
Expected Result: Cut Quartzを入手でき、他の必須Modを増やさない。Certus Quartzも別アイテムとして登録される。

##### AC-F022-002: Quern粉砕
Preconditions: QuartzとFluoriteが各1個ある。  
Action: TFC Quernで粉砕する。  
Expected Result: Quartz PowderまたはFluorite Powderがそれぞれ4個生成され、TFC powder tagが適用される。

### F-023: Quartz・Amethyst・Certus Quartz晶洞
#### Description

Quartz、Amethyst、Certus Quartzの3系統について、鉱物ブロック、Budding Block、Small・Medium・Large Bud、Clusterを実装する。各晶洞の外殻はTFC Hardened Basalt、中間層はRaw Quartzite、内層の候補weightはBudding Block:鉱物Block:Raw Quartzite = 2:5:1とする。標準生成高度はY -48から32、試行頻度は平均300チャンク当たり1回とする。各系統のbud成長・cluster化はTFC環境で機能させる。

| 系統 | 成熟Clusterのドロップ |
|---|---|
| Quartz | Cut Quartz ×2 |
| Amethyst | TFC Amethyst Gem ×2 |
| Certus Quartz | Certus Quartz ×2 |

#### Acceptance Criteria
##### AC-F023-001: 3系統の全ブロック
Preconditions: 3晶洞系統を有効にする。  
Action: Block、Budding Block、3段階Bud、Clusterを列挙し設置する。  
Expected Result: 各系統の必要なBlock・item・モデル・dropが存在する。

##### AC-F023-002: 成長と生成
Preconditions: 新規ワールドを用意する。  
Action: 晶洞の配置とBudding Blockからの成長を確認する。  
Expected Result: 外殻・中間層・内層比率、Y範囲、頻度、段階成長、Clusterドロップが指定どおりである。

### F-024: YAMLによる鉱脈・晶洞生成
#### Description

通常のTFCワールド生成に対して、3つのファイルから鉱脈・晶洞定義を読込・置換・追加する。

```text
config/tfcm/overworld.yaml
config/tfcm/nether.yaml
config/tfcm/end.yaml
```

`enableCustomVeinGeneration = true` のとき、OverworldのTFC通常鉱脈を有効なYAML定義に置き換える。砂利・岩脈など鉱脈以外の既存worldgen要素は維持する。Nether・Endには有効な設定で明示した鉱脈・晶洞だけを追加する。設定が空のNether・Endに鉱脈を自動生成しない。Overworldで有効な鉱脈がゼロなら、TFCの標準鉱脈にfallbackする。カスタム生成有効時は標準のTFCM晶洞を除外し、`geodes` で定義された晶洞だけを生成する。

正規YAMLは次の要素を扱う。

- `veins`：鉱脈定義の配列。各項目に一意のIDを付ける。
- `blocks`：1個以上の `{block, weight}` の配列。`weight` の既定値は1、0超の有限値とする。
- `{rock}`、`{tier}`：母岩名・品位を置換するテンプレート。`tier` は品位から正の整数weightへの対応表とする。
- `rocks`：置換対象の岩石またはブロック。Overworldでは必須、NetherではNetherrack、EndではEnd Stoneを既定にする。
- `type`：`tfc:cluster_vein`、`tfc:disc_vein`、`tfc:kaolin_disc_vein`、`tfc:pipe_vein`。
- `ymin`、`ymax`、`rarity`、`density`、`size`、`height`、`radius`、`min_skew`、`max_skew`、`min_slant`、`max_slant`、`sign`、`random_name`、`seed`、`project`、`project_offset`、`near_lava`。
- `indicator`：`block`、`rarity`、`depth`、`underground_rarity`、`underground_count`。
- `geodes`：`type`、`ymin`、`ymax`、`rarity`、`outer`、`middle`、`inner`、`filling`、`inner_placements`。後3項目は `{block,weight}` 配列とする。

参照先ブロック未登録、依存コンテンツ未導入、数値不正、type不正、ID重複などの入力は警告を出して該当エントリだけを無効とする。重複IDは先頭を採用する。古い設定パスや旧構文のための特別なfallbackは設けない。

#### Acceptance Criteria
##### AC-F024-001: 3ディメンションの読込み
Preconditions: 3つのYAMLに異なる有効定義を配置する。  
Action: 新規ワールドを生成し、各ディメンションを確認する。  
Expected Result: 各設定に指定された鉱脈・晶洞だけを対応ディメンションへ追加する。

##### AC-F024-002: 品位とweight
Preconditions: `blocks`、`tier`、`rocks`、indicatorのある鉱脈を定義する。  
Action: YAMLを読み込み生成結果を確認する。  
Expected Result: 置換・相対weight・品位weight・母岩条件・地表/地下indicatorが有効になる。

##### AC-F024-003: fallbackと無効入力
Preconditions: 空の設定と無効定義を含む設定を用意する。  
Action: ワールドを読み込む。  
Expected Result: 無効エントリを警告で除外し、Overworldでは規定のfallback、Nether・Endでは空定義時に非生成となる。

### F-025: TFC鉱石・FirmaLife Chromiteの異世界ブロック
#### Description

TFC既存鉱石にもNetherrack・End Stone版を追加する。対象は以下の12系列のPoor・Normal・Rich、および17種の品位なし鉱石・鉱物である。

- 品位あり12系列：Bismuthinite、Cassiterite、Garnierite、Hematite、Limonite、Magnetite、Malachite、Native Copper、Native Gold、Native Silver、Sphalerite、Tetrahedrite。
- 品位なし17種類：Amethyst、Borax、Cinnabar、Cryolite、Diamond、Emerald、Graphite、Gypsum、Lapis Lazuli、Opal、Pyrite、Ruby、Saltpeter、Sapphire、Sulfur、Sylvite、Topaz。

FirmaLife導入時にはPoor・Normal・Rich ChromiteのNetherrack・End Stone版を追加する。各ブロックは元の鉱石を採掘したときと同等の鉱石アイテムを落とし、TFCのProspecting Pickと鉱石採掘tool判定に対応する。追加ブロックを登録しただけで、Nether・Endへ自動生成するものではない。

#### Acceptance Criteria
##### AC-F025-001: TFC互換鉱石
Preconditions: TFCがある。  
Action: 12×3品位と17種の鉱石についてNetherrack・End Stone版を列挙する。  
Expected Result: 53種類のTFC鉱石名にそれぞれ2バリアントが存在する。

##### AC-F025-002: Chromite
Preconditions: FirmaLife導入時と未導入時の環境がある。  
Action: Chromiteの3品位×2バリアントを確認する。  
Expected Result: 導入時は6ブロックと適正dropが存在し、未導入時は無効参照でクラッシュしない。

##### AC-F025-003: 探鉱と採掘
Preconditions: 追加鉱石ブロックを設置する。  
Action: Prospector Pickで検知し、正しい工具で採掘する。  
Expected Result: 品位・母岩を区別した正しいlootを得て、探鉱表示で鉱石名を認識する。

### F-026: 金属・鉱石の個別無効化
#### Description

Common Configに `disabledMetals` と `disabledOres` を設ける。設定ファイル `config/tfcm-common.toml` の該当節には、指定可能な金属・鉱石のID一覧をコメントとして自動出力する。金属39種と対応する鉱石名を漏れなく列挙し、実際に受け付ける指定名と一致させる。コメントは無効化の指定として解釈せず、既存の設定値は上書きしない。IDの小文字名で対象を指定する。未知名は無視し、変更反映にはゲーム再起動が必要である。金属を無効化した場合は基本形状、拡張形状、Plate/Nugget/Dust、金属工具・防具・金床・採掘工具など同金属由来のレジストリ登録を抑制する。鉱石を無効化した場合は鉱石アイテム・ブロック、Small Ore、加工中間体、loot、recipe、カスタム生成候補を対象として抑制する。

不正な参照が残らないよう、registry側だけでなくレシピ・タグ・熱定義・戦利品・ワールド生成の読込み条件にも同一の有効判定を適用する。意図せず別金属・別鉱石を連動無効化しない。

#### Acceptance Criteria
##### AC-F026-001: 金属無効化
Preconditions: `disabledMetals=["lead"]` を設定する。  
Action: 再起動してレジストリ、レシピ、タグを確認する。  
Expected Result: Lead由来の全形状・工具が無効となり、他金属は維持され、欠損参照でクラッシュしない。

##### AC-F026-002: 鉱石無効化
Preconditions: `disabledOres=["galena"]` を設定する。  
Action: 再起動して中間体、loot、worldgenを確認する。  
Expected Result: Galena由来コンテンツと対応加工が無効になり、他の鉱石系列を不必要に無効化しない。

##### AC-F026-003: 不正設定
Preconditions: 不明な金属・鉱石名を設定する。  
Action: ゲームを起動する。  
Expected Result: 未知名を無視し、正常な内容を維持する。

##### AC-F026-004: 設定コメントの一覧出力
Preconditions: Common Configを新規生成する。  
Action: `disabledMetals` と `disabledOres` の設定箇所を確認する。  
Expected Result: 指定可能な金属・鉱石名の完全な一覧がコメントとして存在し、設定値や無効化状態を変更しない。

### F-027: TFC属性・Heat・Tagの整合
#### Description

すべての登録アイテムとブロックについて、使用するTFC属性を揃える。金属形状は金属別のcommon ingot/sheet/rod/foil/gear/plate/nugget/dust等のタグ、storage block tag、molten fluid tag、metal heat、size/weightを定義する。追加工具・工具頭・武器・防具・鋳型についてはTFCの工具カテゴリ、tool rack、適正tool、repair ingredient、Heat・item sizeを設定する。鉱石はpickaxe適正tag、一般鉱石tag、Prospector Pickの翻訳キー `.prospected` を備える。Small Oreとgroundcoverは必要なloot・配置特性を持つ。

#### Acceptance Criteria
##### AC-F027-001: レジストリ・データ参照の完全性
Preconditions: 全金属・鉱石が有効な環境がある。  
Action: 全item/block/fluid IDから参照するJSON、tag、Heat、Size、lootを検査する。  
Expected Result: 必須resourceの不足、ID参照失敗、同じ形状の重複定義がない。

##### AC-F027-002: TFC装備属性
Preconditions: 各金属工具と金属形状がある。  
Action: 熱処理、収納、修理、工具ラックへの配置を試す。  
Expected Result: TFCの属性判定が正常に機能し、Heat・Size・Weightと道具カテゴリが整合する。

### F-028: アセットと表示
#### Description

**現行公開版のアセットを本プロジェクトの正式な素材として再利用する。** アイテムとブロックのtexture、model、blockstate、animation metadata、翻訳、rendererに必要なassetを使用許諾を確認して取り込む。アセットを取得した配布版、ファイルパス、ライセンスを記録する。既存コードの内容を知らなくても、assetのファイル形式と見た目から参照関係を構築する。

金属ごとの色は既存アセットの金属色を利用し、TFCの道具・形状では木柄や革部品等の非金属部分を保つ。Neutroniumの既存アニメーション表現を維持する。Plate、Nugget、Dust、Mining Hammer、Quarry Hammerに不足するアセットは、使用可能な既存素材の再構成または新規制作によって用意する。アセットの存在を理由にゲーム上の新機能を創作しない。

#### Acceptance Criteria
##### AC-F028-001: 表示の完全性
Preconditions: 全登録item・block・fluidを表示する。  
Action: Creative inventory、ワールド配置、装備表示、破壊particleを確認する。  
Expected Result: Missing Model/Textureがなく、tool・金属・鉱石・晶洞に適正な外観がある。

##### AC-F028-002: アニメーションと配布条件
Preconditions: Neutroniumと使用した第三者由来アセットがある。  
Action: アニメーションとライセンス記録を確認する。  
Expected Result: Neutroniumが意図したアニメーションで表示され、アセット出典・利用許可を追跡できる。

### F-029: 統合検証とリリース品質
#### Description

新規実装として以下を必須とする。専用サーバー/クライアントでの起動、Data Generation、レシピJSONの解析、全登録IDの照合、39金属×基本・追加形状の網羅、35鉱石系列のレシピ確認、TFC設備での手動実行、3ディメンションの生成、3種の範囲採掘工具の保護・loot・耐久・方向試験、FirmaLife導入時と未導入時の両試験を行う。

ゲーム内の数値は本書に列挙したものを使用する。新しい仕組みに必要な値が本書にない場合は実装側で値を作らず、Open Questionsへ記録して仕様決定後に実装する。

#### Acceptance Criteria
##### AC-F029-001: ビルド・起動
Preconditions: 本書の対応環境がある。  
Action: Java 21で完全ビルドし、クライアント・専用サーバーを起動する。  
Expected Result: ビルド成功、正常ロード、レジストリ競合やresource読み込みエラーなし。

##### AC-F029-002: 鉱石処理の網羅
Preconditions: 35系列の入力鉱石とTFC加工設備がある。  
Action: Step 1〜5を必要な全系列で実行しloot・加工量を検査する。  
Expected Result: 本書の数量と確率、Native Naquadah・Mithril Matrixの固有設定、Dirty Pile廃止が確認される。

##### AC-F029-003: 装備・ワールド生成の網羅
Preconditions: 全金属、3種の範囲工具、3ディメンションの検証ワールドがある。  
Action: 工具tier・範囲・event・loot・耐久・worldgenを検査する。  
Expected Result: F-007〜F-008、F-017〜F-025の受入条件を満たす。

## Global Requirements

1. **仕様の独立性**：このファイルの機能・数量・確率・対象系列を実装上の正本とする。現行版の非公開実装、過去のID一覧、内部クラス名、条件分岐に依存しない。
2. **依存の最小化**：必須ModはTFCとTFCが要求するPatchouliだけとする。FirmaLifeは導入時に限り追加連携が有効になる。実行時に別Modの機械、加工台、道具、レシピserializerを要求しない。
3. **対象版の更新**：Minecraftは1.21.1に固定し、NeoForge/TFC/Patchouli/FirmaLifeの1.21.1向け新しい安定版が公開された場合は、互換性テストのうえで依存版を更新する。異なるMinecraft向けの「最新版」を誤って採用しない。
4. **クライアント・サーバー境界**：レシピ、loot、鉱脈、採掘、工具消耗はサーバー側で決定する。クライアントは表示と入力のみを担当する。
5. **機能の完全性**：すべての金属・鉱石・派生形状に、必要な登録、加工レシピ、Heat、Size、Tag、Model、Localization、Lootが存在すること。
6. **確率の正確性**：本書の確率出力は各loot poolによる独立抽選であり、確率の総和を100%へ正規化しない。
7. **Dirty Pileの全面廃止**：生成・素材・レシピ・loot・タグ・翻訳・モデルにDirty Pileを残さない。Sand Pileは別素材として保持する。
8. **金属量の例外**：Nuggetのクラフト個数9と溶融量10 mBは両立する仕様として扱う。通常の金属量保存規則はこの個別例外を上書きしない。
9. **既存アセットの利用**：現行公開版から正当に再利用できるアセットを用いる。ソースコードの実装詳細を前提とせず、出典と再配布条件を記録する。
10. **名前空間**：追加コンテンツ、生成レシピ、独自lootの名前空間は原則 `tfcm` とする。TFC/FirmaLifeの既存IDを参照するときは対象版の正式IDを使用する。
11. **後方互換性なし**：旧ワールド、設定、レシピ、registryに対する移行・alias・fallbackを要求しない。
12. **未定義仕様の禁止**：未指定のレシピ個数、性能、確率、採掘面、代替工程、設備を作らない。仕様上の未決事項はOpen Questionsを解消した後に確定する。
13. **安定した受入条件ID**：`F-xxx` と `AC-Fxxx-xxx` は後続の仕様変更でも可能な限り維持する。既存IDを他機能へ再利用しない。

## Constraints

- Minecraft 1.21.1のみ対象とし、マルチバージョンビルド、旧loader、旧フォーマット変換を実装しない。
- 新たなワールド互換性保証を行わない。
- TFC以外の加工設備を要求せず、鉱石処理のStep 1〜5、Pan・Sluice資源採取をTFCの仕組みだけで実装する。
- TFC/FirmaLifeの登録IDやpublic APIは対象版の正式仕様を参照するが、本仕様書ではそれらの内部実装を既知と仮定しない。
- 既存アセットの直接再利用・改変はライセンス遵守と出典記録を前提とする。フォントを含む第三者ファイルを無条件に再配布しない。
- F-018の5×5工具は採掘範囲と中心位置以外に独自性能を持たない。
- F-010の鉱石処理定義はワールド生成設定とは独立している。処理対象の鉱石が特定ディメンションに自然生成するとは限らない。
- Dirty Dustの確率出力をDirty Pileの金属量へ自動補正しない。

## Open Questions

None.
