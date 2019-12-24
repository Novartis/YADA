<template>
  <div class="query-editor-view">
    <div class="ui fluid accordion qname">
      <h5 class="title active">
        <i class="dropdown icon"></i>
        Qname
      </h5>
      <div class="content active">
        <div v-if="renaming || creating" class="ui fluid labeled icon input">
          <div class="ui label">
            {{app}}
          </div>
          <input name="qname" type="text" :value="qname" @input="rename"/>
        </div>
        <div v-else class="ui fluid labeled icon input disabled">
          <div class="ui label">
            {{app}}
          </div>
          <input name="qname" type="text" :value="qname" readonly @input="rename"/>
        </div>
      </div>
    </div>

    <div class="ui fluid accordion">
      <h5 class="title active">
        <i class="dropdown icon"></i>
        Code
      </h5>
      <div class="content active">
        <div class="query-editor">
          <CodeMirrorWrap/>
        </div>
      </div>
    </div>

    <div class="ui fluid accordion">
      <h5 class="title" :class="{active:!!comment}">
        <i class="dropdown icon"></i>
        Comments
      </h5>
      <div class="ui form content comment" :class="{active:!!comment}">
        <div v-if="!!comment && !!!editComment" @click="setMode($event)">{{comment}}</div>
        <div class="field" v-else>
          <textarea name="comment" class="comment" @input="unsaved" @blur="setMode($event)">{{comment}}</textarea>
        </div>
      </div>
      <h5 class="title">
        <i class="dropdown icon"></i>
        Default Parameters
      </h5>
      <div class="content hidden">
        <div>
          <ParamTable ref='paramtab'/>
        </div>
      </div>
      <h5 class="title">
        <i class="dropdown icon"></i>
        Security Configuration
      </h5>
      <div class="content hidden">
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
// import Menu from './Menu.vue'
import * as types from '../store/vuex-types'
// import utils from '../mixins/utils'
export default {
  // mixins: [utils],
  components: { CodeMirrorWrap, ParamTable, SecurityConfig },
  data() {
    return {
      qname: '',
      code: '',
      comment: '',
      editComment: false,
      // renaming: false
    }
  },
  methods: {
    /*
     * Updates QNAME, QUERY, COMMENTS, DATE MOD, NAME MOD
     */
    saveQuery: function(e) {
      console.log('save',e,this,'test')
      return new Promise((res,rej) => {
        if(this.renaming)
        {
          // check uniqueness
          let val = document.querySelector('.qname input').value
          this.$store.dispatch(types.CHECK_UNIQ, val)
          .then((r) => {
            console.log(r.data)
          })
          .catch((err) => {
            console.log(err)
          })
        }
      })
      .then(() => {

      })
    },
    rename: function(e) {
      let qname = document.querySelector('input[name="qname"]').value
      this.$store.commit(types.SET_QNAME, qname)
      this.unsaved()
    },
    addRow: function(e) {
      this.$refs.paramtab.addRow(e)
    },
    cancel: function(event) {

    },
    setMode: function(event) {
      // toggle MODE
      if(!!!this.editComment)
      {
        this.editComment = true
        Vue.nextTick(() => {
          document.querySelector('textArea.comment').focus()
        })
      }
      else
      {
        this.editComment = false
        console.log('Saving...')
      }
    }
  },
  computed: {
    ...mapState(['qnameOrig','query','app','renaming','creating','cloning','unsavedChanges','activeTab','loggeduser'])
  },
  mounted() {
    $('.ui.accordion').accordion()
  },
  updated() {

  },
  watch: {
    unsavedChanges(neo,old) {
      if(neo > 0 && this.activeTab == 'query-edit-tab')
      {
        let app   = this.app
        let qname = document.querySelector('input[name="qname"]').value
        let date  = new Date().toISOString().substr(0,19).replace(/T/,' ')
        let commentsEl = document.querySelector('textarea[name="comment"]')
        let comments = commentsEl != null ? commentsEl.value : this.query.COMMENTS
        let user = this.loggeduser
        let query = {
          APP: app,
          QNAME: qname,
          COMMENTS: comments,
          MODIFIED: date,
          MODIFIED_BY: user,
          QUERY: this.query.QUERY
        }
        if(this.creating || this.renaming)
        {
          query.ACCESS_COUNT = this.query.ACCESS_COUNT
          query.CREATED = this.query.CREATED
          query.CREATED_BY = this.query.CREATED_BY
        }
        this.$store.commit(types.SET_QUERY, query)
      }
    },
    query(neo,old) {
      if(neo !== null)
      {
        this.qname = neo.QNAME.replace(this.app+' ','')
        this.code = neo.QUERY
        this.comment = neo.COMMENTS
        if(/^[0-9]+$/.test(this.qname))
        {
          this.$store.commit(types.SET_RENAMING,true)
        }
      }
    },
    renaming(neo,old) {
      if(!!neo)
      {
        let input = document.querySelector('.query-editor-view .qname input')
        let rename = 'RENAME '
        // is it a brand new query, named like 'APP 8098712341'?
        // if(/(.+\s)?[0-9]+$/.test(input.value))
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
    cloning(neo,old) {
      if(!!neo)
      {
        this.$store.dispatch(types.SAVE_QUERY)
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
