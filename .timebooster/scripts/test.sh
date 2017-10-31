#! /bin/bash

report_cp() {
    MODULE=$1
    mkdir "$TIMEBOOSTER_ARTIFACTS/junit"
    mkdir "$TIMEBOOSTER_ARTIFACTS/junit/${MODULE}"
    mkdir "$TIMEBOOSTER_ARTIFACTS/junit-reports"
    mkdir "$TIMEBOOSTER_ARTIFACTS/junit-reports/${MODULE}"

    cp -r ./${MODULE}/build/reports "$TIMEBOOSTER_ARTIFACTS/junit/${MODULE}"
    cp -r ./${MODULE}/build/test-results "$TIMEBOOSTER_ARTIFACTS/junit-reports/${MODULE}"
}

# テスト実行
./gradlew :app:testTerminalDebugUnitTest

if [ $? -ne 0 ]; then
    echo "UnitTest failed..."
    report_cp "app"
    exit 1
else
    report_cp "app"
fi
