<template>
  <div class="item applistitem"
    :id="app"
    role="tab"
    :data-app="app"
    data-toggle="list"
    @click="loadApp"
    @mouseenter="killpopups">{{app}}
    <!-- @contextmenu="contextMenu" disabling this for now-->
    <!-- <a class="" href="#">{{app}}</a> -->
  </div>
</template>

<script>
  import store from '../store'
  import * as types from '../store/vuex-types'
  import { mapState } from 'vuex'

  export default {
    name: 'AppListItem',
    props: ['app'],
    data () {
      return {
        contextmenuitems: ['Delete App']
      }
    },
    methods: {
      changeTab(tab) {
        document.querySelector(`.item[data-tab="${tab}"]`).click()
      },
      loadApp(e) {
        // console.log(e)
        // if(this.app == e.target.dataset.app)
        // {
        //   this.$store.dispatch(types.LOAD_APP,null)
        //   .then(() => {
        //     this.$store.dispatch(types.LOAD_APP,e.target.dataset.app)}
        //   )
        // }
        // else
        // {
          this.$store.dispatch(types.LOAD_APP,e.target.dataset.app)
          .then(() => {
            // console.log(JSON.stringify(this.config))
          })
        // }
      },
      contextMenu(e) {
        e.preventDefault()
        let that = this
        this.$el.classList.add('selected')
        this.$store.commit(types.SET_CONTEXTMENU, [])
        this.$store.commit(types.SET_CONTEXTMENU, [`Delete ${this.app}`])
        this.$store.commit(types.SET_COORDS, [e.pageX, e.pageY])
        console.log(e,this)
      },
      killpopups(e) {
        Array.from(document.querySelectorAll('.selected')).forEach(el => {
          el.classList.remove('selected')
        })
        Array.from(document.querySelectorAll('.contextmenu.visible')).forEach(el => {
          el.classList.remove('visible')
        })
      }
    },
    computed: {
      ...mapState(['config','contextmenu'])
    }
  }
</script>

<style scoped>
  .ui.list .item.applistitem:hover,
  .ui.list .item.applistitem.selected {
    background-color: rgba(249,222,212,.8);
  }
</style>
