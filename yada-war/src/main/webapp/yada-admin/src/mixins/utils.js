import * as types from '../store/vuex-types'
import { mapState } from 'vuex'

export const utils = {
  data() {
    return {}
  },

  computed: {
    ...mapState(['unsavedChanges','TRACE','TRACE_STATE','TRACE_COLLAPSE'])
  },

  methods: {
    hash (str) {
      var hash = 0;
      if (str.length == 0) {
          return hash;
      }
      for (var i = 0; i < str.length; i++) {
          var char = str.charCodeAt(i);
          hash = ((hash<<5)-hash)+char;
          hash = hash & hash; // Convert to 32bit integer
      }
      return hash;
    },
    unsaved (e,o) {
      // this.trace()
      this.$store.commit(types.SET_UNSAVEDCHANGES, this.unsavedChanges+1)
    },
    debounce (delay, fn) {
      let timer
      return function(...args) {
        if(timer)
        {
          console.log('rescheduling showCode')
          clearTimeout(timer)
        }
        timer = setTimeout(() => {
          console.log('scheduling showCode')
          fn(...args)
          timer = null
        }, delay)
      }
    },
    trace(header) {
      if(this.TRACE)
      {
        let e = new Error().stack.split(/\n/)
        let css = [], compLine = []
        if(typeof header === 'undefined' || header == "")
        {
          css = ["color:#888","color:seagreen","color:mediumaquamarine"]
          //    at VueComponent.selectQpi (webpack-internal:///./node_modules/babel-loader/lib/index.js!./node_modules/vue-loader/lib/selector.js?type=script&index=0!./src/components/modules/QPI.vue:264:12)

          // moving this into utils required changing the "componentLine" aka "compLine" variable to be discovered through iteration over the stack trace.
          // previously using e[2] was fine because it was called directly from the tracer mixin, which was always referenced at index 1 of the stacktrace
          // this all changed in order to call the trace function from the utils mixin itself
          compLine = e.slice(1).reduce((a,c,i,r) => {
            if(!/utils|trace/.test(c))
            {
              a = e[i+1].split(/\//).slice(-1)[0].replace(/\)/,'').replace(/\.vue:/,' ').split(/\s/)  //i+1 because we're slicing e from index 1
              r.splice(i)
              return a
            }
          },[])
          // compLine = e[2].split(/\//).slice(-1)[0].replace(/\)/,'').replace(/\.vue:/,' ').split(/\s/)
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
