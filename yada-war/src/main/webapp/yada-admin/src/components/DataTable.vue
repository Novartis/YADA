<template>
  <table class="display cell-border"></table>
</template>
<script>
import Vue from 'vue'
export default {
  props: ['rowData'],
  data() {
    return {
      // headers: [
      //   { title: 'Qname' },
      //   { title: 'Query' },
      //   { title: 'Query' },
      //   { title: 'Comments' },
      //   { title: 'Default_Parameters' },
      //   { title: 'Last_Accessed' },
      //   { title: 'Access_Count' },
      //   { title: 'Last_Modified' },
      //   { title: 'Modified_By' }
      // ],
      rows: [] ,
      dtHandle: null
    }
  },
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
        targets: 2,
        render: function(data, type, row, meta)
        {
          return '<pre>' + data.replace(/</g, "&lt;").replace(/>/g, "&gt;") + '</pre>';
        }
      }, {
        targets: 3,
        render: function(data, type, row, meta)
        {
          return data.replace(/</g, "&lt;").replace(/>/g, "&gt;");
        }
      }, {
        targets: 4,
        render: function(data, type, row, meta)
        {
          var len = 12;
          if (type === 'display' && data.length > len)
          {
            return '<span data-toggle="tooltip" title="' + data + '">' + data.substr(0, len) + '…</span>';
          }
          return data; // "short display", or filter
        }
      }, {
        targets: [ 6, 8
        ], // date columns
        type: "date",
        className: 'dt-body-center',
        render: function(data, type, row, meta)
        {
          if (data == "")
            return data;
          var txt = Vue.getFormattedDate(data, "oracle");
          return '<span title="' + txt + '">' + txt.substr(0, 10) + '</span>';
        }
      }
      ],
      columns: [
      // {
      //   "className": '',
      //   "orderable": false,
      //   "data": null,
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
        title: 'Default Parameters',
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
      searching: false,
      paging: false,
      info: false,
      autoWidth: false,
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
  }
}
</script>
<style>
</style>
