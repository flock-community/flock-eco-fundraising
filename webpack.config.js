const path = require('path')
const webpack = require('webpack')
const HtmlWebPackPlugin = require("html-webpack-plugin");

const htmlPlugin = new HtmlWebPackPlugin({
  template: path.resolve(__dirname, 'src/main/react/index.html'),
  filename: "./index.html"
});

const replacementPlugin = new webpack.NormalModuleReplacementPlugin(
  /eco-feature-(.*)/, (resource) => {
    const ref = resource.request.replace(/eco-feature-([^\/]*)\/(.*)/, 'eco-feature-$1-jar/$2')
    resource.request = path.join(__dirname, 'target/react', ref)
  }
);

module.exports = {

  entry: [
    'whatwg-fetch',
    path.resolve(__dirname, 'src/main/react')
  ],

  output: {
    path: path.resolve(__dirname, 'src/main/webapp')
  },

  module: {
    rules: [
      {
        test: /\.js|jsx$/,
        exclude: /node_modules/,
        use: {
          loader: "babel-loader",
          options: {
            presets: [
              'env',
              'react',
              'stage-2'
            ]
          }
        }
      }
    ]
  },

  plugins: [htmlPlugin, replacementPlugin],

  devServer: {
    port: 3000,
    proxy: {
      '/api/**': 'http://localhost:8080',
      '/tasks/**': 'http://localhost:8080',
      '/login**': 'http://localhost:8080',
      '/configuration': 'http://localhost:8080',
      '/_ah/**': 'http://localhost:8080',
    }
  }

};