/* eslint-disable @typescript-eslint/no-var-requires */
const cypressTypeScriptPreprocessor = require("./cy-ts-preprocessor");
const installLogsPrinter = require('cypress-terminal-report/src/installLogsPrinter');
const env = require('./env');

module.exports = (on, config) => {
    env(on, config)

    //https://github.com/archfz/cypress-terminal-report
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    installLogsPrinter(on);
    on("file:preprocessor", cypressTypeScriptPreprocessor);
    return config;
};
