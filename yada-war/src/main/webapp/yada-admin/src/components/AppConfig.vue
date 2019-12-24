<template>
  <div class="app config">
    <form v-if="config !== null" class="ui form">
      <div class="inline fields">
        <div v-if="creating" class="five wide field">
          <label>Code {{creating}}</label>
          <input  name="app" type="text" :value="config.APP" @input="updateModel">
        </div>
        <div v-else class="five wide field">
          <label>Code {{creating}}</label>
          <input name="app" type="text" :value="config.APP" readonly>
        </div>
        <div class="three wide field">
          <div class="ui toggle checkbox">
            <input type="checkbox" name="active" :checked="checked" @change="updateModel">
            <label>Active</label>
          </div>
        </div>
      </div>
      <div class="field">
        <label>Name</label>
        <input name="name" type="text" :value="config.NAME" @input="updateModel">
      </div>
      <div class="field">
        <label>Description</label>
        <input name="descr" type="text" :value="config.DESCR" @input="updateModel">
      </div>
      <div class="field">
        <label>Configuration</label>
        <textarea id="conf" rows="20">{{config.CONF}}</textarea>
      </div>

    </form>

  </div>
</template>
<script>
import { mapState } from 'vuex'
import * as types from '../store/vuex-types'
// import Vue from 'Vue'
const CodeMirror = require('codemirror')
import 'codemirror/lib/codemirror.css'
import 'codemirror/theme/eclipse.css'
import 'codemirror/mode/shell/shell.js'
export default {
  components: { },
  data(){
    return {
      cm: null
    };
  },
  methods: {
    updateModel(e) {
      let that = this
      let active = document.querySelector('input[name="active"]').checked ? 1 : 0
      let app    = document.querySelector('input[name="app"]').value
      let conf   = this.cm.getValue()
      let descr  = document.querySelector('input[name="descr"]').value
      let name   = document.querySelector('input[name="name"]').value
      let mod    = { ACTIVE:active,APP:app,CONF:conf,DESCR:descr,NAME:name }
      this.$store.commit(types.SET_CONFIG, mod)
      this.unsaved()
    },

    makeCM(value) {
      let vm = this
      let cmr = vm.cm
      setTimeout(() => {
        let ta = document.getElementById('conf')
        if(cmr == null)
        {
          cmr = CodeMirror.fromTextArea(ta,{
            lineNumbers: true,
            theme:'eclipse',
            mode: 'text/x-sh',
          })
          cmr.on('change',(i,obj) => {
            if(obj.origin !== 'setValue')
            {
              vm.unsaved()
            }
            vm.$set(vm.config,'CONF',cmr.getValue())
          })
          vm.cm = cmr
        }
        setTimeout(() => {
          cmr.refresh()
        },100)
      },100)

    }
  },
  computed:{
    ...mapState(['loading','saving','config','creating']),
    checked() { return this.config.ACTIVE == "1" ? 'checked' : '' }
  },
  updated() {

  },
  mounted() {
    $('[type = "checkbox"]').checkbox()
  },
  watch: {
    config(neo,old) {
      // console.log('neo',neo,'old',old)
      if(typeof neo !== 'undefined' && neo !== null)
      {
        // console.log(neo.CONF)
        if(!!this.cm)
        {
          this.cm.setValue(neo.CONF)
          setTimeout(() => {
            this.cm.refresh()
          },100)
        }
        else
        {
          this.makeCM(neo.CONF)
        }
        // let hasMods = Object.keys(neo).filter(a => {
        //   return neo[a] !== old[a]
        // })
      }
    }
  }

}
</script>
<style>
  .app.config label {
    text-align: left;
  }
  .app.config {
    height: 400px;
    text-align: left !important;
  }
  input[readonly=readonly] {
    background-color: #EEE !important;
  }
</style>
