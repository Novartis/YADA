<template>
  <div class="app config">
    <form class="ui form">
      <div class="inline fields">
        <div
          v-if="creating"
          class="five wide field">
          <label>Code {{ creating }}</label>
          <input
            name="app"
            type="text"
            :value="typeof config === 'undefined' || config === null ? '' : config.APP"
            @input="updateModel">
        </div>
        <div
          v-else
          class="five wide field">
          <label>Code {{ creating }}</label>
          <input
            name="app"
            type="text"
            :value="typeof config === 'undefined' || config === null ? '' : config.APP"
            readonly>
        </div>
        <div class="three wide field">
          <div class="ui toggle checkbox">
            <input
              type="checkbox"
              name="active"
              :checked="checked"
              @change="updateModel">
            <label>Active</label>
          </div>
        </div>
      </div>
      <div class="field">
        <label>Name</label>
        <input
          name="name"
          type="text"
          :value="typeof config === 'undefined' || config === null ? '' : config.NAME"
          @input="updateModel">
      </div>
      <div class="field">
        <label>Description</label>
        <input
          name="descr"
          type="text"
          :value="typeof config === 'undefined' || config === null? '' : config.DESCR"
          @input="updateModel">
      </div>
      <div class="field">
        <label>Configuration</label>
        <textarea
          id="conf"
          rows="20"
          v-if="typeof config === 'undefined' || config === null"/>
        <textarea
          id="conf"
          rows="20"
          v-else
          v-model="config.CONF"/>
      </div>
    </form>
  </div>
</template>
<script>
import { mapState } from 'vuex'
import * as types from '../store/vuex-types'
import 'codemirror/lib/codemirror.css'
import 'codemirror/theme/eclipse.css'
import 'codemirror/mode/shell/shell.js'
const CodeMirror = require('codemirror')

export default {
  components: { },
  data () {
    return {
      cm: null
    }
  },
  methods: {
    updateModel (e) {
      let active = document.querySelector('input[name="active"]').checked ? 1 : 0
      let app    = document.querySelector('input[name="app"]').value
      let conf   = this.cm.getValue()
      let descr  = document.querySelector('input[name="descr"]').value
      let name   = document.querySelector('input[name="name"]').value
      let mod    = { ACTIVE: active, APP: app, CONF: conf, DESCR: descr, NAME: name }
      this.$store.commit(types.SET_CONFIG, mod)
      this.unsaved()
    },

    makeCM (value) {
      let vm = this
      setTimeout(() => {
        let ta = document.getElementById('conf')
        if (vm.cm === null)
        {
          vm.cm = CodeMirror.fromTextArea(ta, {
            lineNumbers: true,
            theme: 'eclipse',
            mode: 'text/x-sh'
          })
          vm.cm.on('change', (i, obj) => {
            if (obj.origin !== 'setValue')
            {
              vm.unsaved()
            }
            vm.$set(vm.config, 'CONF', vm.cm.getValue())
          })
        }
        setTimeout(() => {
          vm.cm.refresh()
        }, 10)
      }, 10)
    }
  },
  computed: {
    ...mapState(['loading', 'saving', 'config', 'creating']),
    checked () { return typeof this.config === 'undefined' || this.config === null ? '' : this.config.ACTIVE === '1' ? 'checked' : '' }
  },
  updated () {

  },
  mounted () {
    $('[type = "checkbox"]').checkbox()
  },
  watch: {
    config (neo, old) {
      this.debounce(250, function () {
        if (typeof neo !== 'undefined' && neo !== null)
        {
          if (this.cm !== null)
          {
            this.cm.setValue(neo.CONF)
            setTimeout(() => {
              this.cm.refresh()
            }, 10)
          }
        }
      })
    }
  }
}
</script>
<style>
.app.config .CodeMirror {
  height: 370 !important;
}
.app.config label {
  text-align: left;
}
.app.config {
  height: 600px;
  text-align: left !important;
}
input[readonly=readonly] {
  background-color: #EEE !important;
}
</style>
