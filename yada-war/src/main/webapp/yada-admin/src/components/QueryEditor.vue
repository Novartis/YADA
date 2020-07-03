<template>
  <div class="query-editor-view">
    <div class="ui fluid accordion qname">
      <h5 class="title active">
        <i class="dropdown icon"/>
        Qname
      </h5>
      <div class="content active">
        <div
          v-if="renaming || creating"
          class="ui fluid labeled icon input">
          <div class="ui label">
            {{ app }}
          </div>
          <input
            name="qname"
            type="text"
            :value="qname"
            @input="rename">
        </div>
        <div
          v-else
          class="ui fluid labeled icon input disabled">
          <div class="ui label">
            {{ $1 }}
          </div>
          <input
            name="qname"
            type="text"
            :value="qname"
            readonly
            @input="rename">
        </div>
      </div>
    </div>

    <div class="ui fluid accordion">
      <h5 class="title active">
        <i class="dropdown icon"/>
        Code
      </h5>
      <div class="content active">
        <div class="query-editor">
          <CodeMirrorWrap/>
        </div>
      </div>
    </div>

    <div class="ui fluid accordion">
      <h5
        class="title"
        :class="{active:!!comment}">
        <i class="dropdown icon"/>
        Comments
      </h5>
      <div
        class="ui form content comment"
        :class="{active:!!comment}">
        <div
          v-if="!!comment && !!!editComment"
          @click="setMode($event)"
          v-html="escComment"/>
        <div
          class="field"
          v-else>
          <!-- <textarea name="comment" class="comment" @input="debounce(unsaved, 250)" @blur="setMode($event)">{{ $1 }}</textarea> -->
          <textarea
            name="comment"
            class="comment"
            @input="unsaved"
            @blur="setMode($event)"
            v-model="comment"/>
        </div>
      </div>
    </div>
    <div class="ui fluid accordion">
      <h5 class="title">
        <i class="dropdown icon"/>
        Default YADA Query-level Parameters
      </h5>
      <div class="content parameters">
        <div>
          <ParamTable ref='paramtab'/>
        </div>
      </div>
    </div>
    <div class="ui fluid accordion security">
      <h5 class="title secconf">
        <i class="dropdown icon"/>
        Security Configuration
      </h5>
      <div class="content security">
        <SecurityConfig/>
      </div>
    </div>
  </div>
</template>
<script>

import Vue from 'vue'
import { mapState } from 'vuex'
import CodeMirrorWrap from './CodeMirrorWrap.vue'
import ParamTable from './ParamTable.vue'
import SecurityConfig from './SecurityConfig.vue'
import * as types from '../store/vuex-types'
export default {
  components: { CodeMirrorWrap, ParamTable, SecurityConfig },
  data () {
    return {
      qname: '',
      code: '',
      comment: '',
      editComment: false
    }
  },
  methods: {
    rename: function (e) {
      // set the local qname property to the new field content
      this.qname = document.querySelector('input[name="qname"]').value
      // set a local var
      let qname = `${this.app} ${this.qname}`
      // change the qname in the state and also in the query object
      this.$store.commit(types.SET_QNAME, qname)
      this.$set(this.query, 'QNAME', qname)
      // increment unsaved count
      this.unsaved()
    },
    addRow: function (e) {
      this.$refs.paramtab.addRow(e)
    },
    cancel: function (event) {

    },
    setMode: function (event) {
      // change comment property
      // let ta = document.querySelector('textarea.comment')
      // this.$set(this.query, 'COMMENT', ta!==null?ta.value:this.query.COMMENT)
      // console.log(ta !== null ? ta.value : this.query.COMMENT)

      // toggle MODE
      if (!!!this.editComment)
      {
        this.editComment = true
        Vue.nextTick(() => {
          document.querySelector('textarea.comment').focus()
        })
      }
      else
      {
        this.editComment = false
      }
    }
  },
  computed: {
    ...mapState(['qnameOrig', 'query', 'app', 'renaming', 'creating', 'cloning', 'unsavedChanges', 'unsavedParams', 'activeTab', 'loggeduser', 'queries']),
    escComment () { return this.comment.replace(/\n/g, '<br/>') }
  },
  mounted () {
    $('.ui.accordion').accordion()
  },
  updated () {

  },
  watch: {
    unsavedChanges (neo, old) {
      // only process if we're viewing the edit form
      if (this.activeTab === 'query-edit-tab')
      {
        let app   = this.app
        let qname = document.querySelector('input[name="qname"]').value
        let date  = new Date().toISOString().substr(0, 19).replace(/T/, ' ')
        let commentsEl = document.querySelector('textarea[name="comment"]')
        let comments = commentsEl !== null ? commentsEl.value : this.query.COMMENTS
        let user = this.loggeduser
        let query = {
          APP: app,
          QNAME: `${app} ${qname}`,
          COMMENTS: comments,
          MODIFIED: date,
          MODIFIED_BY: user,
          QUERY: this.query.QUERY,
          ACCESS_COUNT: this.query.ACCESS_COUNT,
          CREATED: this.query.CREATED,
          CREATED_BY: this.query.CREATED_BY
        }
        // if (this.renaming)
        // {
        //   query.ACCESS_COUNT = this.query.ACCESS_COUNT
        //   query.CREATED = this.query.CREATED
        //   query.CREATED_BY = this.query.CREATED_BY
        // }
        // only process if unsaved changes exist and NOT in creation mode (renaming, standard edit)
        if (!this.creating)
        {
          this.$store.commit(types.SET_QUERY, query)
          let index = this.queries.findIndex(q => { return q.QNAME === `${app} ${qname}` })

          if (index > -1)
          {
            this.$set(this.queries, index, query)
          }
        }
      }
    },
    query (neo, old) {
      if (neo !== null)
      {
        this.qname = neo.QNAME.replace(`${this.app} `, '')
        this.code = neo.QUERY
        this.comment = neo.COMMENTS
        // if (/^[0-9]+$/.test(this.qname))
        // {
        //   this.$store.commit(types.SET_RENAMING, true)
        // }
        if (this.creating)
        {
          this.unsaved()
        }
      }
    },
    renaming (neo, old) {
      if (!!neo)
      {
        let input = document.querySelector('.query-editor-view .qname input')
        let rename = 'RENAME '
        // is it a brand new query, named like 'APP 8098712341'?
        // if (/(.+\s)?[0-9]+$/.test(input.value))
        // {
        //   rename = ''
        // }
        document.querySelector('.query-editor-view .qname .disabled').classList.remove('disabled')
        input.removeAttribute('readonly')
        input.focus()
        input.value = `${rename}${input.value}`
        input.select()
      }
    },
    cloning (neo, old) {
      if (!!neo)
      {
        // this.qname = this.qname+' CLONE'
        this.$store.dispatch(types.SAVE_QUERY)
          .then(() => {
            this.qname = this.$store.state.qname.replace(`${this.app} `, '')
          })
      }
    },
    unsavedParams (neo, old) {
      let panel = document.querySelector('.parameters')
      if (neo > 0 && panel.computedStyleMap().get('display').value === 'none')
      {
        panel.closest('.accordion').querySelector('h5').click()
      }
    }
  }
}
</script>
<style>

  .qname input[readonly="readonly"] {
    border: none;
  }

  .query-editor-view .accordion {
    margin-top: 5px;
  }

  .query-editor-view .ui.segment,
  .query-editor-view .content,
  .query-editor-view .title {
    text-align: left;
  }

  .query-editor-view .content {
    padding-left: 15px !important;
  }

  .query-editor-view .content.comment {
    color: #3F7F5F;
  }

  .query-editor-view .qname .disabled {
    opacity: 1 !important;
  }

  .ui.header {
    background-color: rgb(249, 250, 251) !important;
    border-top: 1px solid #D4D4D5 !important;
    border-radius: .25rem .25rem 0px 0px !important;
  }

  .ui.labeled.input > .label:not(.corner) {
    padding-top: 1em !important;
  }

  .flash {
    background-color: orange;
  }

</style>
