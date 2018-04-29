<template>
  <div>
    <ul class="nav nav-pills">
      <li class="nav-item">
        <a class="nav-link" href="#">Apps</a>
      </li>
    </ul>
    <ul class="list-group">
      <AppListItem
        v-for="app in apps"
        :app="app.APP"/>
    </ul>
  </div>
</template>

<script>
import AppListItem from './AppListItem.vue'
export default {
  components: { AppListItem },
  name: 'AppList',
  data() {
    return {
      apps: null
    }
  },
  methods: {
    login() {
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
