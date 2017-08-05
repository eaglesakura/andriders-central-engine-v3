#! /bin/sh

# ビルド
./gradlew -PpreDexEnable=false \
          :app:assembleGoogleplayDebug \
          :app:assembleGoogleplayRelease \
          ciCollectAndroidApps

# lint check
# cat  ./app/build/outputs/lint-results-googleplayDebug.xml
if [ $? -ne 0 ]; then
    echo "assemble failed..."
    cat  ./app/build/outputs/lint-results-googleplayDebug.xml
    exit 1
fi
