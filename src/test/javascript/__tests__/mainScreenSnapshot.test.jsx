import React from 'react';
import {act, cleanup, render} from '@testing-library/react';
import '@testing-library/jest-dom/extend-expect';
import Home from '../../../main/javascript/Sandbox/Home/Home';
import {sandboxMocks} from '../apolloMocks';
import {MockedProvider, wait} from '@apollo/react-testing';

describe('Validate main screen snapshot', () => {
    afterEach(() => {
        cleanup();
    });
    test('match snapshot', async () => {
        const {container} = render(
            <MockedProvider mocks={sandboxMocks} addTypename={false}>
                <Home/>
            </MockedProvider>
        );
        await act(async () => {
            await wait(0);
        });
        expect(container).toMatchSnapshot();
    });
});

