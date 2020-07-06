import axios from 'axios'
// const YADA_HOST = process.env.YADA.host
// const YADA_PORT = process.env.YADA.port
// const YADA_PROT = process.env.YADA.protocol
// const YADA_URL  = YADA_PROT + ': //' + YADA_HOST + (!!YADA_PORT && YADA_PORT !== '' ? ': ' + YADA_PORT : '')

export default {
  install: function (Vue, options) {
    const YADA = JSON.parse(window.sessionStorage.getItem('YADA'))
    Vue.prototype.$yada = axios.create({
      // baseURL: YADA_URL,
      timeout: 0,
      maxContentLength: 2000000,
      withCredentials: true,
      headers: {'Content-Type': 'application/json',
        'X-CSRF-Token': YADA.sec['X-CSRF-Token'],
        'Authorization': `Bearer ${YADA.sec['Bearer']}`}
    })

    function hash (str) {
      var hash = 0
      if (str.length === 0)
      {
        return hash
      }
      for (var i = 0; i < str.length; i++)
      {
        var char = str.charCodeAt(i)
        hash = ((hash << 5) - hash) + char
        hash = hash & hash // Convert to 32bit integer
      }
      return hash
    }

    function handleError (error) {
      if (error.response)
      {
        // The request was made and the server responded with a status code
        // that falls out of the range of 2xx
        const msg = `YADA ${error.config.method.toUpperCase()} Error: %c ${error.response.status} %c  \u21D2`
        console.log(msg, 'background: red;color: white', 'background: white;color: black')
        console.dir(error.response, {depth: null})
        // console.log(error.response.data)
        // console.log(error.response.status)
        // console.log(error.response.headers)
      }
      else if (error.request)
      {
        // The request was made but no response was received
        // `error.request` is an instance of XMLHttpRequest in the browser and an instance of
        // http.ClientRequest in node.js
        console.log(error.request)
      }
      else
      {
        // Something happened in setting up the request that triggered an Error
        console.log('Error', error.message)
      }
      // console.log(error.config)
    }

    function paramSerializer (yadaOptions) {
      const parts        = []
      const jsonparams   = yadaOptions['j']
      // const qname        = typeof jsonparams === 'object' ? jsonparams[0]['qname'] : yadaOptions['q']
      let changeSource   = true
      // let   app          = qname.split(/\s/)[0]

      // add jsonparams to param array if present
      if (typeof jsonparams === 'object')
        parts.push('j=' + JSON.stringify(jsonparams))

      // Removing for now, for yada
      // parts.push('ck=qdssuser,qdssjwt,qdssgroups')

      // process remaining params
      if (!!yadaOptions && Object.keys(yadaOptions).length > 0)
      {
        for (let k of Object.keys(yadaOptions))
        {
          if (k !== 'j')
          {
            // is param value a multiple in an array?
            if (Array.isArray(yadaOptions[k]))
            {
              for (let i = 0; i < yadaOptions[k].length; i++)
              {
                let paramStr = yadaOptions[k][i]
                if ((k === 'pl' || k === 'plugin')               // plugin
                   && process.env.NODE_ENV !== 'production'   // DEV or TEST
                   && changeSource                            // SourceExchanger not set
                   && /SourceExchanger/.test(paramStr))       // its got the param
                {
                  paramStr += process.env.NODE_ENV_LABEL
                  changeSource = false
                }
                parts.push(k + '=' + encodeURIComponent(paramStr))
              }
            }
            else if ((k === 'pl' || k === 'plugin')            // plugin
                 && process.env.NODE_ENV !== 'production'   // DEV or TEST
                 && changeSource                            // SourceExchanger not set
                 && /SourceExchanger/.test(yadaOptions[k])) // it's got the param
            {
              changeSource = false
              let paramStr = yadaOptions[k] + process.env.NODE_ENV_LABEL
              parts.push(k + '=' + encodeURIComponent(paramStr))
            }
            else if (process.env.NODE_ENV !== 'production'   // DEV or TEST
               && changeSource)                             // SourceExchanger not set
            {
              changeSource = false
              // Removing for now, for yada
              // parts.push('pl=' + encodeURIComponent('SourceExchanger,' + app + process.env.NODE_ENV_LABEL))
              parts.push(k + '=' + encodeURIComponent(yadaOptions[k]))
            }
            else
            {
              parts.push(k + '=' + encodeURIComponent(yadaOptions[k]))
            }
          }
        }
      }
      return parts.join('&')
    }

    Vue.prototype.$yada.jp = function () {
      // set jsonparams
      const jsonparams   = arguments[0]
      // const qname        = jsonparams[0].qname
      const axiosOptions = {}

      // set yada options and method
      let yadaOptions, method // , headers

      if (typeof arguments[1] === 'object') // reset 'yadaOptions' to value of 'method'
      {
        yadaOptions = arguments[1]
        method = 'post'
      }
      else
      {
        yadaOptions = typeof arguments[2] !== 'undefined' ? arguments[2] : {}
        method = arguments[1]
      }
      yadaOptions['j'] = jsonparams

      axiosOptions['params'] = yadaOptions
      axiosOptions['paramsSerializer'] = paramSerializer

      if (!!!method || method.toLowerCase() === 'post')
      {
        method = 'post'
      }

      if (process.env.NODE_ENV === 'development' && !!options.debug)
      {
        console.groupCollapsed('YADA ' + method.toUpperCase() + ': ' + yadaOptions['j'][0]['qname'])
        console.group('params')
        let v = yadaOptions['j'][0]['DATA'][0]
        for (let k in v)
          console.log(k + ':     ' + v[k])
          // console.log('params: ', JSON.stringify(yadaOptions['j'][0]['DATA'][0]))
        console.groupEnd()

        console.count(hash(yadaOptions['j'][0]['qname']))
        console.groupEnd()
      }

      return Vue.prototype.$yada[method.toLowerCase()]('/yada.jsp',
        yadaOptions, { transformRequest: [paramSerializer],
          headers: {'Content-Type': 'application/x-www-form-urlencoded'} }
      ).catch((error) => { handleError(error) })
    }

    Vue.prototype.$yada.std = function () {
      const qname  = arguments[0]
      const params = arguments[1]
      // const config = {}
      const axiosOptions = {}

      let method, yadaOptions

      // swap the args if only 3 are passed
      if (typeof arguments[2] === 'object')
      {
        yadaOptions = arguments[2]
        method = 'get'
      }
      else
      {
        yadaOptions = typeof arguments[3] !== 'undefined' ? arguments[3] : {}
        method = arguments[2]
      }
      // deconstruct param string
      yadaOptions['q'] = qname
      if (!!params)
      {
        yadaOptions['p'] = params
      }

      axiosOptions['params'] = yadaOptions
      axiosOptions['paramsSerializer'] = paramSerializer

      // its a get
      if (!!!method || method.toLowerCase() === 'get')
      {
        method = 'get'
      }

      if (process.env.NODE_ENV === 'development' && !!options.debug)
      {
        console.groupCollapsed(`YADA ${method.toUpperCase()}: ${yadaOptions['q']}`)
        console.group('params')
        console.log(yadaOptions['p'])
        console.groupEnd()
        console.count(hash(yadaOptions['q']))
        console.groupEnd()
      }

      return Vue.prototype.$yada[method.toLowerCase()]('/yada.jsp',
        { params: yadaOptions, paramsSerializer: paramSerializer }
      ).catch((error) => { handleError(error) })
    }

    // Vue.prototype.$yada.path = function(qname,params,yadaOptions) {
    //   var path = '/yada/q/' + qname
    //   yadaOptions = changeSourceForEnv(qname,yadaOptions)
    //   path += !!params ? '/p/' + params : '' + !!yadaOptions ? yadaOptions : ''
    //   return Vue.prototype.$yada.get(path).catch((error) => { handleError(error) })
    // }

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
