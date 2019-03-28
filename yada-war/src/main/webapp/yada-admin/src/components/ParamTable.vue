<template>
  <table class="display cell-border"></table>
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

  },
  computed: mapGetters(['getQname','getParamTableRows']),
  watch: {
    rowData (val, oldVal) {
      let vm = this; //vm = view model
      vm.rows = val.filter(o => o[1] == this.getQname); //[];

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
      columnDefs:[
        {
          targets:1,
          render:function(data,type,row,meta) {
            var qname = row[1];
            var id = 'name-'+qname.replace(/\s/g,'-')+'-'+meta.row;
            var elem = '<input type="text" id="'+id+'" value="'+data+'"/>';
            return elem;
          }
        },
        {
          targets:2,
          render:function(data,type,row,meta) {
            var qname = row[1];
            var id = 'value-'+qname.replace(/\s/g,'-')+'-'+meta.row;
            var elem;
            if(data == 'true' || data == 'false')
            {
              elem  = '<input type="radio" name="'+id+'" value="true"'+(data == 'true' ? ' checked="true"' : '')+'/>True';
              elem += '<input type="radio" name="'+id+'" value="false"'+(data == 'false' ? ' checked="true"' : '')+'/>False';
            }
            else if(data =='asc' || data == 'desc')
            {
              elem  = '<input type="radio" name="'+id+'" value="asc"'+(data == 'asc' ? ' checked="true"' : '')+'/>Ascending';
              elem += '<input type="radio" name="'+id+'" value="desc"'+(data == 'desc' ? ' checked="true"' : '')+'/>Descending';
            }
            else
              elem = '<input type="text" id="'+id+'" value="'+data+'"/>';
            return elem;
          }
        },
        {
          targets:3,
          render:function(data,type,row,meta) {
            var qname = row[1];
            var index = qname.replace(/\s/g,'-')+'-'+meta.row;
            var elem  = '<input type="radio" name="rule-'+index+'" value="1" '+(data==1?'checked="true"':'')+'>Non-overridable';
                elem += '<input type="radio" name="rule-'+index+'" value="0" '+(data==0?'checked="true"':'')+'>Overridable';
            return elem;
          }
        }
      ],
      columns:[
        {title:"Id",visible:false,name:"ID"},
        {title:"Target",visible:false},
        {title:"Parameter",sortable:false},
        {title:"Value",sortable:false},
        {title:"Mutability",sortable:false},
        {title:"Action",sortable:false,
          defaultContent:'<button type="button" class="fa fa-save fa-save-md" title="Save"><button type="button" class="fa fa-remove fa-remove-md" title="Remove" style="color:red"/><button type="button" class="fa fa-plus fa-plus-md" title="Add Another" style="color:green"/>'},
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
