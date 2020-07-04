<template>
  <table class="sticky ui celled table query-list">
    <caption><h2>{{ app }}</h2></caption>
    <thead>
      <tr>
        <th
          class="three wide"
          :data-tooltip="'App name '+app+' is implied'"
          data-position="bottom left">Qname <span style="color:#CCC">(click name to edit query)</span></th>
        <th class="twelve wide">Query <span style="color:#CCC">(click to view formatted code)</span></th>
        <th class="one wide">Details</th>
      </tr>
    </thead>
    <tbody class="data">
      <tr
        v-for="row in queries"
        :key="row">
        <td @click="setSelectedQuery($event,row)">{{ row.QNAME.replace(app+' ','') }}</td>
        <td
          class="trigger-codemirror"
          @click="showCode"><div class="code">{{ row.QUERY }}</div></td>
        <QueryDetails
          :qname="row.QNAME.toLowerCase().replace(/\s+/g,'-')"
          :info="[row.LAST_ACCESS,row.ACCESS_COUNT,row.CREATED,row.CREATED_BY,row.MODIFIED,row.MODIFIED_BY]"
          :comments="row.COMMENTS"
          :settings="row.DEFAULT_PARAMS"
          :security="row.IS_SECURE"/>
      </tr>
    </tbody>
  </table>
</template>
<script>
import * as types from '../store/vuex-types'
import QueryDetails from './QueryDetails'
import { mapState } from 'vuex'

const CodeMirror = require('codemirror')
export default {
  components: { QueryDetails },
  data () {
    return {
      filterLabel: 0
    }
  },
  methods: {
    // click handler for table row
    setSelectedQuery: function (e, row) {
      if (e.target.className !== 'copy btn') // exclude clippy clickss
      {
        let qname = `${row.QNAME}`
        this.$store.commit(types.SET_QNAME, qname)
        this.$store.commit(types.SET_QNAMEORIG, qname)
        this.$store.commit(types.SET_QUERY, row)
        this.$emit('query-selected')
        let el = document.querySelector('#query-edit-tab')
        el.classList.remove('disabled')
        el.setAttribute('data-tab', el.id.replace(/panel/), 'tab')
        el.click()
      }
    },

    showCode: function (e) {
      setTimeout(() => {
        let td, code
        if (e.target.tagName === 'TD') // click is outside code area
        {
          td = e.target
          code = e.target.firstChild
        }
        else // click is in code div
        {
          td = e.target.parentElement
          code = e.target
        }
        const hidden = $('.hidden')
        if (!!code) // && !this.adjusting)
        {
          // if syntax highlighting is present, allow selection
          if (code.closest('.CodeMirror') !== null
            || code.parentElement.querySelector('.CodeMirror') !== null)
          {
            code.focus()
          }
          else // toggle syntax
          {
            const onComplete = () => {
              // clean up
              Array.from(document.querySelectorAll('#query-list-panel .CodeMirror')).forEach(cm => {
                cm.parentElement.removeChild(cm)
              })
              hidden.transition({ animation: 'fade in', duration: '1s' })

              // create anew
              const query = code.textContent
              // let width = td.offsetWidth
              CodeMirror(td, { value: query,
                mode: 'text/x-sql',
                lineNumbers: true,
                lineWrapping: true,
                readOnly: true,
                theme: 'eclipse',
                workDelay: 50,
                scrollbarStyle: 'null'})
              let cmEl = document.querySelector('#query-list-panel .CodeMirror-scroll')
              cmEl.style.maxWidth = td.offsetWidth - 27
              $(cmEl).transition({ animation: 'fade in', duration: '.5s' })
            }
            $(code).transition({ animation: 'fade out', duration: '.5s', onComplete: onComplete })
          }
        }
      }, 100)
    },
    tooltip: (title, content) => {
      return `<span class="ui" data-tooltip="${title}">${content}</span>`
    }
  },
  computed: {
    ...mapState(['queries', 'query', 'app', 'qname'])
  },
  watch: {
    queries (neo, old) {
      this.filterLabel = this.queries.length
    }
  },
  updated () {

  },
  mounted () {

  }
}
</script>
<style>
  caption {
    caption-side: top;
    margin-left: auto;
  }

  table.sticky > thead th {
    position: sticky !important;
    top:0px !important;
    padding-bottom: 11px !important;
  }

  table.sticky td {

  }

  div.code {
    overflow: hidden;
    white-space: wrap;
    text-overflow: ellipsis;
  }

  div.filter {
    position: fixed;
    left: 15;
    top: 75px;
    z-index: 1000;
  }

  div.filter .ui.input {
    width: 240px;
  }

  tr.stripe {
    background-color: rgba(0, 0, 50, 0.02);
  }
</style>
