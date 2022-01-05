"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.registerPlugins = void 0;
const env_1 = __importDefault(require("./env"));
const installLogsPrinter_1 = __importDefault(require("cypress-terminal-report/src/installLogsPrinter"));
const registerPlugins = (on, config) => {
    env_1.default(on, config);
    installLogsPrinter_1.default(on);
};
exports.registerPlugins = registerPlugins;
