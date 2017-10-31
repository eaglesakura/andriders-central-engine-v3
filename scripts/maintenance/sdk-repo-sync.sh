#! /bin/sh

REPOSITORY=andriders-central-engine-sdk
DIRECTORY=sdk
BRANCH=v3.0.x

echo "########################################"
echo "## sync $REPOSITORY / $BRANCH"
echo "########################################"
if [ ! -e $REPOSITORY ]; then
  git clone git@github.com:eaglesakura/$REPOSITORY.git $DIRECTORY
fi

cd $DIRECTORY
git config core.filemode false
git fetch
git checkout -f $BRANCH
git branch -a
cd ..

