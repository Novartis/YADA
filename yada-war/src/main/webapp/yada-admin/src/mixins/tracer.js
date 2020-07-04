export const tracer = {
  data() {
    return {
      TRACE: process.env.TRACE,
      TRACE_STATE: process.env.TRACE_STATE,
      TRACE_COLLAPSE: process.env.TRACE_COLLAPSE
    }
  },

  methods: {
    // traceState(header) {
    //   if(this.TRACE_STATE)
    //   {
    //     let e = new Error().stack.split(/\n/)
    //     if(typeof header == 'undefined' || header == "")
    //     {
    //
    //     }
    //     if(this.TRACE_COLLAPSE)
    //       console.groupCollapsed("State Change: " + header)
    //     else
    //       console.group("State Change: " + header)
    //     console.log("Trace\n" + e.slice(2).join("\n"))
    //     console.groupEnd()
    //   }
    // },
    trace(header) {
      if(this.TRACE)
      {
        let e = new Error().stack.split(/\n/)
        let css = []
        if(typeof header === 'undefined' || header == "")
        {
          css = ["color:#888","color:seagreen","color:mediumaquamarine"]
          //    at VueComponent.selectQpi (webpack-internal:///./node_modules/babel-loader/lib/index.js!./node_modules/vue-loader/lib/selector.js?type=script&index=0!./src/components/modules/QPI.vue:264:12)
          let compLine = e[2].split(/\//).slice(-1)[0].replace(/\)/,'').replace(/\.vue:/,' ').split(/\s/)
          let comp = compLine[0]
          let line = compLine[1].split(/:/)[0]
          let fn   = e[2].split(/\s+/)[2].split(/\./)[1]
          let trig = ""

          if (/Watcher\.run/.test(e[3]))
          {
            trig = "%c(watcher)"
            css = [...css,"color:#AAA"]
          }
          else
          {
            let i = 1
            while(trig == "" && i < e.length) {
              //  at VueComponent.mounted (webpack-internal:///./node_modules/babel-loader/lib/index.js!./node_modules/vue-loader/lib/selector.js?type=script&index=0!./src/components/modules/filters/QPICategoryFilter.vue:119:10)
              //  at callHook (webpack-internal:///../node_modules/vue/dist/vue.esm.js:2920:21)
              //
              // or
              //
              //  at VueComponent.selectQpi (webpack-internal:///./node_modules/babel-loader/lib/index.js!./node_modules/vue-loader/lib/selector.js?type=script&index=0!./src/components/modules/QPI.vue:264:12)
              //  at VueComponent.mounted (webpack-internal:///./node_modules/babel-loader/lib/index.js!./node_modules/vue-loader/lib/selector.js?type=script&index=0!./src/components/modules/QPI.vue:314:10)
              //  at callHook (webpack-internal:///../node_modules/vue/dist/vue.esm.js:2920:21)

              if(/callHook/.test(e.slice(2)[i]))
              {
                if(i == 1)
                {
                  trig = "%c(lifecycle hook)"
                  css = [...css,"color:#AAA"]
                }
                else
                {
                  trig = `%c(via %c${e.slice(2)[i-1].split(/\s+/)[2].split(/\./)[1]} %clifecycle hook)`
                  css = [...css,"color:#AAA","color:#888","color:#AAA"]
                }
              }
              //  at VueComponent.buildDropdown (webpack-internal:///./node_modules/babel-loader/lib/index.js!./node_modules/vue-loader/lib/selector.js?type=script&index=0!./src/components/modules/filters/QPICategoryFilter.vue:56:12)
              //  at VueComponent.globalQpiCategoryList (webpack-internal:///./node_modules/babel-loader/lib/index.js!./node_modules/vue-loader/lib/selector.js?type=script&index=0!./src/components/modules/filters/QPICategoryFilter.vue:136:14)
              //  at Watcher.run (webpack-internal:///../node_modules/vue/dist/vue.esm.js:3232:19)
              else if(/Watcher\.run/.test(e.slice(2)[i]))
              {
                trig = `%c(via %c${e.slice(2)[i-1].split(/\s+/)[2].split(/\./)[1]} %cwatcher)`
                css = [...css,"color:#AAA","color:#888","color:#AAA"]
              }
              i++
            }
          }
          header = `%c${comp}.${fn} %c${line} ${trig}`
        }
        if(this.TRACE_COLLAPSE)
          console.groupCollapsed("%cTrace: " + header,...css)
        else
          console.group("%cTrace: %c" + header,...css)
        console.log("Trace\n" + e.slice(2).join("\n"))
        console.groupEnd()
      }
    }
  }
}
