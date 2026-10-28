# TFCM 開発ガイド

## 仕様と実装状態

- 新規実装の要求仕様は `PROJECT.md`。JavaコードとGradle設定にはM01の対応環境・依存関係基盤を実装済みであり、後続機能の実装済みを意味しない。
- GradleはNeoForge `21.1.265`、Mod IDは `tfcm`、基本パッケージは `net.claustra01.tfcm` を使用する。
- Minecraftは `[1.21.1]` に限定する。TFC `[4.2.12,)` とPatchouli `[1.21.1-93-NEOFORGE,)` は必須、FirmaLife `[3.0.15,)` は任意で、3 ModともTFCMより先に読み込む。

## M01 verification

- `gameTest` source setを使用し、`runGameTestServer` はTFC・Patchouliのみ、続けて`runGameTestServerFirmaLife` はFirmaLife 3.0.15を追加して実行する。
- 各runは`tfcm.gametest.report`と`tfcm.gametest.structures`を設定する。JUnit XMLはそれぞれ`build/test-results/gametest/`と`build/test-results/gametest-firmalife/`に出力する。
- `./gradlew classes build runGameTestServer`でcompile、build、両GameTest環境を検証する。`tests/e2e/foundation-boot.mjs`はTFC・Patchouliのみの専用サーバーとclient `tfcm-a`の接続を検証する。

## Harness runtime

- 設定は `.harness-config.json`。ローカル生成物はGit管理外の `.harness-state/runtime/` に置く。
- MC Pilot `0.20.2` は `pilot/node_modules/.bin/mct`。`MCT_HOME` と `MCT_CACHE_DIR` はそれぞれruntime内の `mct-home` と `mct-cache` を指定する。
- Java 21、Minecraft `1.21.1`、NeoForge `21.1.265` をserver/client双方で使用する。
- serverは `server/`、接続先は `127.0.0.1:25583`、検証worldは `harness-world`。harnessは起動時にこのworldを作り直す。
- client名は `tfcm-a`、offline accountは `tfcm_a`。作成時のWebSocket portは `25584`。harness起動時には空きportへ割り当て直される。
- server/client双方にTFC `4.2.12` とPatchouli `1.21.1-93-NEOFORGE` を配置する。現状はFirmaLifeなしの基本構成。FirmaLife `3.0.15` の有無を比較する試験では別途双方へ配置・除去する。
- `node .harness/dist/cli/main.js doctor` と `validate` で構成を確認する。`plan` はClaude CLIへ仕様を送って計画を生成し、仕様・plan・progressをローカルGitへcommitする。
- runtime準備だけではTFCMの受入条件を検証したことにならない。Modの実装・build・GameTest・E2Eは後続の開発工程で行う。
