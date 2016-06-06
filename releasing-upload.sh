#!/bin/bash

function error_exit {
    echo "$1" 1>&2
    exit 1
}

function exit_if_error {
    if [ $? == 0 ]; then
        return
    fi
    echo "$1" 1>&2
    exit 1
}

version=$1
remote=$2

if [ -z "$version" ]; then
    error_exit "Please specific release version."
fi

git commit -am "Prepare for release $version"

exit_if_error "Commit failed."

./gradlew clean bintrayUpload

exit_if_error "Upload failed."

echo "Now review your code on Gerrit. Then do release push after pull from Gerrit."

