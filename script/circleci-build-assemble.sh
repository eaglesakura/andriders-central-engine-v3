#! /bin/sh

# ビルド
./gradlew -PpreDexEnable=false -Pcom.android.build.threadPoolSize=1 -Dorg.gradle.parallel=false -Dorg.gradle.jvmargs="-Xms512m -Xmx512m" -Dorg.gradle.daemon=false :app:assembleGoogleplayDebug :app:assembleGoogleplayRelease

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
