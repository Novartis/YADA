<template>
  <div class="query list">
    <QueryTable @query-selected="querySelected"></QueryTable>
  </div>
</template>
<script>
import QueryTable from './QueryTable.vue'
import QueryEditor from './QueryEditor.vue'
import * as types from '../store/vuex-types'
import { mapState } from 'vuex';

export default {
  components: { QueryTable, QueryEditor },
  data () {
    return {
    }
  },
  methods: {
    querySelected() {
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
  computed: {

  },
  watch: {

  },
  mounted() {
    $(this.$el).on("shown.bs.modal", this.refreshCodeMirror)
    $(this.$el).on("hidden.bs.modal", this.clearCodeMirror)
  }
}
</script>
<style>
  .query.list {
    margin-top: 15px;
  }
</style>
