import {getCurrentUserName} from '../../main/javascript/Sandbox/Home/Home.gql';
export const sandboxMocks = [
    {
        request: {
            query: getCurrentUserName
        },
        result: () => {
            return {
                data: {
                    currentUser: {
                        name: 'JohnDoe'
                    }
                }
            };
        }
    }
];
