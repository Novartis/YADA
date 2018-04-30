<template>
  <div id="list" class="list-group" role="tablist">
    <AppListItem
      v-for="app in apps"
      :app="app.APP"/>
  </div>
</template>

<script>
import AppListItem from './AppListItem.vue'
export default {
  components: { AppListItem },
  name: 'AppList',
  data() {
    return {
      selected: undefined,
      apps: null
    }
  },
  methods: {
    login () {
      let q = 'YADA check credentials', p = ['YADA','yada'].join(',')
      this.$yada.std(q,p,'POST')
        .then(this.fetch_data())
    },
    fetch_data () {
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
