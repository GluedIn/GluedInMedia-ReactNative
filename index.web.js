import {AppRegistry} from 'react-native';
import App from './App';
import {name as appName} from './app.json'; // ensure casing matches the file name
import {render} from 'react-dom';

AppRegistry.registerComponent(appName, () => App);

const RootComponent = AppRegistry.getApplication(appName).element;
render(RootComponent, document.getElementById('root'));
