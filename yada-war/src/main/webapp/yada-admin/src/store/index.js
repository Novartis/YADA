import Vue from 'vue'
import Vuex from 'vuex'

import actions from './actions'
import getters from './getters'
import mutations from './mutations'

import * as types from './vuex-types'

Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    // application activity state
    TRACE: process.env.TRACE,
    TRACE_STATE: process.env.TRACE_STATE,
    TRACE_COLLAPSE: process.env.TRACE_COLLAPSE,
    saving: false,      // used to render "saving" modal alert box
    confirm: false,     // used to render "confirmation" modal alert
    creating: false,    // used to create new app
    loading: false,
    renaming: false,
    cloning: false,
    errors: false,
    showWarning: false,
    unsavedChanges: 0, // triggered by changes to input fields
    unsavedParams: 0,  // triggered by any param changes
    activeTab: null,     // the currently selected tab
    nextTab: null,
    confirmAction: null,

    loggeduser: 'YADA',
    mainmenu: [],

    contextmenu: [],   // list of contextual menu items
    coords: [0,0],     // used to render contextual menu at mouse position

    menuitems: [],
    filter: {
      render: false,
      stripe: false,
      selector: '',
      rxflags: 'i'
    },

    // tab configs
    tabs: {
      apps: {
        filter: {
          render: true,
          stripe: false,
          selector: 'div.applistitem',
          rxflags:'i'
        },
        menuitems: [
          {value:'Add App',icon:'icon plus',fn:types.ADD_APP}
        ]
      },
      conf: {
        filter: {
          render: false,
          stripe: false,
          selector: '',
          rxflags:'i'
        },
        menuitems: [
          {value:'Save',icon:'icon save',shortcut:'\u2318-S',fn: types.SAVE
          }
        ]
      },
      "query-list": {
        filter: {
          render: false,
          stripe: false,
          selector: '#query-list tbody>tr',
          rxflags:'i'
        },
        menuitems: [{value:'Add Query',icon:'icon plus',fn:types.ADD_QUERY}]
      },
      "query-edit": {
        filter: {
          render: false,
          stripe: false,
          selector: '',
          rxflags:'i'
        },
        menuitems: [
          {value:'Save',icon:'icon save',shortcut:'\u2318-S',fn:types.SAVE},
          {value:'Rename',icon:'icon edit',fn: types.RENAME_QUERY },
          {value:'Clone',icon:'icon clone outline',fn: types.CLONE_QUERY },
          {value:'Delete',icon:'icon delete',fn: types.DEL_QUERY_CONFIRM },
          {value:'Copy Code',icon:'icon copy',fn: types.COPY_CODE },
          {value:'Add Param',icon:'icon plus',fn: types.ADD_PARAM },
          {value:'Add Auth Policy ',icon:'icon plus',fn:(e) => {console.log('add auth policy',e,this,'test')}},
          {value:'Add Protector',icon:'icon plus',fn:(e) => {console.log('add protector',e,this,'test')}},
          {value:'Toggle Help',icon:'icon help',fn:(e) => {console.log('helpp',e,this,'test')}}]
      }
    },

    // data selections
    app: null,          // app name
    config: null,       // app config
    apps: [],           // app list
    queries: [],        // query list
    param: null,
    params: [],         // default query params from index
    renderedParams: [], // currently rendered params, incl. unchanged, as well as updates and inserts
    props: [],          // query props
    protectors: [],     // protector queries
    qname: null,        // selected qname
    qnameOrig: null,    // qname before renaming
    query: null,        // query object

    // url parameter widgets
    paramlist: [
		  	{alias:'a',name:'args',
         type:'String',
				 default:'Change me...',
				 placeholder:'Comma-separated list...',
         pattern:'^((?!Change me...).)+',
				 tip:"A comma-separated list of arguments expected by the plugin. For script plugins, the script name should be first."},
		  	{alias:'b',name:'bypassargs',
         type:'String',
				 default:'Change me...',
				 placeholder:'Comma-separated list...',
         pattern:'^((?!Change me...).)+',
				 tip:"A comma-separated list of arguments expected by the bypass plugin. For script plugins, the script name should be first."},
		  	{alias:'c',name:'count',
         type:'Boolean',
				 default:'false',
				 tip:'Defaults to "true" in service layer so YADA will execute a second "count" query returning the total number records in the result set, rather than just the amount in the first page.  Set to "false" to suppress this second query.'},
		  	{alias:'ck',name:'cookies',
         type: 'String',
				 default:'Change me...',
				 placeholder:'',
         pattern:'^((?!Change me...).)+',
				 tip:'A comma-separated list of cookie names to pass to a YADA REST query'},
		  	{alias:'cq',name:'commitQuery',
         type:'Boolean',
				 default:'false',
				 placeholder:'',
				 tip:'Set to "true" to execute a commit after each query, "false", the service layer default, will execute commits after all queries in the request have been executed.'},
		  	{alias:'co',name:'countOnly',
         type:'Boolean',
				 default:'false',
				 placeholder:'',
				 tip:'When "true", YADA will execute the secondary "count" query only, instead of retrieving the data. The service layer default is "false".'},
		  	{alias:'cv',name:'converter',
         type:'String',
				 default:'Change me...',
				 placeholder:'Class name or FQCN...',
         pattern:'^((?!Change me...).)+',
				 tip:'The classname or FQCN of a com...yada.format.Converter implementation.'},
		  	{alias:'d',name:'delimiter',
         type:'String',
				 default:',',
				 placeholder:'Character or string...',
         pattern:'^((?!Change me...).)+',
				 tip:'The column delimiting character or string'},
		  	{alias:'e',name:'export',
         type:'Boolean',
				 default:'false',
				 placeholder:'',
				 tip:'When "true", the result set is written to disk and YADA returns the path to the new file. Turning mutability off while "false" will disable export altogether.'},
		  	{alias:'el',name:'exportlimit',
         type:'Number',
				 default:'100000',
				 placeholder:'Number...',
         min:1,
         max:Infinity,
				 tip:'The maximum number of records to include in an export. This is useful for restricting web clients from downloading very large datasets in entirety'},
		  	// {alias:'fi',name:'filters',
				//  default:'Change me...',
				//  placeholder:'',
				//  tip:'Documentation pending'},
		  	{alias:'f',name:'format',
         type:'String',
				 default:'json',
				 placeholder:'csv, tsv, xml, pipe, etc...',
         pattern:'json|csv|tsv|xml|pipe|.+',
				 tip:'The format of the data in the response: JSON (default), csv, tsv, pipe, xml, custom (use delimiter params)'},
		  	// {alias:'h',name:'harmonyMap',
				//  default:'Change me...',
				//  placeholder:'',
				//  tip:'A JSON string pairing source-result field names or paths to response field names or paths.'},
        {alias:'H',name:'httpHeaders',
         type:'String',
				 default:'Change me...',
				 placeholder:'Comma-separated list...',
         pattern:'^((?!Change me...).)+',
				 tip:'A comma-separated list of header names OR a JSON String with header name keys and String or boolean aliass.'},
		  	{alias:'pg',name:"page/pagestart",
         type:'Number',
				 default:'1',
				 placeholder:'Number...',
         min:1,
         max:Infinity,
				 tip:'When pagination is in use, use this parameter to set the default first page, if > 1 (default)'},
        {alias:'ps',name:"page/pagestart",
         type:'Number',
				 default:'1',
				 placeholder:'Number...',
         min:1,
         max:Infinity,
				 tip:'When pagination is in use, use this parameter to set the default first page, if > 1 (default)'},
		  	// {alias:'path',name:"path",
				//  default:'Change me...',
				//  placeholder:'',
				//  tip:''},
		  	{alias:'pz',name:'pagesize',
         type:'Number',
				 default:'20',
				 placeholder:'Number...',
         min:-1,
         max:Infinity,
				 tip:'Integer denoting the number of records to return per page. Use "-1" to return all records.  The default is 20.'},
		  	{alias:'pl',name:'plugin',
         type:'String',
				 default:'Change me...',
				 placeholder:'Script name, class name, or FQCN...',
         pattern:'^((?!Change me...).)+',
				 tip:'The classname FQDN of the plugin class in the com...yada.plugin package'},
		  	{alias:'pa',name:'postargs',
         type:'String',
				 default:'Change me...',
				 placeholder:'Comma-separated list...',
         pattern:'^((?!Change me...).)+',
				 tip:'A comma-separated list of arguments expected by the postprocessor plugin. For script plugins, the script name should be first'},
		  	{alias:'pr',name:'preargs',
         type:'String',
				 default:'Change me...',
				 placeholder:'Comma-separated list...',
         pattern:'^((?!Change me...).)+',
				 tip:'A comma-separated list of arguments expected by the preprocessor plugin. For script plugins, the script name should be first'},
		  	{alias:'py',name:'pretty',
         type:'Boolean',
				 default:'false',
				 placeholder:'',
				 tip:'Format a JSON result with linefeeds and indentation. Defaults to "false" to reduce footprint.'},
		  	{alias:'px',name:'proxy',
         type:'String',
				 default:'Change me...',
				 placeholder:'hostname:port',
         pattern:'^((?!Change me...).)+\:[0-9]{1,4}',
				 tip:'The hostname and address of the proxy server.  This may be required for external REST queries, for example.'},
		  	{alias:'rd',name:'rowDelimiter',
				 type:'String',
         default:'Change me...',
				 placeholder:'Character or string...',
         pattern:'^((?!Change me...).)+',
				 tip:'The character or string to delimit rows of tabular data returned in a delimited response'},
		  	{alias:'r',name:'response',
         type:'String',
				 default:'\n',
				 placeholder:'Class name or FQCN...',
         pattern:'^((?!Change me...).)+',
				 tip:'The classname or FQDN of the com...yada.format.Response implementation'},
		  	{alias:'s',name:'sortkey',
         type:'String',
				 default:'Change me...',
				 placeholder:'Column name...',
         pattern:'^((?!Change me...).)+',
				 tip:'The column name on which to sort results before the result set is returned from the server. Should support pagination.'},
		  	{alias:'so',name:'sortorder',
         type:'String',
				 default:'desc',
				 placeholder:'asc or desc...',
         pattern:'asc|desc',
				 tip:'asc (default) or desc, referring to the sortkey'},
		  	{alias:'u',name:'user',
				 type:'String',
         default:'Change me...',
				 placeholder:'User ID...',
         pattern:'^((?!Change me...).)+',
				 tip:'The user id, useful when such information is not trasmitted automatically in the request.'},
		  	{alias:'vl',name:'viewlimit',
         type:'Number',
				 default:'100000',
				 placeholder:'Number...',
         min:1,
         max:Infinity,
				 tip:'Integer defining the maximum number of results to return even in pagination use-cases. The view limit effectively caps the secondary "count" query. This is useful when count queries run on very large datasets, causing latency.'}
	  	]
  },
  actions,
  getters,
  mutations
});
