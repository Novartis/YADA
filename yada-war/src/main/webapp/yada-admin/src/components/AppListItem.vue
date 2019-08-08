<template>
  <a class="list-group-item list-group-item-action"
      :id="app"
      href="#"
      role="tab"
      :data-app="app"
      @click="loadApp"
      data-toggle="list">{{app}}</a>
</template>

<script>
  import store from '../store'
  import * as types from '../store/mutation-types'
  export default {
    name: 'AppListItem',
    props: ['app'],
    data () {
      return {

      }
    },
    methods: {
      loadApp(e) {
        this.$store.dispatch(types.LOAD_APP,e.target.dataset.app)
      }
    }
  }
</script>

<style scoped>

</style>
