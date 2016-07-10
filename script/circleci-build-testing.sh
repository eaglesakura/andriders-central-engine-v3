#! /bin/sh


report_cp() {
mkdir "$CIRCLE_TEST_REPORTS/junit/"
mkdir "$CIRCLE_TEST_REPORTS/junit/googleplayDebug"

find . -type f -regex ".*/build/test-results/googleplayDebug/.*xml" -exec cp {} $CIRCLE_TEST_REPORTS/junit/googleplayDebug/ \;

mkdir "$CIRCLE_ARTIFACTS/app"
cp -r ./app/build/reports "$CIRCLE_ARTIFACTS/app"

#mkdir "$CIRCLE_TEST_REPORTS/junit/googleplayRelease"
#find . -type f -regex ".*/build/test-results/googleplayRelease/.*xml" -exec cp {} $CIRCLE_TEST_REPORTS/junit/googleplayRelease/ \;
#mkdir "$CIRCLE_ARTIFACTS/sdk"
#cp -r ./sdk/build/reports "$CIRCLE_ARTIFACTS/sdk"
}

# テスト実行
./gradlew  -Dorg.gradle.daemon=false -Dorg.gradle.jvmargs="-Xmx2048m -XX:MaxPermSize=1024m -XX:+HeapDumpOnOutOfMemoryError" testGoogleplayDebugUnitTest


if [ $? -ne 0 ]; then
    echo "UnitTest failed..."
    report_cp
    exit 1
else
    report_cp
fi
