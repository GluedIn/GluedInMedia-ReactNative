import React from 'react';

import SignInScreen from '../screens/LoginScreen';
import {createNativeStackNavigator} from '@react-navigation/native-stack';

const AuthStack = createNativeStackNavigator();

const AuthNavigator = ({setIsAuthenticated, setIsGuestLogin}: any) => (
  <AuthStack.Navigator screenOptions={{headerShown: false}}>
    <AuthStack.Screen
      name="Login"
      // Pass the onSignIn callback to update authentication state
      children={(props: any) => (
        <SignInScreen
          {...props}
          onGuestSignIn={() => setIsGuestLogin(true)}
          onSignIn={() => setIsAuthenticated(true)}
        />
      )}
    />
  </AuthStack.Navigator>
);

export default AuthNavigator;
