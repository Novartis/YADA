import * as types from './mutation-types';

export default {
  // (s,p) means (state, payload)
  [types.SET_APP](s,p) { s.app = p },
  [types.SET_QNAME](s,p) { s.qname = p },
  [types.SET_CONFIG](s,p) { s.config = p },
  [types.SET_QUERIES](s,p) { s.queries = p },

  [types.LOADING](s,p) { s.loading = p },

  [types.ADD_APP](s,p) {  },
  [types.MOD_APP](s,p) {  },
  [types.DEL_APP](s,p) {  },

  [types.MOD_QUERY](s,p) {  },
  [types.ADD_QUERY](s,p) {  },
  [types.DEL_QUERY](s,p) {  }
}
