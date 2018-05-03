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
    //YADA_queries_loaded_app (obj) { this.$emit('YADA_queries_loaded_app',obj) },
    login () {
      let q = 'YADA check credentials', p = ['YADA','yada'].join(',')
      this.$yada.std(q,p,'POST')
        .then(this.loadApps())
    },
    loadApps () {
      this.$yada.path('YADA select apps')
        .then(response => this.setApps(response))
    },
    setApps (response) {
      this.apps = response.data.RESULTSET.ROWS
    }
  },
  mounted () {
    this.$nextTick(() => this.login());
  }
}
</script>

<style scoped>

</style>
