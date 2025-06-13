import AsyncStorage from '@react-native-async-storage/async-storage';
import {
  Bell,
  HomeIcon,
  LogOut,
  Search,
  ShoppingBag,
  User,
} from 'lucide-react-native';
import {Fragment, useState} from 'react';
import {TouchableOpacity, View} from 'react-native';
import LogoutModal from './LogoutModal';

const TabIcon = ({
  icon: Icon,
  isFocused,
  isSpecial = false,
}: {
  icon: React.ComponentType<{color: string; size: number}>;
  isFocused: boolean;
  isSpecial?: boolean;
}) => {
  if (isSpecial) {
    return <Icon color={'#fff'} size={24} />;
  }
  return <Icon color={isFocused ? '#2563EB' : '#6B7280'} size={24} />;
};

// Custom Tab Bar Component
const CustomTabBar = ({
  state,
  descriptors,
  setIsGuestLogin,
  setIsAuthenticated,
  isAuthenticated,
  navigation,
  isGuestLogin,
}: any) => {
  const [logoutModalVisible, setLogoutModalVisible] = useState(false);

  const tabIcons = [
    {name: 'Home', icon: HomeIcon},
    {name: 'Favorite', icon: Search},
    {name: 'Shorts', icon: ShoppingBag, special: true},
    {name: 'Notifications', icon: Bell},
    {name: 'Profile', icon: isAuthenticated ? LogOut : User},
  ];

  const handleProfilePress = () => {
    setLogoutModalVisible(true);
  };

  const handleLogout = async () => {
    setLogoutModalVisible(false);
    await AsyncStorage.removeItem('userToken');
    setIsAuthenticated(false);
    setIsGuestLogin(false);
  };

  return (
    <Fragment>
      {/* <View className="flex-row justify-around items-center bg-white py-3 border-t border-gray-200">
        {tabIcons.map((tab, index) => {
          const isFocused = state.index === index;
          const onPress = () => {
            if (isAuthenticated && tab.name === 'Profile') {
              handleProfilePress();
              return;
            }

            const event = navigation.emit({
              type: 'tabPress',
              target: state.routes[index].key,
            });

            if (!isFocused && !event.defaultPrevented) {
              navigation.navigate(state.routes[index].name);
            }
          };

          return (
            <TouchableOpacity
              key={tab.name}
              onPress={onPress}
              className={`${
                tab.name === 'Shorts' ? 'bg-blue-600 p-2 rounded-full' : ''
              } items-center justify-center`}>
              <TabIcon
                //@ts-ignore
                icon={tab.icon}
                isFocused={isFocused}
                isSpecial={tab.special}
              />
            </TouchableOpacity>
          );
        })}
      </View> */}
      <View
        style={{
          flexDirection: 'row',
          justifyContent: 'space-around',
          alignItems: 'center',
          backgroundColor: 'white',
          paddingVertical: 12, // Tailwind py-3 ~12px
          borderTopWidth: 1,
          borderColor: '#E5E7EB', // Tailwind gray-200
        }}>
        {tabIcons.map((tab, index) => {
          const isFocused = state.index === index;
          const onPress = () => {
            if (isAuthenticated && tab.name === 'Profile') {
              handleProfilePress();
              return;
            }
            const event = navigation.emit({
              type: 'tabPress',
              target: state.routes[index].key,
            });
            if (!isFocused && !event.defaultPrevented) {
              navigation.navigate(state.routes[index].name);
            }
          };

          return (
            <TouchableOpacity
              key={tab.name}
              onPress={onPress}
              style={[
                {alignItems: 'center', justifyContent: 'center'},
                tab.name === 'Shorts'
                  ? {
                      backgroundColor: '#2563EB', // Tailwind blue-600
                      padding: 8, // Tailwind p-2 ~8px
                      borderRadius: 9999, // rounded-full
                    }
                  : {},
              ]}>
              <TabIcon
                // @ts-ignore
                icon={tab.icon}
                isFocused={isFocused}
                isSpecial={tab.special}
              />
            </TouchableOpacity>
          );
        })}
      </View>

      <LogoutModal
        visible={logoutModalVisible}
        onCancel={() => setLogoutModalVisible(false)}
        onConfirm={handleLogout}
      />
    </Fragment>
  );
};

export default CustomTabBar;
