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
./gradlew -Dorg.gradle.parallel=false  \
          -Dorg.gradle.daemon=false \
          -Dorg.gradle.jvmargs="-Xmx1024m -XX:MaxPermSize=512m -XX:+HeapDumpOnOutOfMemoryError" \
          :app:testGoogleplayDebugUnitTest


if [ $? -ne 0 ]; then
    echo "UnitTest failed..."
    report_cp
    exit 1
else
    report_cp
fi
