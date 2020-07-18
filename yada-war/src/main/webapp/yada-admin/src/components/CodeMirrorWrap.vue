<template>
  <div class="codemirror">
    <!-- <button class="copy btn" data-clipboard-target="#query-editor-ghost"
            id="query-code-copy" type="button">
    </button> -->
    <div class="codemirror-info">
      <span class="instructions">Press 'F2' to enter/exit fullscreen editor (when code has focus)</span>
      <span class="chars">Characters: {{ charcount }}</span>
    </div>

    <div id="query-editor"/>
    <div
      id="query-editor-ghost"
      style="height:1px;width:1px;overflow:hidden;"/>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import CodeMirror from 'codemirror'
// language js
import 'codemirror/mode/sql/sql.js'
// theme css
import 'codemirror/lib/codemirror.css'
import 'codemirror/theme/eclipse.css'
import 'codemirror/addon/display/autorefresh.js'
import 'codemirror/addon/display/fullscreen.js'
import 'codemirror/addon/display/fullscreen.css'
// more codemirror resources
// import 'codemirror/some-resource...'
export default {
  name: 'CodeMirrorWrap',
  data () {
    return {
      charcount: 0,
      codemirror: null
    }
  },
  methods: {
    setValue (o) {
      this.codemirror.setValue(o.QUERY)
    }
  },
  computed: {
    ...mapState(['query', 'unsavedChanges', 'switching'])
  },
  mounted () {

  },
  watch: {
    query (neo, old) {
      let that = this
      if (neo !== null)
      {
        if ((typeof old === 'undefined' || old === null)
            || (typeof old !== 'undefined' && old !== null && Object.keys(old).length === 0))
        // this check protects against infiniloop
        // it will only exec the first time query loads
        {
          Array.from(document.querySelectorAll('#query-list-panel .CodeMirror')).forEach(cm => {
            cm.parentElement.removeChild(cm)
          })
          if (this.codemirror !== null)
          {
            this.setValue(neo)
          }
          else
          {
            this.codemirror = CodeMirror(document.getElementById('query-editor'), {
              value: this.query.QUERY,
              lineNumbers: true,
              firstLineNumber: 1,
              theme: 'eclipse',
              fullscreen: true,
              extraKeys: {
                'F2': function (cm) {
                  cm.setOption('fullScreen', !cm.getOption('fullScreen'))
                },
                'Shift-F2': function (cm) {
                  if (cm.getOption('fullScreen')) cm.setOption('fullScreen', false)
                }
              }
            })
            let mode = 'text/plain'
            if (/^\s*(select|insert|update|delete)/.test(this.query.QUERY.toLowerCase()))
            {
              mode = 'text/x-sql'
            }
            else if (/^\s*[{[]/.test(this.query.QUERY))
            {
              mode = 'application/json'
            }
            else if (/^\s*</.test(this.query.QUERY))
            {
              mode = 'application/xml'
            }
            this.codemirror.setOption('mode', mode)
            this.codemirror.on('change', (i, obj) => {
              let val = this.codemirror.getValue()
              if (obj.origin !== 'setValue')  // avoids marking unsaved when switching queries
              {
                that.unsaved(i, obj)
              }
              this.$set(this.query, 'QUERY', val)
              this.charcount = val.length
              $('#query-editor-ghost').html(`<pre>${val}</pre>`)
            })
            this.charcount = this.codemirror.getValue().length
            $('#query-editor-ghost').html(`<pre>${this.codemirror.getValue()}</pre>`)
            $('#query-editor').data('codemirror', this.codemirror)
            if (window.Cypress && process.env.NODE_ENV_LABEL !== 'PROD')
            {
              window.cm = this.codemirror
            }
          }
        }
      }
    }
  }
}
</script>
<style>
  .CodeMirror {
    /* font-size: larger !important; */
    height: auto !important;
    width: 100%;
    border: 1px solid rgb(0,0,0,0.05);
  }

  .CodeMirror-scroll {
    overflow: auto !important;
    max-height: 200px;
  }

  .CodeMirror-fullscreen .CodeMirror-scroll {

    margin: 5px 10px;
    border: 15px solid rgb(241,80,9,0.2);
    border-right: 5px solid rgb(241,80,9,0.2);

  }

  .codemirror-info .chars {
    float:right;
    background-color: rgb(0,0,0,0.05);
    padding: 3px 7px;
    border-radius: .25rem;
    position: relative;
    right: 0;
  }

  .codemirror-info .instructions {
    color: rgba(0,0,0,.5)
  }

  #query-code-copy {
    margin-top: 6px;
  }

  #query-code-copy:before {
    background-size: 16px;
  }

</style>
