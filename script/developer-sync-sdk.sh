#! /bin/sh
git submodule update --init

repo_sync() {
    echo "########################################"
    echo "## sync $1 / $2"
    echo "########################################"
    cd $1

    git clean -f
    git fetch
    git branch -d $2
    git checkout -f $2
    git pull origin $2
    git branch
    cd ..
}

repo_sync "sdk" "develop"
