import React from 'react';
import {registry} from '@jahia/ui-extender';
import Home from './Home/Home';
import {Accessibility} from '@jahia/moonstone';

export const registerSandbox = () => {
    registry.add('adminRoute', 'sandbox', {
        targets: ['dashboard:99.1'],
        // Icon is Lock as of now, will be changed to proper one after moonstone release
        icon: <Accessibility/>,
        label: 'sandbox:title',
        isSelectable: true,
        render: () => <Home/>
    });
};
