#! /bin/sh -eu

repo_sync() {
    REPOSITORY=$1
    BRANCH=$2
    echo "########################################"
    echo "## sync $REPOSITORY / $BRANCH"
    echo "########################################"

    if [ ! -e $REPOSITORY ]; then
      git clone git@github.com:eaglesakura/$REPOSITORY.git
    fi

    cd $REPOSITORY
    git config core.filemode false
    git fetch
    git checkout -f $BRANCH
    git branch -a
    cd ..
}

if [ ! -e "eglibrary/" ]; then
  mkdir "eglibrary"
fi

cd eglibrary

# repo_sync "alternet"                    "v2.3.x"
repo_sync "android-bluetooth"           "v1.3.x"
# repo_sync "android-camera"              "v1.3.x"
# repo_sync "android-command-service"     "v1.5.x"
repo_sync "android-commons"             "v2.3.x"
# repo_sync "android-devicetest-support"  "v2.3.x"
repo_sync "android-firebase"            "v2.0.x"
repo_sync "android-gms"                 "v2.0.x"
# repo_sync "android-text-kvs"            "v2.4.x"
# repo_sync "android-unittest-support"    "v1.6.x"
# repo_sync "cerberus"                    "v3.0.x"
# repo_sync "garnet"                      "v1.2.x"
# repo_sync "geo-utils"                   "v1.2.x"
# repo_sync "greendao-wrapper"            "v2.4.x"
repo_sync "java-commons"                "v2.2.x"
# repo_sync "json-wrapper"                "v1.2.x"
# repo_sync "junit-support"               "v1.2.x"
# repo_sync "light-saver"                 "v1.4.x"
# repo_sync "margarineknife"              "v1.5.x"
# repo_sync "onactivityresult-invoke"     "v1.3.x"
# repo_sync "publd-serializer"            "v2.1.x"
# repo_sync "simple-logger"               "v2.1.x"
repo_sync "sloth-framework"             "v4.0.x"
# repo_sync "small-aquery"                "v1.3.x"
