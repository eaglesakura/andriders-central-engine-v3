#! /bin/sh -eu
cat ./app/build.gradle | grep "// NEW " | sed -e "s/\/\/ NEW //" > deploy-log.txt

echo "" >> deploy-log.txt
echo "========= COMMIT =========" >> deploy-log.txt
echo "" >> deploy-log.txt

# 直近2日間のログを扱う
#COMMIT_LOG_SINCE=`date "+%Y-%m-%d" -d "-1 days"`
git log --oneline --no-merges --since="7 days ato" -n 10 >> deploy-log.txt

# 結果を表示する
cat deploy-log.txt

# deploygate アップロード
./gradlew uploadDeployGate
