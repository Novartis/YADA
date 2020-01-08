<template>
  <div class="main-menu ui sticky" tabindex="-1">
    <div class="ui vertical mini grey menu inverted menu-button" tabindex="-1" @click="makeActive">
        <div class="ui fluid selection dropdown item">
        Actions
        <i class="bars icon"></i>
        <div class="menu">
          <MenuItem v-for="item in menuitems" :item="item"/>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import MenuItem from './MenuItem.vue'
import * as types from '../store/vuex-types'
import { mapState } from 'vuex';
export default {
  name: 'Menu',
  props: ['menuitems'],
  components: { MenuItem },
  methods: {
    makeActive: (e) => {
      let el = e.currentTarget
      if(el.classList.contains('active'))
        el.classList.remove('active')
      else
        el.classList.add('active')
    },
  },
  computed: mapState(['unsavedChanges','tabs','activeTab']),
  watch: {
    activeTab (neo,old) {
      this.$store.commit(types.SET_MENUITEMS, this.tabs[this.activeTab.replace(/-tab/,'')].menuitems)
    }
  },
  mounted() {
    $('.main-menu .ui.dropdown').dropdown()
    $('.main-menu.sticky').sticky({context:'body',silent:true})
  }
}
</script>
<style>

.main-menu {
  position: fixed !important;
  right: 14px !important;
  top: 63;
}

.main-menu > .ui.vertical.menu.menu-button  {
  font-weight: bold !important;
  border-radius: 0px !important;
  border-bottom-left-radius: 5px !important;
  border-bottom-right-radius: 5px !important;
}

.main-menu > .ui.vertical-.menu.menu-button.active  {
  border-bottom-left-radius: 0px !important;
  border-bottom-right-radius: 0px !important;
}

.main-menu > .prompt {
  display: none;
}

.main-menu > .prompt.show {
  display: block;
  position: fixed !important;
  right: 12px;
  padding-top: 3px !important;
  font-weight: bold;
  -moz-animation: bounce 2s infinite;
  -webkit-animation: bounce 2s infinite;
  animation: bounce 2s infinite;
}

.main-menu > .prompt.show > i.icon {
  position: fixed !important;
  right: 44px !important;
  bottom: 20px !important;
}

.ui.selection.dropdown,
.ui.selection.dropdown:hover,
.ui.selection.dropdown.active:hover,
.ui.selection.dropdown:focus,
.ui.selection.dropdown.active:focus {
  border: 0px solid rgba(0,0,0,0.05) !important;
}

.main-menu .ui.vertical.menu.menu-button .menu {
  top: 30px !important;
  right: 0px !important;
  width: calc(100%) !important;
  left: auto !important;
  border: 1px solid rgba(0,0,0,0.15);
  background-color: rgba(249,222,212,.8);
  border-top-right-radius: 0px !important;
}

.main-menu.hidden {
  display: none;
}


</style>
