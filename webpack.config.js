const path = require('path')
const HtmlWebPackPlugin = require("html-webpack-plugin");

const indexPage = new HtmlWebPackPlugin({
  template: path.resolve(__dirname, 'src/main/react/index.html'),
  filename: "./index.html",
  chunks: ["main"]
});

const formPage = new HtmlWebPackPlugin({
  template: path.resolve(__dirname, 'src/main/react/index.html'),
  filename: "./form.html",
  chunks: ["form"]
});

module.exports = {

  entry: {
    main: path.resolve(__dirname, 'src/main/react/index'),
    form: path.resolve(__dirname, 'src/main/react/form')
  },

  output: {
    path: path.resolve(__dirname, 'src/main/webapp')
  },

  module: {
    rules: [
      {
        test: /\.js|jsx$/,
        exclude: /node_modules\/(?!(@flock-eco)\/).*/,
        use: {
          loader: "babel-loader",
          options: {
            "plugins": [
              "@babel/plugin-proposal-class-properties"
            ],
            "presets": [
              "@babel/preset-env",
              "@babel/preset-react"
            ]
          }
        }
      }
    ]
  },

  plugins: [indexPage,formPage],

  devServer: {
    port: 3000,
    proxy: {
      '/api/**': 'http://localhost:8080',
      '/tasks/**': 'http://localhost:8080',
      '/login**': 'http://localhost:8080',
      '/login/**': 'http://localhost:8080',
      '/oauth2/**': 'http://localhost:8080',
      '/configuration': 'http://localhost:8080',
      '/_ah/**': 'http://localhost:8080',
    }
  }

};
