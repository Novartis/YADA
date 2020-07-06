'use strict'
const merge = require('webpack-merge')
const devEnv = require('./dev.env')

module.exports = merge(devEnv, {
NODE_ENV: '"testing"',
  // label is used in header, to inform developer/tester, or suppress itself in PROD
  NODE_ENV_LABEL: '"TEST"',
  YADA: {
    protocol: '""',
    host: '""',
    port: '""'
  }
})
