#! /bin/sh -eu

repo_status() {
    REPOSITORY=$1

    if [ ! -f "build.gradle" ]; then
      return
    fi
    echo "########################################"
    echo "## check $REPOSITORY"
    echo "########################################"
    git status
}

cd eglibrary

for dir in `ls -F | grep /`; do
  cd $dir
  repo_status $dir
  cd ..
done
