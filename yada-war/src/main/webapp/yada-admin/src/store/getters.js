export default {
  getLoading(state) {
    return state.loading
  },
  getApp(state) {
    return state.app
  },
  getConfig(state) {
    return state.config
  },
  getQueries(state) {
    return state.queries
  },
  getTableRows(state) {
    return state
      .queries
      .map(row => ([row.QNAME,
                    row.QUERY,
                    row.QUERY,
                    row.COMMENTS,
                    row.DEFAULT_PARAMS,
                    row.LAST_ACCESS,
                    row.ACCESS_COUNT,
                    row.MODIFIED,
                    row.MODIFIED_BY]))
  },
  getParamTableRows(state) {
    return state
      .params
      .map(row => ([row.ID,
                    row.TARGET,
                    row.NAME,
                    row.VALUE,
                    row.RULE]))
  },
  getQname(state) {
    return state.qname
  },
  getQuery(state) {
    return state.query
  }

}
