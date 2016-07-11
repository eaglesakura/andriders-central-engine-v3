#! /bin/sh

# ビルド
./gradlew -Dorg.gradle.parallel=false  \
          -Dorg.gradle.daemon=false \
          -Dorg.gradle.jvmargs="-Xmx1024m -XX:MaxPermSize=512m -XX:+HeapDumpOnOutOfMemoryError" \
          :app:assembleGoogleplayDebug :app:assembleGoogleplayRelease

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
