source ./set-env.sh

echo " == Printing the most important environment variables"
echo " MANIFEST: ${MANIFEST}"
echo " TESTS_IMAGE: ${TESTS_IMAGE}"
echo " JAHIA_IMAGE: ${JAHIA_IMAGE}"
echo " JAHIA_URL: ${JAHIA_URL}"
echo " JAHIA_HOST: ${JAHIA_HOST}"

docker-compose up --renew-anon --abort-on-container-exit
