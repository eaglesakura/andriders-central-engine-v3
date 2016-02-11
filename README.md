# Andriders Central Engine Version 3.x

サイクルコンピューターアプリ [Andriders Central Engine](https://play.google.com/store/apps/details?id=com.eaglesakura.andriders) のリニューアルバージョンです。リニューアルに伴い、署名鍵等の重要なプライベートファイルを除いた全てのソースコードをオープン化します。

**現在は開発中であり、正常な動作は行えません。**

| ブランチ | 内容 | ビルドステータス |
|---|---|---|
| master | 最新のリリース版（予定） | - |
| develop | 最新の開発版, DEBUG版デプロイ用 | [![Circle CI](https://circleci.com/gh/eaglesakura/andriders-central-engine-v3/tree/develop.svg?style=svg)](https://circleci.com/gh/eaglesakura/andriders-central-engine-v3/tree/develop) |
| feature/id/{issue num} | issue対応 | - |
| v3.0.x | リリースビルド（予定） | - |


2016年5月の"アルプスあづみのセンチュリーライド"前後のリリースを目標に開発します。

## 開発版インストール

 * DeployGate版のインストールは下記のリンクから行えます。
  * https://dply.me/o3nql5

![DeployGate](https://chart.googleapis.com/chart?chs=256x256&cht=qr&chl=https%3A%2F%2Fdeploygate.com%2Fdistributions%2F0e9f7d4a23ab9744856a5b1be5b2e353fe963baf)

## ビルド方法

コマンドラインで下記のコマンドを実行することで、アプリをビルドすることができます。

ただし、署名鍵等はダミーファイルがコミットされているため、Google PlayやDeploygateにアップロードされているapkを生成することはできません。

アプリの動作確認は可能です。

<pre>
git clone git@github.com:eaglesakura/andriders-central-engine-v3.git
cd andriders-central-engine-v3
git checkout develop
git submodule update --init
./gradlew assembleDebug
</pre>

## ライセンス

MITライセンスで配布します。このコードを一部、もしくは全部を利用した場合はライセンスにしたがって表記を行ってください。
