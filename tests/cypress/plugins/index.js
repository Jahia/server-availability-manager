/* eslint-disable @typescript-eslint/no-var-requires */
const cypressTypeScriptPreprocessor = require('./cy-ts-preprocessor')
const sshCommand = require('./ssh')
const installLogsPrinter = require('cypress-terminal-report/src/installLogsPrinter')
const env = require('./env')

module.exports = (on, config) => {
    env(on, config)

    //https://github.com/archfz/cypress-terminal-report
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    installLogsPrinter(on)
    on('file:preprocessor', cypressTypeScriptPreprocessor)
    on('task', {
        sshCommand(commands) {
            console.log(config.env)
            return sshCommand(commands, {
                hostname: config.env.JAHIA_HOST,
                port: config.env.JAHIA_PORT_KARAF,
                username: config.env.JAHIA_USERNAME_TOOLS,
                password: config.env.JAHIA_PASSWORD_TOOLS,
            })
        },
    })
    return config
}
