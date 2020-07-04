import * as types from './vuex-types'

export default {
  // (s, p) means (state, payload)

  // activities
  [types.SET_MENUITEMS] (s, p) { s.menuitems = p },
  [types.SET_LOADING] (s, p) { s.loading = p },
  [types.SET_SAVING] (s, p) { s.saving = p },
  [types.SET_CONFIRM] (s, p) { s.confirm = p },
  [types.SET_CONFIRMACTION] (s, p) { s.confirmAction = p },
  [types.SET_CREATING] (s, p) { s.creating = p },
  [types.SET_CLONING] (s, p) { s.cloning = p },
  [types.SET_ACTIVETAB] (s, p) { s.activeTab = p },
  [types.SET_NEXTTAB] (s, p) { s.nextTab = p },
  [types.SET_FILTER] (s, p) { s.filter = p },
  [types.SET_LOGGEDUSER] (s, p) { s.loggeduser = p },
  [types.SET_ERRORS] (s, p) { s.errors = p },
  [types.SET_SHOWWARNING] (s, p) { s.showWarning = p },
  [types.SET_UNSAVEDCHANGES] (s, p) { s.unsavedChanges = p },
  [types.SET_UNSAVEDPARAMS] (s, p) { s.unsavedParams = p },
  [types.SET_RENDEREDPARAMS] (s, p) { s.renderedParams = p },
  [types.SET_CONTEXTMENU] (s, p) { s.contextmenu = p },
  [types.SET_COORDS] (s, p) { s.coords = p },
  [types.SET_RENAMING] (s, p) { s.renaming = p },

  // data
  [types.SET_APP] (s, p) { s.app = p },
  [types.SET_APPS] (s, p) { s.apps = p },
  [types.SET_QNAME] (s, p) { s.qname = p },
  [types.SET_QNAMEORIG] (s, p) { s.qnameOrig = p },
  [types.SET_QUERY] (s, p) { s.query = p },
  [types.SET_CONFIG] (s, p) { s.config = p },
  [types.SET_QUERIES] (s, p) { s.queries = p },
  [types.SET_PARAMS] (s, p) { s.params = p },
  [types.SET_PARAM] (s, p) { s.param = p },
  [types.SET_PROPS] (s, p) { s.props = p },
  [types.SET_PROTECTORS] (s, p) { s.protectors = p },
  [types.SET_SECCONF] (s, p) { s.secconf = p }
}
