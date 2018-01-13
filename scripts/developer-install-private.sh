#! /bin/bash

mkdir private
rm -rf private/ace-private/

#################################################################################
# ACE開発者がprivateファイルを同期するためのスクリプト
# OSS化に伴い、プライベートリポジトリで署名鍵等の重要ファイルを管理する
# ace-privateを同期後、必要なファイルをローカルにコピーする
#################################################################################
git clone git@bitbucket.org:eaglesakura/ace-private.git private/ace-private

if [ -e "./private/ace-private/v3.0.x/" ]; then
    cp -rf ./private/ace-private/v3.0.x/** .
else
    echo "ace-private not sync"
    cp -rf private.template/** .
fi
