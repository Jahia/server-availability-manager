import React, {useState} from 'react';
import {useTranslation} from 'react-i18next';
import {Add, Button, GlobalStyle, Typography} from '@jahia/moonstone';
import styles from './Home.scss';
import {getCurrentUserName} from './Home.gql';
import {useQuery} from '@apollo/react-hooks';

const Home = () => {
    const {t} = useTranslation('sandbox');
    const [userInformation, setUserInformation] = useState({username: ''});

    const updateCurrentUser = data => {
        setUserInformation({...userInformation, username: data.currentUser.name});
    };

    useQuery(getCurrentUserName, {
        onCompleted: updateCurrentUser
    });

    return (
        <React.Fragment>
            <GlobalStyle/>
            <div className={styles.root}>
                <div className={styles.headerRoot}>
                    <header className={styles.header}>
                        <Typography variant="title">{t('sandbox:title')}
                        </Typography>
                        <div className={styles.actionBar}>
                            <Button size="big"
                                    color="accent"
                                    label={t('sandbox:action.buttonTitle')}
                                    icon={<Add/>}
                                    onClick={() => {}}/>
                        </div>
                    </header>
                </div>
                <div className={styles.content}>
                    <span>Some content, connected with: {userInformation.username}</span>
                </div>
            </div>
        </React.Fragment>
    );
};

export default Home;
