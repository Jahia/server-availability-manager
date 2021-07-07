FROM cypress/base:14

RUN apt-get update && apt-get install -y jq

RUN adduser --disabled-password jahians

USER jahians
WORKDIR /home/jahians

COPY --chown=jahians:jahians package.json yarn.lock /home/jahians/

RUN mkdir -p /home/jahians/run-artifacts /home/jahians/results /home/jahians/cypress/plugins

#CI=true reduces the verbosity of the installation logs
RUN CI=true yarn install

COPY --chown=jahians:jahians . /home/jahians

RUN ls -lah /home/jahians/artifacts/

RUN CI=true /home/jahians/node_modules/.bin/cypress install

# Jahia-cli is used to warmup the environment at startup
RUN yarn add jahia-cli@latest

CMD ["/bin/bash", "-c", "/home/jahians/env.run.sh"]
