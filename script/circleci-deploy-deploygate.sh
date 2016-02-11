#! /bin/sh

# deploygate アップロード
./gradlew uploadDeployGate

if [ $? -ne 0 ]; then
    echo "upload failed..."
    exit 1
fi