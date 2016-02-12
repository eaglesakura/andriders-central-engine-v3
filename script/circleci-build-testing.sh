#! /bin/sh

# 通常ビルド
./script/circleci-build-assemble.sh
if [ $? -ne 0 ]; then
    echo "build failed..."
    exit 1
fi

# テスト実行
./gradlew testDebug

if [ $? -ne 0 ]; then
    echo "build failed..."
    exit 1
fi