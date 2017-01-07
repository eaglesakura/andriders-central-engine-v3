# Andriders Central Engine Version 3.x

サイクルコンピューターアプリ [Andriders Central Engine](https://play.google.com/store/apps/details?id=com.eaglesakura.andriders) のリニューアルバージョンです。リニューアルに伴い、署名鍵等の重要なプライベートファイルを除いた全てのソースコードをオープン化します。

**現在は開発中であり、正常な動作は行えません。**

| ブランチ | 内容 | ビルドステータス | 開発版ダウンロード |
|---|---|---|---|
| master | 最新ビルド（Nightly Build対象） |  [![Circle CI](https://circleci.com/gh/eaglesakura/andriders-central-engine-v3/tree/master.svg?style=svg)](https://circleci.com/gh/eaglesakura/andriders-central-engine-v3/tree/master) | - |
| develop | 最新の開発版, DEBUG版デプロイ用 | [![Circle CI](https://circleci.com/gh/eaglesakura/andriders-central-engine-v3/tree/develop.svg?style=svg)](https://circleci.com/gh/eaglesakura/andriders-central-engine-v3/tree/develop) | - |
| feature/id/{issue num} | issue対応 | - | - |
| v3.0.x | リリースビルド ver 3.0 | [![CircleCI](https://circleci.com/gh/eaglesakura/andriders-central-engine-v3/tree/v3.0.x.svg?style=svg)](https://circleci.com/gh/eaglesakura/andriders-central-engine-v3/tree/v3.0.x) | [<img src="https://dply.me/kj2ojo/button/large" alt="Try it on your device via DeployGate">](https://dply.me/kj2ojo#install) |

## ビルド方法

コマンドラインで下記のコマンドを実行することで、アプリをビルドすることができます。

ただし、署名鍵等はダミーファイルがコミットされているため、Google PlayやDeploygateにアップロードされているapkを生成することはできません。

署名ファイルやAPIキー等の重要情報は "app/private"配下に管理されます。ダミーファイルは"app/private.tmp"に保存されていますので、そのファイルを"app/private"配下にコピーする（もしくは"script/developer-install-private.sh"を実行することで所定の位置へファイルを移動できます。

アプリの動作確認は可能です。

### 必要環境

 1. JDK 1.8
 1. Cygwin(Windows環境の場合) / Terminal(Mac/Ubuntu環境の場合)
 1. Android SDK(可能な限り最新版)
 1. 環境変数: ANDROID_HOME

### ビルド手順

<pre>

# Android SDKの内容を同期する
sh -c "$(curl -fsSL https://raw.githubusercontent.com/eaglesakura/build-dependencies/master/android-sdk.sh)"

# リポジトリをcloneしてビルド用ブランチへ移動する
git clone git@github.com:eaglesakura/andriders-central-engine-v3.git
cd andriders-central-engine-v3
git checkout develop

# submoduleを同期し、SDK等を取得する
git submodule update --init

# スクリプトに実行権限を付与する
chmod 755 ./script/developer-install-private.sh
chmod 755 ./gradlew

# app/private.tmpをapp/privateにコピーする
./script/developer-install-private.sh

# ビルドを行う
./gradlew --refresh-dependencies assembleDebug
</pre>

## ライセンス

MITライセンスで配布します。このコードを一部、もしくは全部を利用した場合はライセンスにしたがって表記を行ってください。
