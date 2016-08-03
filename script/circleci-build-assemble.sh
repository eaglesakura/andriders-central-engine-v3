#! /bin/sh

# ビルド
./gradlew -PpreDexEnable=false -Pcom.android.build.threadPoolSize=1  \
          :app:assembleGoogleplayDebug :app:assembleGoogleplayRelease \
          ciCollectAndroidApps

# lint check
# cat  ./app/build/outputs/lint-results-googleplayDebug.xml
if [ $? -ne 0 ]; then
    echo "assemble failed..."
    cat  ./app/build/outputs/lint-results-googleplayDebug.xml
    exit 1
fi
