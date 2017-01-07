#! /bin/sh

#################################################################################
# ACE開発者がprivateファイルを同期するためのスクリプト
# OSS化に伴い、プライベートリポジトリで署名鍵等の重要ファイルを管理する
# ace-privateを同期後、必要なファイルをローカルにコピーする
#################################################################################
if [ -e ./ace-private/v3.0.x ]; then
    echo "installed ace-private/"
    cd ace-private/
    git fetch
    git pull origin master
    cd ..
else
    git clone git@bitbucket.org:eaglesakura/ace-private.git
fi

if [ -e "./ace-private/v3.0.x/" ]; then
    cp -rf ./ace-private/v3.0.x/main           ./app/private/
    cp -rf ./ace-private/v3.0.x/sign           ./app/private/
    cp -f  ./ace-private/v3.0.x/private.gradle ./app/private/
    cp -rf ./ace-private/v3.0.x/debug          ./app/src/
    cp -rf ./ace-private/v3.0.x/release        ./app/src/
else
    echo "ace-private not sync"
    cp -rf ./app/private.tmp/** ./app/private/
fi
