/* eslint-disable @typescript-eslint/no-var-requires */
const cypressTypeScriptPreprocessor = require('./cy-ts-preprocessor')
const sshCommand = require('./ssh')
const installLogsPrinter = require('cypress-terminal-report/src/installLogsPrinter')
const env = require('./env')
const FormData = require('form-data')
const axios = require('axios')

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
        async installModule(moduleInfo) {
            let resp;
            try {
                resp = await axios({
                    method: 'get',
                    url: `https://store.jahia.com/cms/mavenproxy/private-app-store/org/jahia/modules/${moduleInfo.name}/${moduleInfo.version}/${moduleInfo.name}-${moduleInfo.version}.jar`,
                    responseType: 'stream'
                });
            } catch (e) {
                console.error('Failed to download module: ', e)
                return false;
            }

            const form = new FormData();
            form.append('bundle', resp.data);

            try {
                await axios.post(`${config.env.JAHIA_URL}/modules/api/bundles`, form, {
                    headers: {
                        ...form.getHeaders(),
                        Origin: config.env.JAHIA_URL
                    },
                    maxContentLength: Infinity,
                    maxBodyLength: Infinity,
                    auth: {
                        username: 'root',
                        password: config.env.SUPER_USER_PASSWORD,
                    },
                })
            } catch (e) {
                console.error('Failed to install module: ', e);
                return false;
            }

            return true;
        },
        async uninstallModule(moduleInfo) {
            try {
                await axios.post(`${config.env.JAHIA_URL}/modules/api/bundles/${moduleInfo.key}/_uninstall`, null, {
                    headers: {
                        Origin: config.env.JAHIA_URL
                    },
                    maxContentLength: Infinity,
                    maxBodyLength: Infinity,
                    auth: {
                        username: 'root',
                        password: config.env.SUPER_USER_PASSWORD,
                    },
                })
            } catch (e) {
                console.error('Failed to uninstall module: ', e)
                return false;
            }

            return true
        }
    })
    return config
}
