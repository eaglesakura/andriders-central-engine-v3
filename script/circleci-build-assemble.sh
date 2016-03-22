#! /bin/sh

# ビルド
./gradlew ciClean clean

if [ $? -ne 0 ]; then
    echo "build failed..."
    exit 1
fi

./gradlew assembleRelease

if [ $? -ne 0 ]; then
    echo "build failed..."
    exit 1
fi

./gradlew assembleDebug

if [ $? -ne 0 ]; then
    echo "build failed..."
    exit 1
fi

# 成果物収集
./gradlew ciCollectAndroidApps

if [ $? -ne 0 ]; then
    echo "unit test failed..."
    exit 1
fi
