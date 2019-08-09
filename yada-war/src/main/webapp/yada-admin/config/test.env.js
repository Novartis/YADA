'use strict'
const merge = require('webpack-merge')
const devEnv = require('./dev.env')

module.exports = merge(devEnv, {
NODE_ENV: '"development"',
  // label is used in header, to inform developer/tester, or suppress itself in PROD
  NODE_ENV_LABEL: '"TEST"',
  YADA: {
    protocol: '"https"',
    host: '"yada-test.qdss.io"',
    port: '""'
  }
})
