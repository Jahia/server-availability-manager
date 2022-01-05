declare global {
    namespace Cypress {
        interface Chainable<Subject> {
            executeGroovy(scriptFile: string): Chainable<any>;
        }
    }
}
export declare const executeGroovy: (scriptFile: string) => void;
