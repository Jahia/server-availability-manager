"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const env = (on, config) => {
    console.log('Setting env', process.env.JAHIA_URL);
    config.baseUrl = process.env.JAHIA_URL;
    config.env.JAHIA_URL = process.env.JAHIA_URL;
    config.env.SUPER_USER_PASSWORD = process.env.SUPER_USER_PASSWORD;
    return config;
};
exports.default = env;
