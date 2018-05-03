import axios from 'axios'
export default {
  install:function(Vue,options) {
    Vue.prototype.$yada = axios.create({
      baseURL: 'https://yada.qdss.io',
      timeout: 1000,
      withCredentials: true,
      headers: {'Content-Type': 'application/json'}
    })

    /**
     * Shortcut function for YADA path-style get requests
     * @method
     * @param  {[type]} qname the yada query name
     * @param  {[type]} params the comma-delimited string of yada data params
     * @return {[type]} the Axios promise
     */
    Vue.prototype.$yada.path = function(qname,params) {
      var path = '/yada/q/'+qname
      path += !!params ? '/p/'+params : ''
      return Vue.prototype.$yada.get(path);
    }

    /**
     * Shortcut function for YADA JSONParams requests. When method is POST,
     * {headers:{'Content-Type':'application/x-www-form-urlencoded'}} is set
     * @method
     * @param  {[type]} jsonparams the JSONParams YADA parameter
     * @param  {[type]} method GET or POST (default)
     * @return {[type]} the Axios promise
     */
    Vue.prototype.$yada.jp = function(jsonparams,method) {
      var config = {};
      var options = {};
      if(!!!method || method.toLowerCase() == 'post')
      {
        method = 'post'
        config = require('qs').stringify({j:JSON.stringify(jsonparams)})
        options = {headers:{'Content-Type':'application/x-www-form-urlencoded'}}
      }
      else
      {
        config = {params:{j:JSON.stringify(jsonparams)}}
      }
      return Vue.prototype.$yada[method.toLowerCase()]('/yada.jsp',config,options)
    }

    /**
     * Shortcut function for YADA standard requests with 'q' or 'qname' and 'p' or
     * 'params' in the URL query string
     * When 'method' is POST,
     * {headers:{'Content-Type':'application/x-www-form-urlencoded'}} is set
     * @method
     * @param  {[type]} qname the YADA query name
     * @param  {[type]} params the comma-delitimed string of yada data params
     * @param  {[type]} method GET (default) or POST
     * @return {[type]} the Axios promise
     */
    Vue.prototype.$yada.std = function(qname,params,method) {
      var config = {};
      var options = {};
      if(!!!method || method.toLowerCase() == 'get')
      {
        method = 'get'
        config = {params:{q:qname,p:params}}
      }
      else
      {
        config = require('qs').stringify({q:qname,p:params})
        options = {headers:{'Content-Type':'application/x-www-form-urlencoded'}}
      }
      return Vue.prototype.$yada[method.toLowerCase()]('/yada.jsp',config,options)
    }

    /**
     * alias to 'std' function
     * @type {[type]}
     */
    Vue.prototype.$yada.standard = Vue.prototype.$yada.std

    /**
     * alias to 'jp' function
     * @type {[type]}
     */
    Vue.prototype.$yada.jsonparams = Vue.prototype.$yada.jp
  }
}
