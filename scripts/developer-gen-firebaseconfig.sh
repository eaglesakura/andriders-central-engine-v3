#! /bin/bash -eu

if [[ $(uname) = CYGWIN* ]]; then
    export WORK_PATH="/`cygpath -w ${PWD}`"
    export WORK_PATH=`echo $WORK_PATH | sed -e 's/:\\\\/\//g' | sed -e 's/\\\\/\//g'`
else
    export WORK_PATH=$PWD
fi

for yaml in $(find firebase/config/ -name "*.yaml"); do
  echo "${yaml}"
  docker run --rm \
         -v "$WORK_PATH:/work" \
         -w "/work" \
         eaglesakura/yaml2json:1.0.1 \
         $yaml > ${yaml}.json
done