#! /bin/sh

# ビルド
./gradlew -PpreDexEnable=false -Pcom.android.build.threadPoolSize=1  \
          :app:assembleGoogleplayDebug :app:assembleGoogleplayRelease \
          ciCollectAndroidApps

if [ $? -ne 0 ]; then
    echo "assemble failed..."
    exit 1
fi
