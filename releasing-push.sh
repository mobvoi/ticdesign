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

git tag -a $version -m "Version $version"

exit_if_error "Tagging failed."

git push $remote && git push $remote --tags

exit_if_error "Push to repo failed."

