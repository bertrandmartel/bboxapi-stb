#!/bin/bash

modules=("bboxapi-stb" "bboxapi-stb-android")
for i in "${modules[@]}"
do
echo ">> Deploying $i ..."
./gradlew :$i:clean :$i:build :$i:bintrayUpload -PbintrayUser=$BINTRAY_USER -PbintrayKey=$BINTRAY_KEY -PdryRun=false
echo ">> Done deploying for $i"
done
