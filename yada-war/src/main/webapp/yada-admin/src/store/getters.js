export default {
  loading(state) {
    return state.loading
  },
  app(state) {
    return state.app
  },
  config(state) {
    return state.config
  },
  queries(state) {
    return state.queries
  },
  tableRows(state) {
    return state
      .queries
      .map(row => ([row.QNAME,
                    row.QUERY,
                    row.COMMENTS,
                    row.DEFAULT_PARAMS,
                    row.LAST_ACCESS,
                    row.ACCESS_COUNT,
                    row.MODIFIED,
                    row.MODIFIED_BY]))
      .map(vals => Object.values(vals))
  },
  qname(state) {
    return state.qname
  }
}
