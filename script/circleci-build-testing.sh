#! /bin/sh


report_cp() {
cp -R "./app/build/reports" "$CIRCLE_TEST_REPORTS"
}

# テスト実行
./gradlew testDebug

if [ $? -ne 0 ]; then
    echo "UnitTest failed..."
    report_cp
    exit 1
else
    report_cp
fi
