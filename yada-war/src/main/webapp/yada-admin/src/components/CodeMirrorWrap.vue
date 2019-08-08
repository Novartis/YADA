<template>
  <div class="codemirror">
    <button class="copy btn" data-clipboard-target="#query-editor-ghost"
            id="query-code-copy" type="button">
    </button>
    <div class="codemirror-info">Characters: {{charcount}}</div>

    <div id="query-editor"></div>
    <div id="query-editor-ghost" style="height:1px;width:1px;overflow:hidden;"/>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
// import { codemirror, CodeMirror } from 'vue-codemirror'
import CodeMirror from 'codemirror'
// language js
import 'codemirror/mode/sql/sql.js'
// theme css
import 'codemirror/lib/codemirror.css'
import 'codemirror/theme/eclipse.css'
import 'codemirror/addon/display/autorefresh.js'
import 'codemirror/addon/display/fullscreen.js'
// more codemirror resources
// import 'codemirror/some-resource...'
export default {
  name: 'CodeMirrorWrap',
  // components: { codemirror },
  props: [ 'content' ],
  data () {
    return {
      charcount: 0,
      codemirror: null
    }
  },
  methods: {
    setValue(o) {
      this.codemirror.setValue(o.QUERY)
    }
  },
  computed:  mapGetters(['getQuery']),
  mounted() {
    this.codemirror = CodeMirror(document.getElementById('query-editor'), {
      value: this.getQuery.QUERY,
      lineNumbers: true,
      firstLineNumber: 1,
      theme: 'eclipse',
      extraKeys: {
        "F2": function(cm) {
          cm.setOption("fullScreen", !cm.getOption("fullScreen"));
        },
        "Shift-F2": function(cm) {
          if (cm.getOption("fullScreen")) cm.setOption("fullScreen", false);
        }
      }
    })
    let mode = 'text/plain'
    if (/^\s*(select|insert|update|delete)/.test(this.getQuery.QUERY.toLowerCase()))
    {
      mode = 'text/x-sql'
    }
    else if (/^\s*[{[]/.test(this.getQuery.QUERY))
    {
      mode = 'application/json'
    }
    else if (/^\s*</.test(this.getQuery.QUERY))
    {
      mode = 'application/xml'
    }
    this.codemirror.setOption('mode', mode)
    this.codemirror.on('change', () => {
      this.charcount = this.codemirror.getValue().length;
      $('#query-editor-ghost').html('<pre>' + this.codemirror.getValue() + '</pre>')
    })
    this.charcount = this.codemirror.getValue().length
    $('#query-editor-ghost').html('<pre>' + this.codemirror.getValue() + '</pre>')
    $('#query-editor').data('codemirror', this.codemirror)
  },
  watch: {
  }
}
</script>
<style>
  .CodeMirror {
    font-size: larger !important;
  }
  .codemirror-info {
    float:right;
    background-color: rgb(0,0,0,0.05);
    padding: 3px 7px;
    border-radius: .25rem;
    position: relative;
    top: -32px;
    margin-right: -26px;
  }

  #query-code-copy {
    margin-top: 6px;
  }

  #query-code-copy:before {
    background-size: 16px;
  }

</style>
