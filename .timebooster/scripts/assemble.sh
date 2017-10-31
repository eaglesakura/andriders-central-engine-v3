#! /bin/sh

# ビルド
./gradlew --parallel \
          :app:assembleTerminalDebug \
          :app:assembleTerminalRelease \
          ciCollectAndroidApps

# lint check
# cat  ./app/build/outputs/lint-results-googleplayDebug.xml
if [ $? -ne 0 ]; then
    echo "assemble failed..."
    cat  ./app/build/outputs/lint-results-googleplayDebug.xml
    exit 1
fi

if [[ "${CIRCLE_BRANCH:-nil}" =~ develop ]]; then
    echo "Deploy to Google Play[Alpha]"
    ./gradlew uploadGooglePlayDevelopAlpha
    ./gradlew uploadGooglePlayProductionAlpha
fi