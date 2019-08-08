import * as types from './mutation-types'

export default {
  [types.LOAD_APP]({state, commit, dispatch}, app) {
    // enable spinner
    if(!state.loading)
      commit(types.LOADING,true)
    // store the selected app name
    commit(types.SET_APP,app)

    // the queries to execute and the events to emit on response
    const selects = ['YADA select app config','YADA queries']

    // build the jsonparams
    let j = []
    selects.forEach(function(q){
      j.push({qname:q,DATA:[{APP:app}]})
    })

    // exec the queries
    this._vm.$yada.jp(j).then(function(resp) {
        let r = resp.data.RESULTSETS
        let confSelect = selects[0]
        let conf = r.map(set => set.RESULTSET)
                    .filter(conf => conf.qname == confSelect)[0]
                    .ROWS[0]

        let queriesSelect = selects[1]
        let queries = r.map(set => set.RESULTSET)
                       .filter(queries => queries.qname == queriesSelect)[0].ROWS


        commit(types.SET_CONFIG, conf)
        commit(types.SET_QUERIES, queries)
        commit(types.LOADING, false)
      }
    )
  },
  [types.LOAD_PARAMS]({state,commit,dispatch}) {
    // enable spinner
    if(!state.loading)
      commit(types.LOADING,true)
    const selects = ['YADA select protectors for target',
                     'YADA select default params for app',
                     'YADA select props like target']

    // build the jsonparams
    let j = []
    j.push({qname: selects[0],DATA:[{TARGET:state.qname}]})
    j.push({qname: selects[1],DATA:[{APP:state.app}]})
    j.push({qname: selects[2],DATA:[{TARGET:state.app}]})

    this._vm.$yada.jp(j).then(resp => {
      let r = resp.data.RESULTSETS
      let prots  = r[0].RESULTSET.ROWS
      let params = r[1].RESULTSET.ROWS
      let props  = r[2].RESULTSET.ROWS

      commit(types.SET_PARAMS, params)
      commit(types.SET_PROPS, props)
      commit(types.SET_PROTECTORS, prots)
    })
  },
  [types.ADD_APP]({commit}) { commit(types.ADD_APP) },
  [types.MOD_APP]({commit}) { commit(types.MOD_APP) },
  [types.DEL_APP]({commit}) { commit(types.DEL_APP) },
  [types.MOD_QUERY]({commit}) { commit(types.MOD_QUERY) },
  [types.ADD_QUERY]({commit}) { commit(types.ADD_QUERY) },
  [types.DEL_QUERY]({commit}) { commit(types.DEL_QUERY) }
}
