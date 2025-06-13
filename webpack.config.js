const path = require('path');
const webpack = require('webpack');

module.exports = {
  // Define the entry point of your app
  entry: path.resolve(__dirname, 'index.web.js'),

  // Set up output options
  output: {
    filename: 'bundle.js',
    path: path.resolve(__dirname, 'dist'),
  },

  // Resolve module imports
  resolve: {
    // Alias 'react-native' to 'react-native-web'
    alias: {
      'react-native$': 'react-native-web',
    },
    // Automatically resolve these extensions
    extensions: ['.web.js', '.js', '.jsx', '.json'],
  },

  // Module rules for transpiling JavaScript files
  module: {
    rules: [
      {
        test: /\.js$/,
        exclude: /node_modules/,
        use: 'babel-loader',
      },
    ],
  },

  // Plugins for environment variables and other enhancements
  plugins: [
    new webpack.DefinePlugin({
      __DEV__: JSON.stringify(process.env.NODE_ENV !== 'production'),
    }),
  ],

  //   // Development server configuration
  //   devServer: {
  //     // Serve static files from the 'public' folder
  //     contentBase: path.resolve(__dirname, 'public'),
  //     historyApiFallback: true, // for SPA routing support
  //     port: 3000, // or any port you prefer
  //     open: true, // auto-open the browser
  //   },
  devServer: {
    static: {
      directory: path.resolve(__dirname, 'public'),
    },
    historyApiFallback: true, // For SPA routing support
    port: 3000, // Use your preferred port
    open: true, // Automatically open the browser
  },
};
