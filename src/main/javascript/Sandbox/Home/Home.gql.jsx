import gql from 'graphql-tag';

const getCurrentUserName = gql`
    {
      currentUser {
        name
      }
    }
`;

export {getCurrentUserName};
