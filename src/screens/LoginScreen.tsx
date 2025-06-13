import React, {useState} from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  SafeAreaView,
  Alert,
  Image,
  StyleSheet,
  NativeModules,
} from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import {BaggageClaim, Eye, EyeOff, ShoppingBag} from 'lucide-react-native';

const SignInScreen = ({
  navigation,
  onSignIn,
  onGuestSignIn,
  isGuestLogin,
}: any) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const { GluedInBridge } = NativeModules;

  const saveUserData = async (key: string, value: any) => {
    try {
      const jsonValue = JSON.stringify(value); // Convert the value to a string
      await AsyncStorage.setItem(key, jsonValue);
      console.log('Data saved successfully');
    } catch (e) {
      console.error('Failed to save data:', e);
    }
  };

  const handleSignIn = async () => {
    if (email.trim() && password.trim()) {

      try {
        saveUserData('userInfo', {
          id: "1",
          email: email,
          name: "Amit",
          isLogin: true
        });
  
        await AsyncStorage.setItem('userToken', 'dummy-token');
        // Navigate first
        if (isGuestLogin) {
          navigation.navigate('Home');
        }

        GluedInBridge.performLogin(email, password, (error: any, result: any) => {
          if (error) {
            console.error('Error during performLogin:', error);
          } else {
            console.log('PerformLogin result:', result);
             // Then update auth state
            onSignIn();
          }
        });
      } catch (error) {
        console.error('Error storing user token:', error);
        Alert.alert('Error', 'Something went wrong. Please try again.');
      }
    } else {
      Alert.alert('Missing Fields', 'Please enter both email and password.');
    }
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <View style={styles.container}>
        {/* Sign In Text */}
        {!isGuestLogin && <Text style={styles.signInText}>Sign In</Text>}

        <View style={styles.centerContent}>
          {/* Logo */}
          <View style={styles.logoContainer}>
            <Text style={styles.logoText}>
              F<ShoppingBag color={'#3b82f6'} size={24} />
              sn
            </Text>
          </View>

          {/* Email Input */}
          <View style={styles.inputContainer}>
            <Text style={styles.inputLabel}>Email Address / Username</Text>
            <TextInput
              placeholder="johndoe@gmail.com"
              value={email}
              onChangeText={setEmail}
              style={styles.textInput}
              placeholderTextColor="#9CA3AF"
            />
          </View>

          {/* Password Input */}
          <View style={styles.passwordContainer}>
            <Text style={styles.inputLabel}>Password</Text>
            <TextInput
              placeholder="********"
              value={password}
              onChangeText={setPassword}
              secureTextEntry={!showPassword}
              style={styles.passwordInput}
              placeholderTextColor="#9CA3AF"
            />
            <TouchableOpacity
              style={styles.eyeIconContainer}
              onPress={() => setShowPassword(!showPassword)}>
              {showPassword ? (
                <EyeOff color="#6B7280" size={20} />
              ) : (
                <Eye color="#6B7280" size={20} />
              )}
            </TouchableOpacity>
          </View>

          {/* Forgot Password */}
          <TouchableOpacity style={styles.forgotPasswordContainer}>
            <Text style={styles.forgotPasswordText}>Forgot Password?</Text>
          </TouchableOpacity>
        </View>

        {/* Sign In Button */}
        <View style={styles.buttonContainer}>
          <TouchableOpacity style={styles.signInButton} onPress={handleSignIn}>
            <Text style={styles.signInButtonText}>Sign In</Text>
          </TouchableOpacity>

          {!isGuestLogin && (
            <View style={styles.guestLoginContainer}>
              <Text style={styles.guestLoginText}>Don't have an account? </Text>
              <TouchableOpacity onPress={() => onGuestSignIn()}>
                <Text style={styles.guestLoginButtonText}>Guest Login</Text>
              </TouchableOpacity>
            </View>
          )}
        </View>
      </View>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: 'white',
  },
  container: {
    flex: 1,
    paddingHorizontal: 24,
    justifyContent: 'center',
  },
  signInText: {
    fontSize: 20,
    color: '#3b82f6',
    fontWeight: '600',
    position: 'absolute',
    top: 16,
    left: 16,
    textAlign: 'center',
    marginBottom: 24,
  },
  centerContent: {
    marginBottom: 'auto',
    marginTop: 'auto',
  },
  logoContainer: {
    alignItems: 'center',
    marginBottom: 32,
  },
  logoText: {
    fontSize: 36,
    fontWeight: '100',
    fontFamily: 'monospace',
    flexDirection: 'row',
    alignItems: 'flex-end',
  },
  inputContainer: {
    marginBottom: 16,
    alignItems: 'flex-start',
  },
  inputLabel: {
    marginBottom: 4,
    fontWeight: 'bold',
  },
  textInput: {
    borderWidth: 1,
    width: '100%',
    borderColor: '#d1d5db',
    borderRadius: 8,
    paddingHorizontal: 16,
    paddingVertical: 12,
    color: 'black',
  },
  passwordContainer: {
    marginBottom: 8,
    alignItems: 'flex-start',
    position: 'relative',
  },
  passwordInput: {
    borderWidth: 1,
    width: '100%',
    borderColor: '#d1d5db',
    borderRadius: 8,
    paddingHorizontal: 16,
    paddingVertical: 12,
    paddingRight: 48,
    color: 'black',
  },
  eyeIconContainer: {
    position: 'absolute',
    right: 16,
    top: 40,
  },
  forgotPasswordContainer: {
    marginBottom: 16,
    alignSelf: 'flex-end',
  },
  forgotPasswordText: {
    color: '#1d4ed8',
    textAlign: 'right',
  },
  buttonContainer: {
    marginBottom: 40,
    marginLeft: 'auto',
    marginRight: 'auto',
    alignItems: 'center',
  },
  signInButton: {
    backgroundColor: '#2563eb',
    width: 160,
    borderRadius: 999,
    paddingVertical: 16,
    alignItems: 'center',
    marginBottom: 16,
  },
  signInButtonText: {
    color: 'white',
    fontWeight: 'bold',
    fontSize: 16,
  },
  guestLoginContainer: {
    flexDirection: 'row',
    justifyContent: 'center',
  },
  guestLoginText: {
    color: '#4b5563',
  },
  guestLoginButtonText: {
    color: '#2563eb',
    fontWeight: 'bold',
  },
});

export default SignInScreen;