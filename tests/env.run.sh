#!/bin/bash
# This script can be used to warmup the environment and execute the tests
# It is used by the docker image at startup

source ./set-env.sh

#!/usr/bin/env bash
START_TIME=$SECONDS

echo " == Using MANIFEST: ${MANIFEST}"
echo " == Using JAHIA_URL= ${JAHIA_URL}"

echo " == Waiting for Jahia to startup"
while [[ "$(curl -s -o /dev/null -w ''%{http_code}'' ${JAHIA_URL}/cms/login)" != "200" ]];
  do sleep 5;
done
ELAPSED_TIME=$(($SECONDS - $START_TIME))
echo " == Jahia became alive in ${ELAPSED_TIME} seconds"

mkdir -p ./run-artifacts
mkdir -p ./results

# Add the credentials to a temporary manifest for downloading files
# Execute jobs listed in the manifest
# If the file doesn't exist, we assume it is a URL and we download it locally
if [[ -e ${MANIFEST} ]]; then
  cp ${MANIFEST} ./run-artifacts
else
  echo "Downloading: ${MANIFEST}"
  curl ${MANIFEST} --output ./run-artifacts/curl-manifest
  MANIFEST="curl-manifest"
fi

sed -i -e "s/NEXUS_USERNAME/$(echo ${NEXUS_USERNAME} | sed -e 's/\\/\\\\/g; s/\//\\\//g; s/&/\\\&/g')/g" ./run-artifacts/${MANIFEST}
sed -i -e "s/NEXUS_PASSWORD/$(echo ${NEXUS_PASSWORD} | sed -e 's/\\/\\\\/g; s/\//\\\//g; s/&/\\\&/g')/g" ./run-artifacts/${MANIFEST}
sed -i "" -e "s/JAHIA_VERSION/${JAHIA_VERSION}/g" ./run-artifacts/${MANIFEST}

# If we're building the module (and manifest name contains build), then we're copying the module file to one with a set name'
cd ./artifacts
ls -lah
for file in *-SNAPSHOT.jar
do
  echo "$(date +'%d %B %Y - %k:%M') == Processing file: $file =="
  if [[ $file == *"server-availability-manager"* ]]; then
    echo "$(date +'%d %B %Y - %k:%M') == copying $file to ./SAM-SNAPSHOT.jar =="
    cp $file ./SAM-SNAPSHOT.jar
  fi
done
ls -lah
cd ..

./node_modules/jahia-cli/bin/run manifest:run --manifest=./run-artifacts/${MANIFEST} --jahiaAdminUrl=${JAHIA_URL} --jahiaToolsUsername=${JAHIA_USERNAME_TOOLS} --jahiaToolsPassword=${JAHIA_PASSWORD_TOOLS} --nosandbox
if [[ $? -eq 1 ]]; then
  echo "PROVISIONING FAILURE - EXITING SCRIPT, NOT RUNNING THE TESTS"
  echo "failure" >/tmp/results/test_failure
  exit 1
fi

echo "$(date +'%d %B %Y - %k:%M') == Fetching the list of installed modules =="
./node_modules/jahia-reporter/bin/run utils:modules \
  --moduleId="${MODULE_ID}" \
  --jahiaUrl="${JAHIA_URL}" \
  --jahiaPassword="${SUPER_USER_PASSWORD}" \
  --filepath="results/installed-jahia-modules.json"
echo "$(date +'%d %B %Y - %k:%M') == Modules fetched =="
INSTALLED_MODULE_VERSION=$(cat results/installed-jahia-modules.json | jq '.module.version')
if [[ $INSTALLED_MODULE_VERSION == "UNKNOWN" ]]; then
  echo "$(date +'%d %B %Y - %k:%M') ERROR: Unable to detect module: ${MODULE_ID} on the remote system "
  echo "$(date +'%d %B %Y - %k:%M') ERROR: The Script will exit"
  echo "$(date +'%d %B %Y - %k:%M') ERROR: Tests will NOT run"
  echo "failure" > ./results/test_failure
  exit 1
fi

echo "$(date +'%d %B %Y - %k:%M')== Run tests =="
yarn e2e:ci
if [[ $? -eq 0 ]]; then
  echo "success" > ./results/test_success
  exit 0
else
  echo "failure" > ./results/test_failure
  exit 1
fi

# After the test ran, we're dropping a marker file to indicate if the test failed or succeeded based on the script test command exit code
