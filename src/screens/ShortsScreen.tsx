import React from 'react';
import {View, Text, ScrollView, Image, Dimensions, NativeModules} from 'react-native';

const {height, width} = Dimensions.get('window');

const shortsData = [
  {id: 1, title: 'Short 1'},
  {id: 2, title: 'Short 2'},
  {id: 3, title: 'Short 3'},
  {id: 4, title: 'Short 4'},
  {id: 5, title: 'Short 5'},
];

const ShortsScreen = () => {
  return (
    <ScrollView>
      <View style={{flex: 1, justifyContent: 'center', alignItems: 'center'}}>
        <Text style={{color: 'black', fontSize: 24}}>Dummy</Text>
      </View>

    </ScrollView>
  );
};

export default ShortsScreen;
