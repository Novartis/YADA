'use strict'
const merge = require('webpack-merge')
const prodEnv = require('./prod.env')

module.exports = merge(prodEnv, {
  TRACE: 'true',
  TRACE_STATE: 'true',
  TRACE_COLLAPSE: 'true',
  NODE_ENV: '"development"',
  // label is used in header, to inform developer/tester, or suppress itself in PROD
  NODE_ENV_LABEL: '"DEV"',
  YADA: {
    protocol: '"https"',
    host: '"yada-test.qdss.io"',
    port: '""',
  }
})
