import './Sandbox';
import {registry} from '@jahia/ui-extender';

registry.add('callback', 'sandbox', {
    targets: ['jahiaApp-init:99'],
    callback: () => Promise.all([
        window.jahia.i18n.loadNamespaces('sandbox')
    ])
});
