#!/bin/bash
# This script can be used to manually build the docker images necessary to run the tests
# It should be executed from the tests folder

source ./set-env.sh

# It assumes that you previously built the module you're going to be testing
#   and that the modules artifacts are located one level up

if [ ! -d ./artifacts ]; then
  mkdir -p ./artifacts
fi

if [[ -e ../target ]]; then
  cp -R ../target/*-SNAPSHOT.jar ./artifacts/
  cp -R ../target/server-availability-*-SNAPSHOT.jar ./artifacts/SAM-SNAPSHOT.jar
fi

docker build -t ${TESTS_IMAGE} .
