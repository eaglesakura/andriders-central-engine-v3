#! /bin/sh
if [ -e eglibrary/.git/ ]; then
  echo "eglibrary has cache"
else
  rm -rf ./eglibrary
  git clone git@github.com:eaglesakura/eglibrary.git
fi

cd eglibrary/
git clean -f .
git checkout -f develop
git pull origin develop
chmod 755 ./script/sync-develop.sh
./script/sync-develop.sh
cd ../