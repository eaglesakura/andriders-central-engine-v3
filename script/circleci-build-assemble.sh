#! /bin/sh

# ビルド
./gradlew ciClean clean assembleDebug assembleRelease

if [ $? -ne 0 ]; then
    echo "build failed..."
    exit 1
fi

# 成果物収集
./gradlew ciCollect

if [ $? -ne 0 ]; then
    echo "collect failed..."
    exit 1
fi
