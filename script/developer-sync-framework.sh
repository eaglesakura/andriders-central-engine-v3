#! /bin/sh
if [ -e eglibrary/.git/ ]; then
  echo "eglibrary has cache"
else
  rm -rf ./eglibrary
  git clone git@github.com:eaglesakura/eglibrary-java.git ./eglibrary
fi

cd eglibrary/
git clean -f .
git pull origin master
chmod 755 ./script/sync-develop.sh
./script/sync-develop.sh
cd ../
