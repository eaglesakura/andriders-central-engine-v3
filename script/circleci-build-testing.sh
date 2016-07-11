#! /bin/sh


report_cp() {
mkdir "$CIRCLE_ARTIFACTS/app"
mkdir "$CIRCLE_TEST_REPORTS/junit/"
mkdir "$CIRCLE_TEST_REPORTS/junit/googleplayDebug"

cp -r ./app/build/reports "$CIRCLE_ARTIFACTS/app"
find . -type f -regex ".*/build/test-results/googleplayDebug/.*xml" -exec cp {} $CIRCLE_TEST_REPORTS/junit/googleplayDebug/ \;

#mkdir "$CIRCLE_TEST_REPORTS/junit/googleplayRelease"
#find . -type f -regex ".*/build/test-results/googleplayRelease/.*xml" -exec cp {} $CIRCLE_TEST_REPORTS/junit/googleplayRelease/ \;
#mkdir "$CIRCLE_ARTIFACTS/sdk"
#cp -r ./sdk/build/reports "$CIRCLE_ARTIFACTS/sdk"
}

# テスト実行
./gradlew -PpreDexEnable=false -Pcom.android.build.threadPoolSize=1 -Dorg.gradle.parallel=false -Dorg.gradle.jvmargs="-Xms512m -Xmx512m" -Dorg.gradle.daemon=false :app:testGoogleplayDebugUnitTest


if [ $? -ne 0 ]; then
    echo "UnitTest failed..."
    report_cp
    exit 1
else
    report_cp
fi
