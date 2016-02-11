#! /bin/sh

###############################################
# ビルドに必要な依存環境を取得するスクリプト。
# キャッシュの削除とsyncを行うため、時間がかかることに注意する。
# リポジトリのrootから実行する
#
# ./scripts/refresh-dependenceis.sh
#
###############################################
echo "delete gradle cache"
rm -rf "~/.gradle/"
./gradlew clean

echo "sync submodules"
git submodule update --init

echo "update Android SDK"
sh -c "$(curl -fsSL https://raw.githubusercontent.com/eaglesakura/build-dependencies/master/android-sdk.sh)"
