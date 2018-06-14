<template>
  <div>
    <DataTable :row-data="getTableRows" @query-selected="querySelected"></DataTable>
    <QueryEditor v-if="showEditor"/>
  </div>
</template>
<script>
import DataTable from './DataTable.vue'
import QueryEditor from './QueryEditor.vue'
import * as types from '../store/mutation-types'
import { mapGetters } from 'vuex';

export default {
  components: { DataTable, QueryEditor },
  data () {
    return {
      showEditor: false,
      rowData: null
    }
  },
  methods: {
    querySelected() {
      this.showEditor=true
      this.$store.dispatch(types.LOAD_PARAMS)
    },
    refreshCodeMirror() {
      let cm = $('#query-editor').data('codemirror')
      setTimeout(() => cm.refresh(), 1)
    },
    clearCodeMirror(e) {
      this.showEditor = false;
    }
  },
  computed: mapGetters(['getTableRows','getQname','getQuery']),
  watch: {

  },
  mounted() {
    $(this.$el).on("shown.bs.modal", this.refreshCodeMirror)
    $(this.$el).on("hidden.bs.modal", this.clearCodeMirror)
  }
}
</script>
<style>
</style>
