'use strict'
const merge = require('webpack-merge')
const prodEnv = require('./prod.env')

module.exports = merge(prodEnv, {
  TRACE : 'false',
  TRACE_STATE : 'false',
  TRACE_COLLAPSE : 'true',
  NODE_ENV: '"development"',
  NODE_ENV_LABEL: '"DEV"',
  YADA_BASEURL : '""',
  YADA_ADMIN_BASEURL : '""',
})
