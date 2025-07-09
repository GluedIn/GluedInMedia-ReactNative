import {
  Image,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
  NativeModules,
  FlatList,
  ImageBackground,
  Alert,
  Pressable
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Search } from 'lucide-react-native';
import { useEffect, useState } from 'react';
import { RailData } from './RailData';
import Config from '../constants/Config';
import { Platform } from 'react-native';

const { GluedInBridge, NavigationModule } = NativeModules;

const HomeScreen = ({ navigation }: any) => {
  // const videoRailID = 'a9d89921-c9b3-427d-9757-ad7629f3cb33';
  // const seriesRailID = '804108b8-7a80-4c71-9882-794c6ae2ce94';

  var RailDataItems: RailData;
  const [railData, setRailData] = useState<any[]>([]); // State to store widget data

  var SeriesRailItems: RailData;
  const [seriesRailData, setSeriesRailData] = useState<any[]>([]); // State to store widget data

  const fetchRailData = () => {
    // Add your logic here (e.g., API call, state setup)
    if (Platform.OS === 'ios') {
      GluedInBridge.getTrandingRailResult(
        Config.API_KEY,
        Config.SECRET_KEY,
        Config.VIDEO_RAILID,
        (error: any, result: any) => {
          if (error) {
            console.error('Error during the widgetDetailWithFeed:', error);
          } else {
            if (result && result.railResponse) {
              const RailDataItems = result.railResponse.result.itemList;
              setRailData(RailDataItems);
            }
          }
        },
      );
    }
  };

  const fetchEpisodes = () => {
    // Add your logic here (e.g., API call, state setup)
    if (Platform.OS === 'ios') {
      GluedInBridge.getTrandingRailResult(
        Config.API_KEY,
        Config.SECRET_KEY,
        Config.SERIES_RAILID,
        (error: any, result: any) => {
          if (error) {
            console.error('Error during the widgetDetailWithFeed:', error);
          } else {
            if (result && result.railResponse) {
              SeriesRailItems = result.railResponse.result.itemList;
              setSeriesRailData(SeriesRailItems);
            }
          }
        },
      );
    } else {
      fetchSeriesAndRailDataFromAndroidSDK();
    }
  };

  const fetchSeriesAndRailDataFromAndroidSDK = async () => {
    try {
      const sdkInit = await NavigationModule.validateGluedInSDKSilently(Config.API_KEY,
        Config.SECRET_KEY,
        Config.BASE_URL,
        Config.DEFAULT_EMAIl,
        Config.DEFAULT_PASSWORD,
        Config.DEFAULT_FULLNAME,
        Config.DEFAULT_PROFILEPIC);

      if (sdkInit == 'success') {
        const seriesRaw = await NavigationModule.fetchRailDataFromAndroidSDK(
          Config.SERIES_RAILID);

        const videoRailRaw = await NavigationModule.fetchRailDataFromAndroidSDK(
          Config.VIDEO_RAILID);


        console.log('Native Series JSON response:', seriesRaw);
        const parsed = JSON.parse(seriesRaw);
        const SeriesDataItems = parsed?.result.itemList
        setSeriesRailData(SeriesDataItems);

        console.log('Native Rail JSON response:', videoRailRaw);
        const parsedRail = JSON.parse(videoRailRaw);
        const RailDataItems = parsedRail?.result.itemList
        setRailData(RailDataItems);
      }
    } catch (error) {
      console.error('Error from native module:', error);
    }
  };

  useEffect(() => {
    fetchRailData();
    fetchEpisodes();
  }, []);

  const renderImageItem = ({
    item,
    index,
    type,
    railList,
  }: {
    item: any;
    index: number;
    type: 'videos' | 'series';
    railList: any[];
  }) => (
    <View style={{ margin: 1, alignItems: 'center' }}>
      <Text> {item.length} </Text>
      <Pressable
        onPress={() => onCallSubFeed(item, index, type, railList)}>
        <Image
          source={{ uri: item.thumbnail }}
          style={styles.widgetImage}
          resizeMode="cover"
        />
      </Pressable>
    </View>
  );

  const onCallSubFeed = async (
    item: any,
    index: number,
    type: 'videos' | 'series',
    feedRailData: any[],
  ) => {
    if (Platform.OS === 'ios') {
      GluedInBridge.userDidTapOnFeed(
        index,
        type,
        item,
        feedRailData,
        Config.API_KEY,
        Config.SECRET_KEY,
        Config.DEFAULT_EMAIl,
        Config.DEFAULT_PASSWORD,
        Config.DEFAULT_FULLNAME,
        Config.PERSONA_TYPE,
        (error: any, result: any) => {
          if (error) {
            console.log('onCallSubFeed result', error);
          } else {
            console.log('onCallSubFeed result', result);
          }
        },
      );
    } else {
      console.log('GludIn onCallSubFeed result', item);
      if ("series" == type) {
        NavigationModule.launchSeriesFeed(Config.API_KEY,
          Config.SECRET_KEY,
          Config.BASE_URL,
          Config.DEFAULT_EMAIl,
          Config.DEFAULT_PASSWORD,
          Config.DEFAULT_FULLNAME,
          Config.DEFAULT_PROFILEPIC,
          Config.PERSONA_TYPE,
          item.assetId
        );
      } else {
        NavigationModule.launchCarouselFeed(Config.API_KEY,
          Config.SECRET_KEY,
          Config.BASE_URL,
          Config.DEFAULT_EMAIl,
          Config.DEFAULT_PASSWORD,
          Config.DEFAULT_FULLNAME,
          Config.DEFAULT_PROFILEPIC,
          index,
          feedRailData
        );
      }
    }
  };

  const handleWatchNow = () => {
    navigation.navigate('ProductDetails');
  };

  return (
    <ScrollView showsVerticalScrollIndicator={false} style={styles.scrollArea}>
      <View style={styles.searchContainer}>
        <View style={styles.searchInputContainer}>
          <TextInput
            placeholder="Search..."
            style={styles.searchInput}
            placeholderTextColor="#777777"
          />
          <Search width={20} height={20} color={'#777777'} />
        </View>
      </View>

      <View style={styles.bannerContainer}>
        <Text style={styles.sectionTitle}>Top Release</Text>

        <View style={styles.card}>
          <ImageBackground
            source={require('../../assets/images/banner01.jpg')}
            style={styles.image}
            imageStyle={styles.imageStyle}>
            <View style={styles.overlayContent}>
              <Text style={styles.title}>The Rising Voice</Text>
              <TouchableOpacity style={styles.button} onPress={handleWatchNow}>
                <Text style={styles.buttonText}>Watch Now</Text>
              </TouchableOpacity>
            </View>
          </ImageBackground>
        </View>
      </View>

      <View style={styles.sectionContainer}>
        <Text style={styles.sectionTitle}>Trending Fashion</Text>
        {railData != null && railData.length > 0 ? (
          <FlatList
            showsHorizontalScrollIndicator={false}
            data={railData}
            renderItem={({ item, index }) =>
              renderImageItem({
                item,
                index,
                type: 'videos',
                railList: railData,
              })
            } // ✅ Pass index
            horizontal={true} // Enable horizontal scrolling
            keyExtractor={(item, index) => index.toString()}
          />
        ) : (
          <Text style={styles.loadingText}>Loading widget details...</Text>
        )}
      </View>

      <View style={styles.sectionContainer}>
        <Text style={styles.sectionTitle}>Non Stop Action</Text>
        <FlatList
          data={BOXES}
          horizontal
          keyExtractor={(item) => item.id}
          renderItem={() => <View style={styles.box} />}
          showsHorizontalScrollIndicator={false}
        />
      </View>

      <View style={styles.sectionContainer}>
        <Text style={styles.sectionTitle}>Latest Micro Drama</Text>
        {seriesRailData != null && seriesRailData.length > 0 ? (
          <FlatList
            showsHorizontalScrollIndicator={false}
            data={seriesRailData}
            renderItem={({ item, index }) =>
              renderImageItem({
                item,
                index,
                type: 'series',
                railList: seriesRailData,
              })
            } // ✅ Pass index
            horizontal={true} // Enable horizontal scrolling
            keyExtractor={(item, index) => index.toString()}
          />
        ) : (
          <Text style={styles.loadingText}>Loading widget details...</Text>
        )}
      </View>

      <View style={styles.sectionContainer}>
        <Text style={styles.sectionTitle}>Popular in Comedy</Text>
        <FlatList
          data={BOXES}
          horizontal
          keyExtractor={(item) => item.id}
          renderItem={({ item, index }) =>
            renderImageItem({
              item,
              index,
              type: 'series',
              railList: seriesRailData,
            })
          }
          showsHorizontalScrollIndicator={false}
        />
      </View>

      <View style={styles.sectionContainer}>
        <Text style={styles.sectionTitle}>Latest Movies Just For You</Text>
        <FlatList
          data={BOXES}
          horizontal
          keyExtractor={(item) => item.id}
          renderItem={({ item, index }) =>
            renderImageItem({
              item,
              index,
              type: 'series',
              railList: seriesRailData,
            })
          }
          showsHorizontalScrollIndicator={false}
        />
      </View>

    </ScrollView>
  );
};

const BOXES = Array.from({ length: 10 }, (_, i) => ({ id: i.toString() }));

const styles = StyleSheet.create({

  box: {
    width: 170,
    height: 106,
    backgroundColor: '#E8E8E8',
    marginRight: 4,
    marginTop: 12,
    marginBottom: 12,
    borderRadius: 4,
  },

  scrollArea: {
    flex: 1,
    backgroundColor: 'white',
  },
  searchContainer: {
    flex: 1,
    paddingTop: 8,
    paddingLeft: 16,
    paddingRight: 16,
    paddingBottom: 8,
    backgroundColor: 'white',
  },
  searchInputContainer: {
    padding: 4,
    paddingHorizontal: 12,
    position: 'relative',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: '#E8E8E8',
    borderRadius: 8,
  },
  searchInput: {
    padding: 8,
    flex: 1,
    color: 'black',
  },
  bannerContainer: {
    paddingLeft: 16,
    paddingRight: 16,
  },
  sectionContainer: {
    paddingHorizontal: 0,
    paddingLeft: 16,
    marginTop: 12
  },
  sectionTitle: {
    color: '#2B2B2B',
    fontSize: 14,
    fontFamily: 'SF UI Display-Semibold',
    fontWeight: 'semibold',
  },
  card: {
    borderRadius: 12,
    overflow: 'hidden',
    marginTop: 12,
  },
  image: {
    height: 180,
    justifyContent: 'center',
    alignItems: 'center',
  },
  imageStyle: {
    borderRadius: 12,
  },
  overlayContent: {
    position: 'absolute',
    alignItems: 'center',
  },
  title: {
    fontSize: 16,
    color: '#fff',
    fontWeight: '600',
    marginBottom: 10,
  },
  button: {
    backgroundColor: '#fff',
    paddingVertical: 6,
    paddingHorizontal: 16,
    borderRadius: 8,
  },
  buttonText: {
    color: 'blue',
    fontWeight: 'semibold',
    fontSize: 10,
  },
  trendingItem: {
    marginRight: 16,
  },
  widgetImage: {
    padding: 1,
    backgroundColor: '#E8E8E8',
    marginBottom: 5,
    borderRadius: 5,
    width: 117,
    height: 200,
  },
  loadingText: {
    textAlign: 'center',
    fontSize: 16,
    color: '#666',
  },
});

export default HomeScreen;
