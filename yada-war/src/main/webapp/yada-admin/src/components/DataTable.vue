<template>
  <table class="display cell-border"><caption><h2>{{getApp}}</h2></caption></table>
</template>
<script>
import Vue from 'vue'
import * as types from '../store/mutation-types'
import { mapGetters } from 'vuex';
export default {
  props: ['rowData'],
  data() {
    return {
      rows: [] ,
      dtHandle: null
    }
  },
  methods: {
    // click handler for table row
    setSelectedQuery: function(e) {
      if(e.target.className != 'copy btn') // exclude clippy clickss
      {
        let row   = this.dtHandle.row(e.currentTarget)
        let col   = 0
        let qname = this.dtHandle.cell(row,col).data()
        let query = this.getQueries.filter(q => q.QNAME == qname)[0]
        this.$store.commit(types.SET_QNAME,qname)
        this.$store.commit(types.SET_QUERY,query)
        this.$emit('query-selected')
      }
    }
  },
  computed: mapGetters(['getApp','getQueries','getQname','getQuery']),
  watch: {
    rowData (val, oldVal) {
      let vm = this; //vm = view model
      vm.rows = val; //[];

      // Here's the magic to keeping the DataTable in sync.
      // It must be cleared, new rows added, then redrawn!
      vm.dtHandle.clear();
      vm.dtHandle.rows.add(vm.rows);
      vm.dtHandle.draw();
    }
  },
  mounted() {
    let vm = this;
    // Instantiate the datatable and store the reference to the instance in our dtHandle element.
    vm.dtHandle = $(this.$el).DataTable({
      columnDefs: [ {
        targets: 0,
        render: function(data, type, row, meta) {
          let btn = '<button class="copy btn" data-clipboard-target="tr:nth-child('+(meta.row+1)+')>td:nth-child('+(meta.col+1)+')"></button>';
          return btn+data;
        }
      },{
        targets: 1,
        render: function(data, type, row, meta)
        {
          let btn = '<button class="copy btn" data-clipboard-target="tr:nth-child('+(meta.row+1)+')>td:nth-child('+(meta.col+1)+')"></button>';
          let pre = '<pre>' + data.replace(/</g, "&lt;").replace(/>/g, "&gt;") + '</pre>'
          return btn+pre;
        }
      }, {
        targets: 2,
        render: function(data, type, row, meta)
        {
          let btn = '<button class="copy btn" data-clipboard-target="tr:nth-child('+(meta.row+1)+')>td:nth-child('+(meta.col+1)+')"></button>';
          let code = data.replace(/</g, "&lt;").replace(/>/g, "&gt;");
          return btn+code;
        }
      }, {
        targets: 3,
        render: function(data, type, row, meta)
        {
          var len = 12;
          if (type === 'display' && data.length > len)
          {
            return '<span data-toggle="tooltip" title="' + data + '">' + data.substr(0, len) + '…</span>';
          }
          return data; // "short display", or filter
        }
      }
      ,{
        targets: [ 5, 7
        ], // date columns
        type: "date",
        className: 'dt-body-center',
        render: function(data, type, row, meta)
        {
          if (data == "")
            return data;
          if (data == null)
            return "";
          var txt = Vue.getFormattedDate(data, "oracle");
          return '<span title="' + txt + '">' + txt.substr(0, 10) + '</span>';
        }
      }
      ],
      columns: [
      // {
      //   "className": '',
      //   "orderable": false,
      //   // "data": null,
      //   "defaultContent": '<span class="fa fa-chevron-circle-right fa-md"></span>',
      //   visible: false
      // },
      {
        // data: "QNAME",
        title: "Qname",
        searchable: true
      }, {
        // data: "QUERY",
        title: "Query",
        name: "QueryCode",
        searchable: true
      }, {
        // data: "QUERY",
        title: "Query",
        name: "QueryText",
        searchable: true,
        visible: false
      }, {
        // data: "COMMENTS",
        title: "Comments",
        searchable: true
      }, {
        // data: "DEFAULT_PARAMS",
        title: 'Default Param Count',
        type: 'num',
        searchable: false,
        className: 'dt-body-center'
      }, {
        // data: "LAST_ACCESS",
        title: "Last Accessed",
        searchable: false
      }, {
        // data: "ACCESS_COUNT",
        title: "Access Count",
        searchable: false,
        type: 'num',
        className: 'dt-body-right'
      }, {
        // data: "MODIFIED",
        title: "Last Modified",
        searchable: false
      }, {
        // data: "MODIFIED_BY",
        title: "Modified By",
        searchable: false
      }
      ],
      data: vm.rows,
      //searching: false,
      //paging: false,
      //info: false,
      //autoWidth: false,
      "lengthMenu": [ [ 5, 10, 25, 50, 75, -1
      ], [ 5, 10, 25, 50, 75, 'All'
      ]
      ],
      dom: '<"top"f>t<"bottom-l"i><"bottom-c"p><"bottom-r"l>',
      language: {
        search: 'Filter on qname, query, or comments:',
        searchPlaceholder: 'Filter...'
      },
    });
    // row click handler
    $('tbody').on('click','tr',vm.setSelectedQuery)
  }
}
</script>
<style>
  caption {
    caption-side: top;
    margin-left: 50%
  }

  table.dataTable tbody td {
    vertical-align: top;
  }

</style>
