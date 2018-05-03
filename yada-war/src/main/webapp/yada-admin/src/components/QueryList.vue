<template>
  <div>
    <h3>Vue Datatable example</h3>
    Filter by anything: <input v-model="search">
    <hr>
    <DataTable :row-data="tableRows"></DataTable>
  </div>
</template>
<script>
import DataTable from './DataTable.vue'
import { mapGetters } from 'vuex';

export default {
  components: { DataTable },
  data () {
    return {
      rowData: null,
      search: null
    }
  },
  computed: mapGetters(['tableRows'])
}
</script>
<style>
</style>
