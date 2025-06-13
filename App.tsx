import {View, Text} from 'react-native';
import React, {useEffect} from 'react';

import {NavigationContainer} from '@react-navigation/native';
import AppNavigator from './src/navigation/AppNavigator';
import AuthNavigator from './src/navigation/AuthNavigator';

import AsyncStorage from '@react-native-async-storage/async-storage';

export default function App() {
  const [isAuthenticated, setIsAuthenticated] = React.useState(false);
  const [isGuestLogin, setIsGuestLogin] = React.useState(false);

  useEffect(() => {
    const checkAuthStatus = async () => {
      // const token = await AsyncStorage.getItem('userToken');
      // if (token) {
      setIsAuthenticated(true);
      // } else {
      // setIsAuthenticated(false);
      // }
    };

    checkAuthStatus();
  }, []);

  return (
    // <NavigationContainer>
    //   {isAuthenticated || isGuestLogin ? (
    //     <AppNavigator
    //       isGuestLogin={isGuestLogin}
    //       setIsGuestLogin={setIsGuestLogin}
    //       isAuthenticated={isAuthenticated}
    //       setIsAuthenticated={setIsAuthenticated}
    //     />
    //   ) : (
    //     <AuthNavigator
    //       setIsGuestLogin={setIsGuestLogin}
    //       setIsAuthenticated={setIsAuthenticated}
    //     />
    //   )}
    // </NavigationContainer>

    <NavigationContainer>
      <AppNavigator
        isGuestLogin={isGuestLogin}
        setIsGuestLogin={setIsGuestLogin}
        isAuthenticated={isAuthenticated}
        setIsAuthenticated={setIsAuthenticated}
      />
    </NavigationContainer>
  );
}
