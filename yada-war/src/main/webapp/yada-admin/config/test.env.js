'use strict'
const merge = require('webpack-merge')
const devEnv = require('./dev.env')

module.exports = merge(devEnv, {
  TRACE : 'false',
  TRACE_STATE : 'false',
  TRACE_COLLAPSE : 'true',
  NODE_ENV: '"testing"',
  NODE_ENV_LABEL: '"TEST"',
  YADA_BASEURL : '""',
  YADA_ADMIN_BASEURL : '""',
})
