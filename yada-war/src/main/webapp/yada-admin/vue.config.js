module.exports = {
  baseUrl: process.env.NODE_ENV === 'production' || process.env.NODE_ENV === 'testing'
    ? '/yada-admin/'
    : '/'
}
