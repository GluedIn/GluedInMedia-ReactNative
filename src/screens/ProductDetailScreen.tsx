import {
  Image,
  SafeAreaView,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
  NativeModules,
} from 'react-native';
const {GluedInBridge} = NativeModules;
import React, {useEffect, useState} from 'react';
import {
  FlatList,
  ImageBackground,
  ScrollView,
  NativeEventEmitter,
} from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import {ChallengeInfo, ChallengeResponse} from './ChallengeModel';
import Config from '../constants/Config';
import Icon from 'react-native-vector-icons/Ionicons';

const ProductDetailScreen = ({navigation}: any) => {
  const [widgetData, setWidgetData] = useState<any[]>([]); // State to store widget data
  const [isCreatorVisible, setIsCreatorVisible] = useState(false); // State for creator visibility
  const [isLeaderBoardVisible, setIsLeaderBoardVisible] = useState(false); // State for creator visibility
  const [isRewardVisible, setIsRewardVisible] = useState(false); // State for creator visibility

  const [challangeInfo, setChallangeInfo] = useState<ChallengeInfo | null>(
    null,
  );

  const [productDetail, setProductDetail] = useState(null);

  var widgetResponse: ChallengeResponse;
  var productId = 0;

  useEffect(() => {
    console.log('useEffect for GluedInBridge called');

    const fetchData = async () => {
      const data = await getUserData('productDetailInfo');
      setProductDetail(data); // Update state with the fetched data
    };

    fetchData(); // Call the async function

    console.log('Product item detail ', productDetail);
    if (
      !GluedInBridge ||
      typeof GluedInBridge.initWithUserInfo !== 'function'
    ) {
      console.error('GluedInBridge is not properly initialized.');
      return;
    }

    GluedInBridge.initWithUserInfo(
      Config.API_KEY,
      Config.SECRET_KEY,
      Config.DEFAULT_EMAIl,
      Config.DEFAULT_PASSWORD,
      Config.DEFAULT_FULLNAME,
      (error: any, result: any) => {
        if (error) {
          console.error('Error during the init:', error);
        } else {
          GluedInBridge.widgetDetailWithFeed(
            Config.ASSET_ID,
            (error: any, result: any) => {
              if (result && result.WidgetResponse) {
                widgetResponse = result.WidgetResponse;
                const creatorEnabled =
                  widgetResponse.result?.creatorEnabled ?? false;
                const LeaderBoardEnabled =
                  widgetResponse.result?.challengeInfo?.leaderboardEnabled ??
                  false;
                const challangeInfoData =
                  widgetResponse.result?.challengeInfo ?? null;
                setChallangeInfo(challangeInfoData);
                setIsCreatorVisible(creatorEnabled); // Set state for button visibility
                setIsLeaderBoardVisible(LeaderBoardEnabled);

                const rewardEnabled = result.isRewardEnable ?? false;
                setIsRewardVisible(rewardEnabled);

                if (widgetResponse.result.widgetEnabled) {
                  const feedModelResponse = result.FeedDataModel.result;
                  if (feedModelResponse.length > 0) {
                    setWidgetData(feedModelResponse);
                  }
                } else {
                  console.error(
                    'widgetEnabled is false or not available:',
                    widgetResponse.result.widgetEnabled,
                  );
                }
              }
              if (error) {
                console.error('Error during the widgetDetailWithFeed:', error);
              } else {
              }
            },
          );
          console.log('PerformLogin result', result);
        }
      },
    );
  }, []);

  const getUserData = async (key: string) => {
    try {
      const jsonValue = await AsyncStorage.getItem(key);
      return jsonValue != null ? JSON.parse(jsonValue) : null; // Parse the string back to JSON
    } catch (e) {
      console.error('Failed to fetch data:', e);
    }
  };

  const onCallSubFeed = async (item: any) => {
    GluedInBridge.launchSDKFromMicrocommunity(
      Config.ASSET_ID,
      Config.ASSET_NAME,
      Config.ASSET_DISCOUNT_PRICE,
      Config.ASSET_IMAGE_URL,
      Config.ASSET_DISCOUNT_END_DATE,
      Config.ASSET_DISCOUNT_START_DATE,
      Config.ASSET_IMAGE_URL,
      Config.ASSET_MRP,
      Config.ASSET_SHOPPABLE_LINK,
      Config.ASSET_CURRENCY_SYMBOL,
      item.topicId,
      (error: any, result: any) => {
        if (error) {
          console.log('onCallSubFeed result', error);
        } else {
          console.log('onCallSubFeed result', result);
        }
      },
    );
  };

  const onCallLeaderBoard = async () => {
    GluedInBridge.launchSDKFromLeaderboard(
      Config.ASSET_ID,
      challangeInfo,
      (error: any, result: any) => {
        if (error) {
          console.log('onCallSubFeed result', error);
        } else {
          console.log('onCallSubFeed result', result);
        }
      },
    );
  };

  const onCallReward = async () => {
    GluedInBridge.launchSDKFromReward(
      Config.ASSET_ID,
      challangeInfo,
      (error: any, result: any) => {
        if (error) {
          console.log('onCallSubFeed result', error);
        } else {
          console.log('onCallSubFeed result', result);
        }
      },
    );
  };

  const onCallCreator = async () => {
    console.log('microcomminity data ', widgetData[0]);
    console.log('microcomminity data length', widgetData.length);
    var item: any = widgetData[0];
    GluedInBridge.launchSDKWithCreator(
      Config.ASSET_ID,
      item.title,
      12,
      'https://assets.gluedin.io/hashtag/fileImage/hashtag_1736317874073.jpg',
      '2025-07-24',
      '2025-01-08',
      'https://www.amazon.in/gp/buyagain/ref=pd_hp_d_atf_rp_1?ie=',
      15,
      'https://www.amazon.in/gp/buyagain/ref=pd_hp_d_atf_rp_1?ie=',
      '$',
      'newyearChallenge',
      'false',
      (error: any, result: any) => {
        if (error) {
          console.log('onCallSubFeed result', error);
        } else {
          console.log('onCallSubFeed result', result);
        }
      },
    );
  };

  const onGluedInLaunchPress = async () => {
    try {
      console.log('Fetching user data in onGluedInLaunchPress...');

      // Get user data
      const data = await getUserData('userInfo');
      console.log('Raw user data:', data); // Log the raw data to check its format

      if (data) {
        let parsedData;
        // Check if the data is a valid JSON string
        if (typeof data === 'string') {
          try {
            parsedData = JSON.parse(data); // Parse the string into JSON
          } catch (parseError) {
            console.error('Error parsing user data as JSON:', parseError);
            return; // Exit if parsing fails
          }
        } else {
          parsedData = data; // If not a string, assume it's already an object
        }
        const {email, isLogin} = parsedData; // Destructure fields
        GluedInBridge.launchSDK((error: any, result: any) => {
          if (error) {
            console.error('Error during launchSDK:', error);
          } else {
            console.log('LaunchSDK result:', result);
          }
        });
      } else {
        console.error('No user data found');
      }
    } catch (error) {
      console.error('Error in onGluedInLaunchPress:', error);
    }
  };

  const eventEmitter = new NativeEventEmitter(GluedInBridge);

  useEffect(() => {
    const eventSignListener = eventEmitter.addListener(
      'onSignInClick',
      event => {
        console.log('here is message from callback ===>>>> ', event);
        //useNavigation.navigate('Login')
      },
    );
    const eventSignUpListener = eventEmitter.addListener(
      'onSignUpClick',
      event => {
        console.log('here is message from callback ===>>>> ', event);
        //navigation.navigate('SignUp')
      },
    );
    return () => {
      eventSignListener.remove();
      eventSignUpListener.remove();
    };
  }, []);

  const renderImageItem = ({item}: {item: any}) => (
    <View style={{alignItems: 'center'}}>
      <TouchableOpacity onPress={() => onCallSubFeed(item)}>
        <Image
          source={{uri: item.thumbnailUrl}}
          style={styles.widgetImage}
          resizeMode="cover"
        />
      </TouchableOpacity>
    </View>
  );

  const handlePlay = () => {
    console.log('Custom icon button pressed');
  };

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView
        bounces={true}
        contentContainerStyle={{paddingBottom: 13}}
        style={styles.detailContainer}>
        <View style={styles.content}>
          <View style={styles.videoContainer}>
            <TouchableOpacity style={styles.playButton} onPress={handlePlay}>
              <Image
                source={require('../../assets/images/play-circle.png')}
                style={[styles.customIcon]}
                resizeMode="contain"
              />
            </TouchableOpacity>
          </View>

          {isCreatorVisible && (
            <TouchableOpacity
              style={styles.participateButton}
              onPress={() => onCallCreator()}>
              <View style={styles.textContainer}>
                <Text style={styles.participateButtonText}>
                  Participate in #newyearChallenge
                </Text>
                <Text style={styles.participateButtonText}>
                  Upload videos and earn reward points
                </Text>
              </View>
              <Image
                source={require('../../assets/images/camera.png')}
                style={styles.participateImage}
              />
            </TouchableOpacity>
          )}

          <View style={styles.relatedContainer}>
            <Text style={styles.relatedTitle}>Related Shorts</Text>
          </View>
          <View style={styles.productDetailView}>
            {widgetData.length > 0 ? (
              <FlatList
                data={widgetData}
                renderItem={renderImageItem}
                horizontal={true} // Enable horizontal scrolling
                keyExtractor={(item, index) => index.toString()}
                showsHorizontalScrollIndicator={false}
              />
            ) : (
              <Text style={styles.loadingText}>Loading widget details...</Text>
            )}
          </View>

          {isLeaderBoardVisible && (
            <TouchableOpacity
              style={styles.buttonView}
              onPress={() => onCallLeaderBoard()}>
              <Text
                style={{
                  backgroundColor: '#FFF',
                  fontSize: 16,
                  fontWeight: 'medium',
                  borderColor: '#0033FF',
                  color: '#0033FF',
                }}>
                View Leaderboard
              </Text>
            </TouchableOpacity>
          )}

          {isRewardVisible && (
            <TouchableOpacity
              style={styles.buttonView}
              onPress={() => onCallReward()}>
              <Text
                style={{
                  backgroundColor: '#FFF',
                  fontSize: 16,
                  fontWeight: 'medium',
                  borderColor: '#0033FF',
                  color: '#0033FF',
                }}>
                View Rewards
              </Text>
            </TouchableOpacity>
          )}
        </View>
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: 'white',
  },
  content: {
    flex: 1,
  },
  videoContainer: {
    height: 200,
    borderRadius: 8,
    backgroundColor: '#e0e0e0',
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 16,
    marginHorizontal: 16,
  },
  playButton: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: '#000',
    justifyContent: 'center',
    alignItems: 'center',
  },
  customIcon: {
    width: 40,
    height: 40,
  },
  //   image: {
  //     width: '100%',
  //     height: 226,
  //     backgroundColor: '#E8E8E8',
  //     borderRadius: 4,
  //   },
  relatedContainer: {
    marginTop: 16,
    marginHorizontal: 16,
  },
  relatedTitle: {
    fontSize: 16,
    marginBottom: 0,
  },
  widgetImage: {
    padding: 2,
    backgroundColor: '#f9f9f9',
    marginBottom: 12,
    borderRadius: 6,
    width: 99,
    height: 176,
  },
  detailContainer: {
    backgroundColor: '#ffffff',
  },
  productDetailView: {
    marginTop: 16,
    marginLeft: 16,
  },
  reviewText: {
    fontSize: 18,
    color: '#000000',
  },
  loadingText: {
    textAlign: 'center',
    fontSize: 16,
    color: '#666',
  },
  participateButton: {
    backgroundColor: '#0033FF',
    marginHorizontal: 16,
    marginTop: 20,
    borderRadius: 8,
    padding: 16,
    flexDirection: 'row', // ✅ Align text and image in a row
    alignItems: 'center',
    justifyContent: 'space-between', // ✅ Push text to left and image to right
    paddingRight: 16, // ✅ Prevents image from cutting at the right
  },
  textContainer: {
    flex: 1, // ✅ Ensures text takes available space
  },
  participateButtonText: {
    color: '#FFFFFF',
    fontSize: 14,
    textAlign: 'left', // ✅ Align text to the left
  },
  participateImage: {
    width: 30, // ✅ Set fixed width to prevent cutting
    height: 30, // ✅ Set fixed height
    resizeMode: 'contain', // ✅ Ensures image scales properly
    flexShrink: 0, // ✅ Prevents image from shrinking
  },
  buttonView: {
    borderColor: '#0033FF',
    padding: 10,
    marginTop: 16,
    marginLeft: 16,
    marginRight: 16,
    borderRadius: 8,
    borderWidth: 1,
    alignItems: 'center',
  },
});

export default ProductDetailScreen;
