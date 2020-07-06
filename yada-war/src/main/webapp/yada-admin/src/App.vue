<template>
  <div id="app">
    <div class="meaty-bit">
      <div class="background" >
        <img
          class="box-shadow"
          src="../static/blox250.png">
        <span>Admin</span>
      </div>
      <div class="ui top attached tabular menu">
        <div
          class="item active"
          id="apps-tab"
          data-tab="apps-tab"
          @click="clearApp">Apps</div>
        <div
          class="item disabled"
          id="conf-tab"
          ref="conftab"
          @click="setupConfTab">Configuration</div>
        <div
          class="item disabled"
          id="query-list-tab"
          ref="querylisttab">Queries</div>
        <div
          class="item disabled"
          id="query-edit-tab"
          ref="queryedittab">Edit Query</div>
      </div>
      <div
        class="ui bottom attached active tab segment"
        data-tab="apps-tab"
        id="apps-panel">
        <AppList/>
      </div>
      <div
        class="ui bottom attached tab segment"
        data-tab="conf-tab"
        ref="confpanel"
        id="conf-panel">
        <AppConfig ref="confpanelform"/>
      </div>
      <div
        class="ui bottom attached tab segment"
        data-tab="query-list-tab"
        ref="querylistpanel"
        id="query-list-panel">
        <QueryList/>
      </div>
      <div
        class="ui bottom attached tab segment"
        data-tab="query-edit-tab"
        ref="queryeditpanel"
        id="query-edit-panel">
        <QueryEditor/>
      </div>
    </div>
    <div
      v-if="contextmenu.length > 0"
      class="ui vertical compact menu contextmenu">
      <div class="header item">Special Actions:</div>
      <a
        v-for="item in contextmenu"
        :key="item"
        class="item contextmenuitem">{{ item }}</a>
    </div>
    <!-- <IdleAlert/> -->
    <div class="ui mini modal saving">
      <div class="content">
        <p>Saving...</p>
      </div>
    </div>
    <div class="ui mini modal warn">
      <div class="content">
        <p>You are trying to save an invalid value.</p>
        <div class="actions">
          <div class="ui positive right floated labeled icon button dang">
            Dang
            <i class="checkmark icon"/>
          </div>
        </div>
      </div>
    </div>
    <div class="ui mini modal confirm">
      <div class="content">
        <p>{{ confirm }}</p>
        <div class="actions">
          <div class="ui black deny right button">
            Nope
          </div>
          <div class="ui positive right labeled icon button">
            Yup
            <i class="checkmark icon"/>
          </div>
        </div>
      </div>
    </div>
    <Menu :menuitems="menuitems"/>
    <TableFilter
      :filter="filter.render"
      :stripe="filter.stripe"
      :selector="filter.selector"/>
  </div>
</template>

<script>
// TODO undo ux
// TODO save ux with prompt
// TODO delete ux with prompt
// TODO cancel ux

import IdleAlert from './components/IdleAlert.vue'
import AppList from './components/AppList.vue'
import AppConfig from './components/AppConfig.vue'
import QueryList from './components/QueryList.vue'
import QueryEditor from './components/QueryEditor.vue'
import Menu from './components/Menu.vue'
import TableFilter from './components/TableFilter.vue'
import * as types from '@/store/vuex-types'
import { mapState } from 'vuex'

/* eslint-disable-next-line camelcase */
const rx_neoapp = /APP[0-9]+/

export default {
  name: 'App',
  components: { IdleAlert, AppList, AppConfig, QueryList, QueryEditor, TableFilter, Menu },
  data () {
    return {
      env: process.env.NODE_ENV_LABEL,
      modalSave: null,
      modalConfirm: null,
      modalWarn: null
    }
  },
  methods: {
    clearApp () {
      // this.trace()
      [this.$refs.conftab,
        this.$refs.querylisttab,
        this.$refs.queryedittab
      ]
        .forEach(el => {
          el.classList.add('disabled')
        })
    },
    setupConfTab () {
      if (!this.$refs.conftab.classList.contains('disabled'))
        this.$refs.confpanelform.makeCM()
    },
    initTabs () {
      $('.menu > .item').tab({
        deactivate: 'all',
        // debug:true,
        // verbose:true,
        onVisible: (p) => {
          this.$store.commit(types.SET_ACTIVETAB, p)
          this.$store.dispatch(types[`ACTIVATE_${p.replace(/-/g, '').toUpperCase()}`])
        }
      })
    }
  },
  mounted () {
    let vm = this
    // set event listener for clicks to inject save prompt before navigation
    Array.from(document.querySelectorAll('.menu > .item')).forEach(el => {
      el.addEventListener('click', e => {
        e.preventDefault()
        // store clicked tab
        let nextTab = e.target
        // only proceed if click is on different, enabled tab
        if (/.+-tab/.test(nextTab.id)
           && !nextTab.classList.contains('disabled')
           && nextTab.id !== vm.activeTab)
        {
          // do stuff even if nothing changed:
          //   close 'Security' section on query edit
          //   done with pure js -- semantic api doesn't work here
          //
          //
          const queryEditorView = document.querySelector('.query-editor-view')
          const securityAccordion = queryEditorView.querySelector('.ui.accordion.security')
          securityAccordion.querySelectorAll('.active').forEach(el => {
            el.classList.remove('active')
          })

          // stuff to do ONLY if something changed:
          if (vm.unsavedChanges > 0)
          {
            vm.$store.commit(types.SET_NEXTTAB, nextTab.id)
            // disable tab temporarily to prevent navigation before save
            // this is necessary to delay 'onVisible' handler which clears state
            // required for saving
            nextTab.classList.add('disabled')
            let context = 'query-edit'
            if (/conf/.test(vm.activeTab))
            {
              context = 'app'
            }
            // configger and trigger modal only if real mouse click
            // the screenx, screeny coords should only be present when actually clicking
            // we'll see if cypress imposes it
            if (!!e.screenX && e.screenX !== 0 && !!e.screenY && e.screenY !== 0)
              vm.$store.dispatch(types.SAVE_CHANGES_CONFIRM, context)
          }
        }
        return false
      })
    })
    // initialize tabs
    vm.initTabs()
    // initialize modal
    this.modalSave = $('#app > .ui.mini.modal.saving').modal({
      // name:'Saving',
      // debug:true,
      // verbose:true,
      inverted: true
    })
    this.modalWarn = $('#app > .ui.mini.modal.warn').modal({
      // name:'Confirming',
      // debug:true,
      // verbose:true,
      // closable: true,
      inverted: true,
      onApprove: function () {
        vm.$store.commit(types.SET_SHOWWARNING, false)
      }
      // onApprove: function () {
      //   vm.$store.dispatch(types[vm.confirmAction]).then((r) => {
      //     // unsetting flag now _seems_ early but necessary to support prompt
      //     // plus it's happening after the promise resolves.
      //     // TODO check for error handling - what happens if 'confirmAction' fails?
      //     vm.$store.commit(types.SET_UNSAVEDCHANGES, 0)
      //     vm.$nextTick(() => {
      //       if (!!vm.nextTab)
      //       {
      //         // force hide the modals to enable clicking, then enable it and click
      //         vm.modalSave.modal('hide')
      //         vm.modalConfirm.modal('hide')
      //         let nextTab = document.querySelector(`#${vm.nextTab}`)
      //         vm.$store.commit(types.SET_NEXTTAB, null)
      //         nextTab.classList.remove('disabled')
      //         nextTab.click()
      //       }
      //     })
      //   })
      // }
    })

    this.modalConfirm = $('#app > .ui.mini.modal.confirm').modal({
      // name:'Confirming',
      // debug:true,
      // verbose:true,
      closable: false,
      inverted: true,
      onApprove: function () {
        vm.$store.dispatch(types[vm.confirmAction]).then((r) => {
          // unsetting flag now _seems_ early but necessary to support prompt
          // plus it's happening after the promise resolves.
          // TODO check for error handling - what happens if 'confirmAction' fails?
          vm.$store.commit(types.SET_UNSAVEDCHANGES, 0)
          vm.$nextTick(() => {
            if (!!vm.nextTab)
            {
              // force hide the modals to enable clicking, then enable it and click
              vm.modalSave.modal('hide')
              vm.modalConfirm.modal('hide')
              let nextTab = document.querySelector(`#${vm.nextTab}`)
              vm.$store.commit(types.SET_NEXTTAB, null)
              nextTab.classList.remove('disabled')
              nextTab.click()
            }
          })
        })
      },
      onDeny: function (a) {
        // reset state, renable clicking, and click
        if (/SAVE/.test(vm.confirmAction))
        {
          vm.$store.commit(types.SET_UNSAVEDCHANGES, 0)
        }
        vm.$nextTick(() => {
          vm.$store.commit(types.SET_CONFIRM, null)
          vm.$store.commit(types.SET_CONFIRMACTION, null)
          vm.$store.commit(types.SET_PARAM, null)
          if (!!vm.nextTab)
          {
            let nextTab = document.querySelector(`#${vm.nextTab}`)
            vm.$store.commit('SET_NEXTTAB', null)
            nextTab.classList.remove('disabled')
            nextTab.click()
          }
        })
      }
    })
    this.$store.commit(types.SET_ACTIVETAB, 'apps-tab')

    // scroll handler
    window.addEventListener('scroll', (e) => {
      // blur filter
      document.querySelector('div.filter > div.ui.input > input').blur()

      // move menu to top of screen when scrolled past header
      let menu = document.querySelector('.main-menu')
      let filter = document.querySelector('.filter')
      let th
      if (vm.activeTab === 'query-list-tab')
      {
        th = document.querySelector('.query.list table thead th')
        if (window.scrollY === 0)
        {
          menu.style.top = 63
          filter.style.top = 75
        }
        else if (window.scrollY < 63)
        {
          menu.style.top = 63 - window.scrollY
          filter.style.top = 75 - window.scrollY
        }
        else if (window.scrollY < 123) // if (window.scrollY < newTop)
        {
          menu.style.top = (123 + th.offsetHeight) - window.scrollY
          filter.style.top = (123 + th.offsetHeight) - window.scrollY
        }
        else
        {
          menu.style.top = th.offsetHeight
          filter.style.top = th.offsetHeight
        }
      }
      else if (vm.activeTab === 'query-edit-tab' || vm.activeTab === 'apps-tab')
      {
        if (window.scrollY === 0)
        {
          menu.style.top = 63
          filter.style.top = 75
        }
        else if (window.scrollY < 63)
        {
          menu.style.top = 63 - window.scrollY
          filter.style.top = 75 - window.scrollY
        }
        else
        {
          menu.style.top = 0
          filter.style.top = 0
        }
      }
    })

    document.addEventListener('keydown', (e) => {
      if (/(?:query-edit|conf)-tab/.test(vm.activeTab) && e.keyCode === 83 && e.metaKey) // Cmd-s
      {
        e.preventDefault()
        this.$store.dispatch(types.SAVE, {})
      }
      else if (/(?:query-edit|conf)-tab/.test(vm.activeTab) && e.keyCode === 113) // F2
      {
        e.preventDefault()
        let el = document.querySelector('.CodeMirror-scroll')
        if (el.closest('.CodeMirror-fullscreen') !== null)
        {
          el.style.maxHeight = window.visualViewport.height - 100
        }
        else
        {
          el.style.maxHeight = '200px'
        }
        let menu = document.querySelector('.main-menu.ui.sticky')
        if (menu.classList.contains('hidden'))
        {
          menu.classList.remove('hidden')
        }
        else
        {
          menu.classList.add('hidden')
        }
      }
      else if (/(?:query-list|apps)-tab/.test(vm.activeTab) && e.keyCode === 70 && e.metaKey) // Cmd-f
      {
        e.preventDefault()
        let el = document.querySelector('div.filter > div.ui.input > input')
        el.focus()
        setTimeout(() => {
          if (el.value.length < 4)
            el.value = ''
        }, 50)
      }
    })
  },
  computed: {
    ...mapState(['showWarning', 'nextTab', 'contextmenu', 'coords', 'unsavedChanges', 'app', 'menuitems', 'filter', 'activeTab', 'qname', 'query', 'saving', 'creating', 'confirm', 'confirmAction'])
  },
  watch: {
    showWarning (neo, old) {
      if (neo)
      {
        this.modalWarn.modal('show')
      }
    },
    unsavedChanges (neo, old) {
      let bg = document.querySelector('.background')
      if (neo > 0)
      {
        bg.classList.add('unsaved')
        bg.querySelector('span').innerText = 'Unsaved Changes'
      }
      else
      {
        bg.classList.remove('unsaved')
        bg.querySelector('span').innerText = 'Admin'
      }
    },
    confirm (neo, old) {
      if (neo)
        this.modalConfirm.modal('show')
      else
        this.modalConfirm.modal('hide')
    },
    saving (neo, old) {
      if (neo)
        this.modalSave.modal('show')
      else
        this.modalSave.modal('hide')
    },
    contextmenu (neo, old) {
      if (neo.length > 0)
      {
        this.$nextTick(() => {
          let el = document.querySelector('.contextmenu')
          el.style.left = this.coords[0] - 10
          el.style.top = this.coords[1] - 10
          el.classList.add('visible')
        })
      }
    },
    app (val, oldVal) {
      if (!!val)
      {
        [this.$refs.conftab,
          this.$refs.querylisttab
        ].forEach(el => {
          el.setAttribute('data-tab', el.id.replace(/panel/, 'tab'))
          // go to config tab if app code matches new app rx
          // otherwise go to queries tab
          if (!!!this.nextTab)
          {
            if (rx_neoapp.test(val) && /(?:conf)-tab/.test(el.id))
            {
              el.classList.remove('disabled')
              el.click()
              this.unsaved()
            }
            else if (!rx_neoapp.test(val))
            {
              el.classList.remove('disabled')
              if (/query-list-tab/.test(el.id))
                el.click()
            }
          }
          else
          {
            if (/(?:conf)-tab/.test(el.id))
              el.classList.remove('disabled')
            document.querySelector(`#${this.nextTab}`).click()
          }
        })
      }
    },
    query (neo, old) {
      if (neo !== null)
      {
        let el = document.querySelector('#query-edit-tab')
        el.classList.remove('disabled')
        el.setAttribute('data-tab', el.id.replace(/panel/), 'tab')
        el.click()
      }
      else
      {
        let el = document.querySelector('#query-edit-tab')
        el.classList.add('disabled')
      }
    },
    qname (neo, old) {
      // delete query will set qname to empty string, triggering
      // switch to querylist tab
      if (neo === '')
      {
        this.$store.dispatch(types.LOAD_APP, this.app)
          .then(() => {
            this.$refs.querylisttab.click()
          })
      }
    }
  }
}
</script>

<style>

.meaty-bit {
  background-color: rgb(249, 222, 212);
  top: 0px;
  position: absolute;
  width: 100%;
}

.meaty-bit > .background {
  background-color: rgb(249, 222, 212);
  height: 19px;
}

.meaty-bit .background img {
  margin: 5px 8px -35px 3px;
  width: 50px;
  float: right;
}

.meaty-bit > .ui.top.attached.tabular.menu {
  margin-top: -1px;
}

.copy.btn {
  display: inline-block;
  font-size: 14px;
  vertical-align: top;
  user-select: none;
  padding: 0;
  position: relative;
  top: -3px;
  float: right;
  border: 1px solid rgb(0, 0, 0, 0.2);
  background-image: linear-gradient(#fcfcfc, #eee);
  opacity: 0;
}

.copy.btn:before {
  content:"";
  background-repeat: no-repeat;
  background-image: url('../static/clippy.svg');
  background-position: 50%;
  background-size: 14px;
  display: block;
  height: 26px;
  width: 26px;
  vertical-align: top;
}

*:hover>.copy.btn {
  opacity: 1;
}

#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-bottom: 100px;
}

.box-shadow {
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.5);
}

.ui.menu.contexmenu {
  border: none;
}

.ui.menu.vertical.compact.contextmenu {
  display: none;
  margin: 0;
  min-height: 0;
  background-color: #767676 !important;
  border: none;
}

.ui.menu.vertical.compact.contextmenu.visible {
  display: inline;
  position: absolute;
}

.ui.menu.vertical.compact.contextmenu .header.item,
.ui.menu.vertical.compact.contextmenu .item {
  padding: 6px;
  background-color: #767676;
  color: rgba(255, 255, 255, 0.9);
  font-size: smaller;
  border: 1px solid rgba(0, 0, 0, 0.15);
}

.ui.menu.vertical.compact.contextmenu .item:not(.header) {
  border-top-right-radius: 0px;
  border-top-left-radius: 0px;
  border-bottom-left-radius: 4px;
}

.ui.menu.vertical.compact.contextmenu .item:not(.header):hover {
  background-color: rgba(249, 222, 212, .8);
  color: rgba(0, 0, 0, 0.87);
}

.ui.menu.vertical.compact.contextmenu .header.item {
  text-align: left;
  border-bottom: 1px solid rgba(255, 255, 255, 0.9);
}

.filtered-out {
  display: none !important;
}

.meaty-bit .background.unsaved {
  background-color: red;
}

.meaty-bit .background.unsaved span {
  color: white;
  font-weight: bold;
}

.ui.button.dang {
  margin-bottom: 20px;
}
</style>
