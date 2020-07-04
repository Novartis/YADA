module.exports = {
  publicPath: process.env.NODE_ENV === 'production' || process.env.NODE_ENV === 'testing'
    ? '/yada-admin/'
    : '/'
}
