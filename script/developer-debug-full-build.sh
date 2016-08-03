#! /bin/sh

# ディレクトリをリフレッシュ
rm -rf ./ci-release
mkdir ./ci-release

export CIRCLE_BUILD_NUM=3103
export CIRCLE_ARTIFACTS=ci-release
export CIRCLE_TEST_REPORTS=ci-release

chmod 755 ./gradlew
./gradlew  -Dapp.lib_no_compile=true \
           --refresh-dependencies \
           clean \
           :app:dependencies \
           :app:testGoogleplayDebug \
           :app:assembleAndroidTest \
           :app:assembleGoogleplayDebug
