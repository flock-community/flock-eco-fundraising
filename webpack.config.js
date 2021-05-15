const ecoConfig = require("@flock-community/flock-eco-webpack");

module.exports = {
  ...ecoConfig,
  devServer: {
    ...ecoConfig.devServer,
    historyApiFallback: true,
    proxy: {
      ...ecoConfig.devServer.proxy,
      '/api/**': 'http://localhost:8080',
      '/oauth2/**': 'http://localhost:8080',
      '/login/**': 'http://localhost:8080',
      '/tasks/**': 'http://localhost:8080',
      '/login**': 'http://localhost:8080',
      '/graphql**': 'http://localhost:8080',
      '/configuration': 'http://localhost:8080',
      '/_ah/**': 'http://localhost:8080',
    },
  },
};
