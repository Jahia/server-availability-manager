declare global {
    namespace Cypress {
        interface Chainable<Subject> {
            runProvisioningScript(script: FormFile, files?: FormFile[]): Chainable<any>;
        }
    }
}
declare type FormFile = {
    fileName?: string;
    fileContent?: string;
    type?: string;
};
export declare const runProvisioningScript: (script: FormFile, files?: FormFile[], options?: Cypress.Loggable) => void;
export {};
