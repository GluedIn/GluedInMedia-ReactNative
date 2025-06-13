import React from 'react';
import {TouchableOpacity, NativeModules} from 'react-native';
import HomeScreen from '../screens/HomeScreen';
import {createBottomTabNavigator} from '@react-navigation/bottom-tabs';
import ProductDetailScreen from '../screens/ProductDetailScreen';
import {ArrowLeft} from 'lucide-react-native';
import ShortsScreen from '../screens/ShortsScreen';
import CustomTabBar from './components/TabIcon';
import AppLogo from './components/AppLogo';
import SignInScreen from '../screens/LoginScreen';

const AppNavigator = ({
  setIsAuthenticated,
  isGuestLogin,
  setIsGuestLogin,
  isAuthenticated,
}: any) => {
  const Stack = createBottomTabNavigator();
  const {GluedInBridge} = NativeModules;

  const onGluedInLaunchPress = async () => {
    try {
      GluedInBridge.launchSDK((error: any, result: any) => {
        if (error) {
          console.error('Error during launchSDK:', error);
        } else {
          console.log('LaunchSDK result:', result);
        }
      });
      console.error('No user data found');
    } catch (error) {
      console.error('Error in onGluedInLaunchPress:', error);
    }
  };

  return (
    <Stack.Navigator
      tabBar={props => (
        <CustomTabBar
          isGuestLogin={isGuestLogin}
          setIsGuestLogin={setIsGuestLogin}
          isAuthenticated={isAuthenticated}
          setIsAuthenticated={setIsAuthenticated}
          {...props}
        />
      )}>
      <Stack.Screen
        name="Home"
        options={{
          headerTitle: props => <AppLogo />,
          headerTitleAlign: 'center',
          headerStyle: {
            borderBottomWidth: 0,
            elevation: 0,
            shadowOpacity: 0,
          },
        }}
        component={HomeScreen}
      />

      <Stack.Screen
        name="ProductDetails"
        options={({navigation}) => ({
          headerTitle: '',
          headerStyle: {
            borderBottomWidth: 0,
            elevation: 0,
            shadowOpacity: 0,
          },
          headerLeft: () => (
            <TouchableOpacity
              style={{paddingHorizontal: 16}}
              onPress={() => navigation.navigate('Home')}>
              <ArrowLeft style={{marginLeft: 2, width: 128}} size={24} />
            </TouchableOpacity>
          ),
        })}
        component={ProductDetailScreen}
      />

      <Stack.Screen
        name = "Shorts"
        component={() => {
          onGluedInLaunchPress();
          return null;
        }}
      />

      <Stack.Screen name="ProductInfo" component={ProductDetailScreen} />

      <Stack.Screen name="Login" options={{headerShown: false}}>
        {props => (
          <SignInScreen
            {...props}
            onSignIn={() => setIsAuthenticated(true)}
            isGuestLogin={isGuestLogin}
          />
        )}
      </Stack.Screen>
    </Stack.Navigator>
  );
};

export default AppNavigator;
