<template>
  <div id="list" class="list-group" role="tablist">
    <AppListItem
      v-for="app in apps"
      :key="app.APP"
      :app="app.APP" />
      <!-- @YADA_queries_loaded_app="YADA_queries_loaded_app"/> -->
  </div>
</template>

<script>
import AppListItem from './AppListItem.vue'
export default {
  components: { AppListItem },
  name: 'AppList',
  data () {
    return {
      apps: null
    }
  },
  methods: {
    // YADA_queries_loaded_app (obj) { this.$emit('YADA_queries_loaded_app',obj) },
    login () {
      let q = 'YADA check credentials'
      let p = ['YADA', 'yada'].join(',')
      this.$yada.std(q, p).then(this.loadApps)
    },
    loadApps () {
      let q = 'YADA select apps'
      this.$yada.std(q,null,{c:false})
        .then(response => this.setApps(response))
    },
    setApps (response) {
      this.apps = response.data.RESULTSET.ROWS.sort((a,b)=>{ return a.APP.localeCompare(b.APP, 'en',{sentivity: 'base'})})
    }
  },
  mounted () {
    this.$nextTick(() => this.login())
  }
}
</script>

<style scoped>

</style>
