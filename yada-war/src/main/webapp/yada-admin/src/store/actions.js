import * as types from './vuex-types'
import {utils} from '../mixins/utils'

const util = utils.methods
function prepareQueries(qnames,payloads) {
  let j = []
  for(let i=0; i<qnames.length;i++)
  {
    if(Array.isArray(payloads[i]))
    {
      if(payloads[i].length > 0)
        j.push({qname:qnames[i],DATA:payloads[i]})
    }
    else if (typeof payloads[i] === 'object')
    {
      j.push({qname:qnames[i],DATA:[payloads[i]]})
    }
  }
  return j
}

export default {
  [types.ACTIVATE_APPSTAB] ({state,commit,dispatch}, tab) {
    // let conf = { ACTIVE:0, APP:'', CONF:'', DESCR:'', NAME:'' }
    commit(types.SET_APP, null)
    commit(types.SET_RENAMING, false)
    commit(types.SET_CREATING, false)
    commit(types.SET_CLONING, false)
    commit(types.SET_SAVING, false)
    commit(types.SET_CONFIG, null)
    commit(types.SET_QUERIES, [])
    commit(types.SET_QNAME, null)
    commit(types.SET_QNAMEORIG, [])
    commit(types.SET_QUERY, null)
    commit(types.SET_PARAM, null)
    commit(types.SET_PARAMS, [])
    commit(types.SET_RENDEREDPARAMS, [])
    commit(types.SET_PROPS, [])
    commit(types.SET_PROTECTORS, [])

    commit(types.SET_UNSAVEDPARAMS, 0)
    // UNSAVEDCHANGES = 0 must be last
    commit(types.SET_UNSAVEDCHANGES, 0)
    dispatch(types.LOAD_APPS, {})

  },

  [types.ACTIVATE_CONFTAB] ({state,commit}, tab) {
    commit(types.SET_RENAMING, false)
    commit(types.SET_CREATING, false)
    commit(types.SET_CLONING, false)
    commit(types.SET_SAVING, false)
    commit(types.SET_LOADING, false)
    commit(types.SET_QNAME, null)
    commit(types.SET_QNAMEORIG, [])
    commit(types.SET_QUERY, null)
    commit(types.SET_PARAM, null)
    commit(types.SET_PARAMS, [])
    commit(types.SET_RENDEREDPARAMS, [])
    commit(types.SET_PROPS, [])
    commit(types.SET_PROTECTORS, [])

    commit(types.SET_UNSAVEDPARAMS, 0)
    // UNSAVEDCHANGES = 0 must be last
    commit(types.SET_UNSAVEDCHANGES, 0)
  },

  [types.ACTIVATE_QUERYLISTTAB] ({state,commit,dispatch}, tab) {
    commit(types.SET_RENAMING, false)
    commit(types.SET_CREATING, false)
    commit(types.SET_CLONING, false)
    commit(types.SET_SAVING, false)
    commit(types.SET_LOADING, false)
    commit(types.SET_QNAME, null)
    commit(types.SET_QNAMEORIG, [])
    commit(types.SET_QUERY, null)
    commit(types.SET_PARAM, null)
    commit(types.SET_PARAMS, [])
    commit(types.SET_RENDEREDPARAMS, [])
    commit(types.SET_PROPS, [])
    commit(types.SET_PROTECTORS, [])

    commit(types.SET_UNSAVEDPARAMS, 0)
    // UNSAVEDCHANGES = 0 must be last
    commit(types.SET_UNSAVEDCHANGES, 0)
  },

  [types.ACTIVATE_QUERYEDITTAB] ({state,commit}, tab) {

  },

  [types.SAVE] ({state,commit,dispatch}) {
    if(state.activeTab == 'conf-tab')
    {
      dispatch(types.SAVE_APP)
    }
    else
    {
      dispatch(types.SAVE_QUERY)
    }
  },

  [types.LOAD_APPS] ({state,commit,dispatch},{}) {
    let q = 'YADA select apps'
    return this._vm.$yada.std(q,null,{c:false})
      .then(r => {
        commit(types.SET_APPS,r.data.RESULTSET.ROWS.sort((a,b)=>{ return a.APP.localeCompare(b.APP, 'en',{sentivity: 'base'})}))
      })
  },

  [types.ADD_APP] ({state, commit, dispatch}, {}) {
    commit(types.SET_CREATING, true)
    let app  = `APP${Math.abs(util.hash(new Date().toString()))}`
    let conf = { ACTIVE:'1',APP:app,CONF:'',DESCR:'',NAME:'' }
    commit(types.SET_APP, app)
    commit(types.SET_CONFIG, conf)
  },

  [types.LOAD_APP]({state, commit, dispatch}, app) {

    commit(types.SET_LOADING, true)
    // store the selected app name
    commit(types.SET_APP,app)

    if(!!app)
    {
      // the queries to execute and the events to emit on response
      const selects = ['YADA select app config','YADA queries']

      // build the jsonparams
      let j = []
      selects.forEach((q) => {
        j.push({qname:q,DATA:[{APP:app}]})
      })

      // exec the queries
      return this._vm.$yada.jp(j).then(resp => {
          let r = resp.data.RESULTSETS
          let confSelect = selects[0]
          let conf = r.map(set => set.RESULTSET)
                      .filter(conf => conf.qname == confSelect)[0]
                      .ROWS[0]

          let queriesSelect = selects[1]
          let queries = r.map(set => set.RESULTSET)
                         .filter(queries => queries.qname == queriesSelect)[0]
                         .ROWS
                         .sort((a,b) => { return a.QNAME.localeCompare(b.QNAME, 'en',{sentivity: 'base'})})


          commit(types.SET_CONFIG, conf)
          commit(types.SET_QUERIES, queries)
          commit(types.SET_LOADING, false)
        }
      )
    }
  },

  [types.SAVE_APP]({state,commit,dispatch}) {
    commit(types.SET_SAVING,true)
    if(state.creating)
    {
      let app = state.config.APP
      let j = []
      j.push({qname:'YADA new app', DATA:[state.config]})
      j.push({qname:'YADA new app admin', DATA:[{APP:app,USERID:state.loggeduser}]})

      return this._vm.$yada.jp(j)
        .then(function(resp) {
          dispatch(types.LOAD_APP,null)
        })
        .then(function(resp) {
          dispatch(types.LOAD_APP,app)
        })
        .finally(function(resp) {
          commit(types.SET_UNSAVEDCHANGES, 0)
          commit(types.SET_SAVING,false)
          commit(types.SET_CREATING,false)
        })
    }
    else
    {
      let j = []
      let q = 'YADA update app'
      let payload = state.config
      j.push({qname:q,DATA:[payload]})
      return this._vm.$yada.jp(j)
      .then(() => {setTimeout(() => {commit(types.SET_SAVING, false)},500)})
    }
  },

  [types.LOAD_PARAMS]({state,commit,dispatch}) {
    // enable spinner
    // if(!state.loading)
    //   commit(types.SET_LOADING,true)
    const selects = ['YADA select protectors for target',
                     'YADA select default params for target',
                     'YADA select props like target'
                     ]

    // build the jsonparams
    let j = []
    selects.forEach(q => j.push({qname:q,DATA:[{target:state.qname}]}))

    this._vm.$yada.jp(j).then(resp => {
      let r = resp.data.RESULTSETS
      let prots  = r[0].RESULTSET.ROWS
      let params = r[1].RESULTSET.ROWS.sort((a,b) => {return parseInt(a.ID) - parseInt(b.ID)})
      let props  = r[2].RESULTSET.ROWS

      commit(types.SET_PARAMS, params)
      commit(types.SET_RENDEREDPARAMS, params)
      commit(types.SET_PROPS, props)
      commit(types.SET_PROTECTORS, prots)
    })
  },

  [types.RENAME_QUERY]({state,commit}) {
    commit(types.SET_RENAMING, true)
  },

  [types.CLONE_QUERY]({commit,state}) {
    commit(types.SET_CLONING,true)
    commit(types.SET_CREATING,true)
  },


  [types.MOD_QUERY]({commit,state,dispatch}) {

    let qname, query, qnames, renderedParams, params, props, prots, payloads, j, promises = []
    qnames = ['YADA update query',
      'YADA update prop',
      'YADA update protector for target']

    payloads = [
      // query
      {
        QNAME: state.qname,
        QUERY: state.query.QUERY,
        MODIFIED_BY: state.loggeduser,
        MODIFIED: new Date().toISOString().substr(0,19).replace(/T/,' '),
        COMMENTS: state.query.COMMENTS,
        APP: state.app
      },

      // prop
      state.props.map(p => {
        return {
          TARGET: state.qname,
          NAME: p.NAME,
          VALUE: p.VALUE,
          APP: state.app
        }
      }),

      // protector
      state.protectors.map(p => {
        return {
          TARGET: state.qname,
          POLICY: p.POLICY,
          QNAME: p.QNAME,
          TYPE: p.TYPE,
          APP: state.app
        }
      })
    ]

    if(state.unsavedParams > 0)
    {
      qnames.push('YADA insert default param')
      payloads.push(
        // mods to existing param
        state.renderedParams.map(p => {
          return {
            TARGET: state.qname,
            ID: p.ID,
            NAME: p.NAME,
            VALUE: p.VALUE,
            RULE: p.RULE,
            APP: state.app
          }
        })
      )
    }

    j = prepareQueries(qnames,payloads)
    let updateQuery = j.filter(q => q.qname == 'YADA update query')[0]
    let updateParams = j.filter(q => q.qname == 'YADA insert default param')[0]
    let updateProps = j.filter(q => q.qname == 'YADA update prop')[0]
    let updateProtectors = j.filter(q => q.qname == 'YADA update protector for target')[0]
    promises.push(this._vm.$yada.jp([updateQuery]))
    if(typeof updateParams !== 'undefined')
      promises.push(this._vm.$yada.jp([updateParams]))
    if(typeof updateProps !== 'undefined')
      promises.push(this._vm.$yada.jp([updateProps]))
    if(typeof updateProtectors !== 'undefined')
      promises.push(this._vm.$yada.jp([updateProtectors]))
    return Promise.all(promises)
  },
  /**
   *  See flow chart: https://drive.google.com/file/d/1Y4H8-AQLNBs0d8UZY83qfzm_DOmTh6tk/view?usp=sharing
   */
  [types.SAVE_QUERY]({commit,state,dispatch}) {
    commit(types.SET_SAVING, true)
    let qname, query, qnames, params, props, prots, payloads, j, promises = []

    qname = state.qname
    if(state.renaming || state.cloning)
    {
      // modify qname in vuex objects to reflect changes
      query = state.query
      if(state.cloning)
      {
        qname = `${state.qname} CLONE`
        let query = state.query
          query['QNAME'] = qname
          query['ACCESS_COUNT'] = 0
          query['COMMENTS'] = `Clone of ${qname.replace(/ CLONE$/,'')} at ${new Date().toISOString().substr(0,19).replace(/T/,' ')}

          ${query.COMMENTS}`
      }
      let hasParams = typeof state.params !== 'undefined' && state.params !== null && state.params.length > 0
      let hasProps = typeof state.props !== 'undefined' && state.props !== null && state.props.length > 0
      let hasProtectors = typeof state.protectors !== 'undefined' && state.protectors !== null && state.protectors.length > 0
      if(hasParams)
      {
        params = state.params.map(p => {p['TARGET'] = qname; p['APP'] = state.app; return p})
      }
      if(hasProps)
      {
        props = state.props.map(p => {p['TARGET'] = qname, p['APP'] = state.app; return p})
      }
      if(hasProtectors)
      {
        prots = state.protectors.map(p => {
          p['TARGET'] = qname;
          p['QNAME'] = state.protectors[0].qname
          return p
        })
      }
      commit(types.SET_QNAME,qname)
      commit(types.SET_QUERY,query)
      commit(types.SET_PARAMS,params)
      commit(types.SET_PROPS,props)
      commit(types.SET_PROTECTORS,prots)
    }

    if(state.creating || state.renaming || state.cloning)
    {
      // prepare query payloads
      qnames = [
        'YADA new query',
        'YADA insert default param',
      ]
      payloads = [
        state.query,
        state.params
      ]

      if(state.renaming || state.cloning)
      {
        // include properties and protectors if renaming or cloning
        qnames.push('YADA insert prop')
        qnames.push('YADA insert protector for target')
        payloads.push(state.props)
        payloads.push(state.protectors)
      }

      // prepare jsonparams
      j = prepareQueries(qnames,payloads)
      let insertQuery = j.filter(q => q.qname == 'YADA new query')[0]
      let insertParams = j.filter(q => q.qname == 'YADA insert default param')[0]
      let insertProps = j.filter(q => q.qname == 'YADA insert prop')[0]
      let insertProtectors = j.filter(q => q.qname == 'YADA insert protector for target')[0]
      return dispatch(types.CHECK_UNIQ, qname)
      .then((r) => {
        if(r.data.RESULTSET.ROWS[0].count == 0)
        {
          promises.push(this._vm.$yada.jp([insertQuery]))
          if(typeof insertParams !== 'undefined')
            promises.push(this._vm.$yada.jp([insertParams]))
          if(typeof insertProps !== 'undefined')
            promises.push(this._vm.$yada.jp([insertProps]))
          if(typeof insertProtectors !== 'undefined')
            promises.push(this._vm.$yada.jp([insertProtectors]))
          // execute inserts
          return Promise.all(promises)
          .then(() => {
            if(state.renaming)
            {
              dispatch(types.DEL_QUERY)
            }
          })
          .catch((err) => {
            // error handling
          })
          .finally(() => {
            if(state.cloning || state.renaming)
            {
              console.log(state)
            }
            // query list tab is active
            dispatch(types.LOAD_APP,state.app)
            let int = setInterval(() => {
              // if(!state.loading)
              // {
                commit(types.SET_UNSAVEDCHANGES, 0)
                commit(types.SET_UNSAVEDPARAMS, 0)
                commit(types.SET_SAVING, false)
                commit(types.SET_CREATING, false)
                commit(types.SET_CLONING, false)
                commit(types.SET_RENAMING, false)
                clearInterval(int)
              // }
            },200)
          })
        } // uniqueness check
        else
        {
          // TODO set alert -- query exists
          console.log('query exists')
          commit(types.SET_UNSAVEDCHANGES, 0)
          commit(types.SET_UNSAVEDPARAMS, 0)
          commit(types.SET_SAVING, false)
          commit(types.SET_CREATING, false)
          commit(types.SET_CLONING, false)
          commit(types.SET_RENAMING, false)
        }
      })
    }
    else // standard mod (not renaming, creating, cloning)
    {
      if(state.unsavedParams > 0)
      {
        qnames = ['YADA delete default params for target']
        payloads = [{
          TARGET: state.qname,
          APP: state.app
        }]
        j = prepareQueries(qnames,payloads)
        return this._vm.$yada.jp(j)
        .then(() => {
          return dispatch(types.MOD_QUERY)
        })
        .catch((err) => {
          // error handling
        })
        .finally(() => {
          setTimeout(() => {
            commit(types.SET_UNSAVEDCHANGES, 0)
            commit(types.SET_UNSAVEDPARAMS, 0)
            commit(types.SET_SAVING, false)
          },500)
        })
      }
      else
      {
        return dispatch(types.MOD_QUERY)
        .catch((err) => {
          // error handling
        })
        .finally(() => {
          setTimeout(() => {
            commit(types.SET_UNSAVEDCHANGES, 0)
            commit(types.SET_UNSAVEDPARAMS, 0)
            commit(types.SET_SAVING, false)
          },500)
        })
      }
    }
  },

  [types.ADD_PARAM]({state,commit}) {
    // create new param object and store it
    let max = state.renderedParams.length == 0 ? 0 : Math.max(...state.renderedParams.map(p => parseInt(p.ID)))
    let row = {'TARGET':state.qname,'ID': (max + 1), 'NAME':'','VALUE':'','RULE':1}
    let renderedParams = state.renderedParams
    renderedParams.push(row)
    commit(types.SET_RENDEREDPARAMS,renderedParams)
    commit(types.SET_UNSAVEDCHANGES,state.unsavedChanges+1)
    commit(types.SET_UNSAVEDPARAMS,state.unsavedParams+1)
  },

  [types.COPY_CODE]({state,commit,dispatch}){
    let txt = document.querySelector('#query-editor-ghost pre').textContent
    navigator.clipboard.writeText(txt).then(e => { console.log(e) })
    Array.from(document.querySelectorAll('.query-editor div')).forEach(el => el.classList.add('flash'))
    setTimeout(() => {
      Array.from(document.querySelectorAll('.query-editor .flash')).forEach(el => el.classList.remove('flash'))
    },200)
  },

  [types.CHECK_UNIQ]({state,commit,dispatch}, qname){
    let q = 'YADA check uniqueness'
    return this._vm.$yada.jp([{qname:q,DATA:[{qname:qname,app:state.app}]}])
  },


  [types.ADD_QUERY]({state,commit,dispatch}) {
    commit(types.SET_CREATING, true)
    let app     = state.app
    let date    = new Date().toISOString().substr(0,19).replace(/T/,' ')
    let qname   = `${app} ${Math.abs(util.hash(date))}`
    let user    = state.loggeduser
    let query = {
      QNAME: qname,
      QUERY: '-- Replace with YADA markup',
      CREATED_BY: user,
      CREATED: date,
      MODIFIED_BY: user,
      MODIFIED: date,
      APP: state.app,
      ACCESS_COUNT: 0,
      COMMENTS: `Created in yada-admin by ${user} at ${date}`
    }
    commit(types.SET_QNAME, qname)
    commit(types.SET_QUERY, query)
  },

  // delete query menu item will trigger this action
  [types.DEL_QUERY_CONFIRM]({commit,state}) {
    commit(types.SET_CONFIRMACTION, 'DEL_QUERY')
    commit(types.SET_CONFIRM, `Are you sure you want to delete ${state.qname}` )
  },

  // affirmative choice in delete query confirmation will trigger this
  // deletes query, params, props, and protectors
  [types.DEL_QUERY]({commit,state,dispatch}) {
    let qnames = ['YADA delete query']  // app, qname
    let payloads = [
      {APP: state.app, QNAME: state.qnameOrig}, // crud protector
    ]
    let hasParams = typeof state.params !== 'undefined' && state.params !== null && state.params.length > 0
    let hasProps = typeof state.props !== 'undefined' && state.props !== null && state.props.length > 0
    let hasProtectors = typeof state.protectors !== 'undefined' && state.protectors !== null && state.protectors.length > 0
    if(hasParams)
    {
      qnames.push('YADA delete default params for target') // app, qname (target)
      payloads.push({APP: state.app, TARGET: state.qnameOrig}) // crud protector
    }
    if(hasProps)
    {
      qnames.push('YADA delete prop for target') // app, qname (target)
      payloads.push({APP: state.app, TARGET: state.qnameOrig}) // crud protector
    }
    if(hasProtectors)
    {
      qnames.push('YADA delete protector for target') // target, qname
      payloads.push({TARGET: state.qnameOrig, QNAME: state.protectors[0].qname})  //crud by qname protector
    }
    let j = prepareQueries(qnames,payloads)
    return this._vm.$yada.jp(j)
    .then(() => {
      commit(types.SET_QNAME, null)
      commit(types.SET_QNAMEORIG, null)
    })
  },

  // delete query menu item will trigger this action
  [types.SAVE_CHANGES_CONFIRM]({commit,state}, context) {
      let action = `SAVE_${context == 'app' ? 'APP' : 'QUERY'}`
      commit(types.SET_CONFIRMACTION, action)
      commit(types.SET_CONFIRM, `You have unsaved changes. Would you like to save them before moving on?` )
  },

  // delete query menu item will trigger this action
  [types.DEL_PARAM_CONFIRM]({commit,state},param) {
    commit(types.SET_CONFIRMACTION, 'DEL_PARAM')
    commit(types.SET_PARAM, param)
    commit(types.SET_CONFIRM, `Are you sure you want to delete the ${param.name} parameter` )
  },

  // affirmative choice in delete query confirmation will trigger this
  [types.DEL_PARAM]({commit,state,dispatch}) {
    let paramhash = util.hash(Object.values(state.param).join(""))
    let renParams = state.renderedParams.filter(p => {
      let phash = util.hash(Object.values(p).join(""))
      return phash != paramhash
    })

    commit(types.SET_CONFIRMACTION,null)
    commit(types.SET_PARAM,null)
    commit(types.SET_CONFIRM,null)
    commit(types.SET_RENDEREDPARAMS, renParams)
    commit(types.SET_UNSAVEDCHANGES, state.unsavedChanges+1)
    commit(types.SET_UNSAVEDPARAMS, state.unsavedParams+1)

  }
}
