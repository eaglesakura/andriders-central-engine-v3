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
    cd ..
else
    git clone git@bitbucket.org:eaglesakura/ace-private.git
fi

if [ -e "./ace-private/v3.0.x/" ]; then
    cp -rf ./ace-private/v3.0.x/** ./app/private/
else
    echo "ace-private not sync"
    cp -rf ./app/private.tmp/** ./app/private/
fi
