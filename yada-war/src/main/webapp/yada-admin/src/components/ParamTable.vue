<template>
  <div class="params">
    <table class="ui sticky celled table paramtab">
      <thead>
        <tr>
          <th class="one wide" data-tooltip="rollover and drag to change order" data-position="top left">Order</th>
          <th class="three wide">Parameter</th>
          <th class="ten wide">Value <span style="color:#CCC">(click cell to edit)</span></th>
          <th class="one wide">Mutability</th>
          <th class="one wide">Action</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="param,idx in params" @dragover="dragover($event)" @dragenter="dragenter($event)" @dragleave="dragleave($event)" @dragstart="dragstart($event)" @drop="dragdrop($event)">
          <!-- ID (rank) -->
          <td class="ui one wide center aligned" @mouseenter="toggleDraggers" @mouseleave="toggleDraggers">
            <i :class="idx == 0 ? 'sort down' : idx == params.length - 1 ? 'sort up' : 'sort'" class="icon hidden" ></i>
            {{param.ID}}
          </td>
          <!-- NAME -->
          <td class="ui three wide center aligned" :data-content="getParamName(param)" data-position="top center" @click="setMode($event,param,idx)">
            <div>{{param.NAME}}</div>
            <div class="hide">
              <div class="ui search selection compact dropdown parameter-selector">
                <input type="hidden" name="parameter" value="">
                <i class="dropdown icon"></i>
                <div class="default text">Select parameter</div>
                <div class="menu">
                  <div v-for="param in paramlist" class="item" :data-value="param.alias" :data-content="param.tip" data-position="right center">{{param.alias}} ({{param.name}})</div>
                </div>
              </div>
            </div>
          </td>
          <!-- VALUE -->
          <td class="ui nine wide collapsing" nowrap :data-content="getTooltip(param)"  @click="setMode($event,param,idx)">
            <div v-if="!!!mode[idx] || mode[idx] !== 'edit' || isSecurityParam(param)">
              {{param.VALUE}}
            </div>
            <div v-else-if="getParamType(param) == 'Number'" class="ui input">
              <input type="number" :value="param.VALUE" :min="getParamSpec(param).min" :max="getParamSpec(param).max" required @blur="setMode($event,param,idx)">
            </div>
            <div v-else-if="getParamType(param) == 'String'" class="ui fluid input">
              <input type="text" :value="param.VALUE" :pattern="getParamSpec(param).pattern" required  @blur="setMode($event,param,idx)">
            </div>
            <div v-else-if="getParamType(param) == 'Boolean'" class="">
              <div class="ui toggle checkbox paramval" :class="param.VALUE == 'true' ? 'checked' : ''">
                <input v-if="param.VALUE == 'true'" type="checkbox" :name="`param.NAME-${idx}`" checked >
                <input v-else type="checkbox" :name="`param.NAME-${idx}`">
                <label>True</label>
              </div>
            </div>
          </td>
          <!-- RULE -->
          <td class="ui one wide center aligned">
            <span :data-content="!!param.RULE ? (isSecurityParam(param) ? 'Security params are never permitted to be overrideable' : 'NOT overrideable in URL') : 'overrideable in URL'">
              <!-- <i :class="mutable(param)" class="large icon" @click="toggleMutability($event,param)"></i> -->
              <div class="ui toggle fitted checkbox mutability" :class="isSecurityParam(param) ? 'disabled' : ''">
                <input type="checkbox" :name="`mutability-${idx}`">
                <label></label>
              </div>
            </span>
          </td>
          <!-- ACTION (delete) -->
          <td class="ui two wide center aligned" @mouseenter="toggleButton" @mouseleave="toggleButton">
            <div class="ui center aligned">
              <button class="ui tiny icon red button delete hidden" v-if="true" @click="deleteRow(idx)" data-tooltip="Delete Parameter" data-position="top right">
                <i class="small delete icon"></i>
              </button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
    <!-- <button class="ui tiny icon green button right labeled add-row" data-tooltip="Add Parameter" data-position="top right" @click="addRow">
      Add Row
      <i class="plus icon"></i>
    </button> -->
    <div class="ui mini negative modal">
      <i class="close icon"></i>
      <div class="header">
        Alert!
      </div>
      <div class="content">
        <p>Are you sure you want to delete this parameter? This is <span style="font-style:italic;">NOT undoable!</span></p>
      </div>
      <div class="actions">
        <div class="ui cancel button">Cancel</div>
        <div class="ui negative approve button" @click="deleteRow">OK</div>
      </div>
    </div>
  </div>
</template>
<script>
// TODOs
//TODO data entry validator message
//TODO exception handling

import Vue from 'vue'
import * as types from '../store/vuex-types'
import { mapState } from 'vuex';
export default {
  props: ['rowData'],
  data() {
    return {
      rows: [] ,
      draggedRow: null,
      dragging: false,
      currentRowIdx: null,
      neoRow: null
    }
  },
  methods: {
    // toggles delete button on mouse over
    // editing: function(idx) {
    //   return this.params[idx].MODE
    // },
    toggleButton: function(e) {
      let button = e.currentTarget.querySelector('button.delete')
      if(button.classList.contains('hidden'))
        button.classList.remove('hidden')
      else
        button.classList.add('hidden')
    },

    // storeIndex: function(idx) {
    //   this.currentRowIdx = idx
    // },
    // addRow: function() {
    //   let row = {'TARGET':this.qname,'ID': (this.getMaxId() + 1), 'NAME':'','VALUE':'','RULE':1}
    //   this.neoRow = row
    //   this.params.push(row)
    // },
    deleteRow: function(idx) {
      let param = this.params[idx]
      this.$store.dispatch(types.DEL_PARAM_CONFIRM, param)
      this.$store.commit(types.SET_UNSAVEDPARAMS,this.unsavedParams+1)
    },

    //*** DND METHODS
    // sets directional arrows and cursor for dragging
    // makes rows draggable
    toggleDraggers: function() {
      Array.from(document.querySelectorAll('.paramtab tbody > tr > td:first-child .icon')).forEach(el => {
        if(el.classList.contains('hidden'))
        {
          el.classList.remove('hidden')
          el.closest('tr').setAttribute('draggable',true)
        }
        else
        {
          el.classList.add('hidden')
        }
      })
    },
    // updates icons in dragged rows to keep directions sane
    updateDraggers: function() {
      Array.from(document.querySelectorAll('.paramtab tbody > tr > td:first-child i.icon')).forEach(el => {
        el.classList.remove('sort','down','up')
        let idx  = el.closest('tr').rowIndex
        let icon = idx == 1 ? 'sort down' : idx == this.params.length ? 'sort up' : 'sort'
        el.classList.add(...icon.split(' '))
      })
    },
    // this disables dragover event, not sure why 'row' is set--it currently does noting
    dragover: function(e) {
      e.preventDefault()
      let row = e.target.closest('tr')
    },
    // updates row css on exit
    dragleave: function(e) {
      e.preventDefault()
      let row = e.target.closest('tr')
      row.classList.remove('dropzone')
    },
    // updates row css on enter, 'id' and 'did' are set but have not impact currently
    dragenter: function(e) {
      this.dragging = true
      let row = e.target.closest('tr')
      if(!!!this.draggedRow)
      {
        this.draggedRow = row
      }
      else
        row.classList.add('dropzone')
      let id = parseInt(row.querySelector('td:first-child').textContent)
      let did = parseInt(this.draggedRow.querySelector('td:first-child').textContent)
    },
    // sets 'row' and 'id' vars but does nothing with them
    dragstart: function(e) {
      let row = e.target.closest('tr')
      row.classList.add('dragging')
      let id = parseInt(row.querySelector('td:first-child').textContent)
      console.log(row.rowIndex, id)
    },
    // disables dnd,
    dragdrop: function(e) {
      let row   = e.target.closest('tr')
      row.removeAttribute('draggable')
      this.draggedRow.classList.remove('dragging')

      let id    = row.rowIndex //parseInt(row.querySelector('td:first-child').textContent)
      let did   = this.draggedRow.rowIndex //parseInt(this.draggedRow.querySelector('td:first-child').textContent)
      let tbody = document.querySelector('table.paramtab tbody')
      if(id < did)
        row.insertAdjacentElement('beforebegin',this.draggedRow)
      else
        row.insertAdjacentElement('afterend',this.draggedRow)
      Array.from(tbody.querySelectorAll('tr.dropzone')).forEach(el => el.classList.remove('dropzone'))
      this.draggedRow = null
      this.updateIds()
      this.dragging = false
      this.updateDraggers()
      this.$store.commit(types.SET_UNSAVEDPARAMS,this.unsavedParams+1)
    },
    // resets ids in params objects to row indexes
    updateIds: function() {
      let vm = this
      let rowDict = Array.from(document.querySelectorAll('.params tbody > tr')).reduce((a,c) => {
        a[parseInt(c.querySelector('td:first-child').textContent)] = c.rowIndex-1
        return a
      },{})
      console.log(rowDict)
      vm.params.forEach((param,idx) => {
        console.log(param.ID,idx)
        param['OLDID'] = param['ID']
        if(vm.dragging)
          param['ID'] = rowDict[param['ID']]
        else
          param['ID'] = idx
        vm.$set(vm.renderedParams, idx, param)
      })
      console.log(vm.renderedParams.map(p => p.ID))
      this.unsaved()
    },

    setMode: function(event,param,idx) {
      console.log(event.currentTarget)
      let vm = this
      // if mode == 'EDIT', current target will be INPUT
      if(event.currentTarget.tagName == 'INPUT')
      {
        let input = event.currentTarget
        // if edit just occurred and tabbing out, event.type == 'blur'
        if(event.type == 'blur')
        {
          delete param.MODE
          if(input.validity.valid)
          {
            // change stored value and set flags for saving
            if(param.VALUE != input.value)
            {
              param.VALUE = input.value
              // global unsavedChanges flag
              vm.unsaved()
              // unsavedParams flag
              vm.$store.commit(types.SET_UNSAVEDPARAMS,this.unsavedParams+1)
            }
          }
          else
          {
            input.parentElement.classList.add('error')
          }
          vm.$set(vm.params,idx,param)

        }
        else if(event.type == 'click' )
        {
          return false
        }
      }
      else
      {
        // change value column to input for editing
        let index = event.currentTarget.cellIndex
        let td = event.currentTarget
        // do nothing for sec param
        if(this.isSecurityParam(param))
          return false
        // toggle MODE
        if(index == 2)
        {
          if(param.MODE == 'edit')
          {
            if(event.type == 'click' && this.getParamType(param) == 'Boolean')
            {
              return false
            }
          }
          else
          {
            param['MODE'] = 'edit'
          }
          this.$set(this.params,idx,param)

          Vue.nextTick(() => {
            if(!!param.MODE)
            {
              let input = td.querySelector('input')
              input.select()
            }
            else
            {
              // update store
              this.$set(this.params,idx,param)
            }
          })
        }
        // change parameter column to input for editing
        else if(index == 1)
        {
          let els, tgt = event.currentTarget
          if(tgt.tagName === 'TD')
            els = tgt.closest('td').querySelectorAll('div')
          else
            els = tgt.querySelectorAll('div')
          Array.from(els).forEach(el => {
            if(el.classList.contains('hide'))
            {
              el.classList.remove('hide')
              $(el.querySelector('.parameter-selector')).dropdown(
                {onChange: (value, text, $choice) => {
                  let idx = $choice[0].closest('tr').rowIndex-1
                  let param = vm.params[idx]
                  param.NAME = value
                  param.VALUE = vm.getParamDefault(param)
                  vm.$set(vm.params,idx,param)
                  delete param.MODE
                  vm.unsaved()

                }
              }).dropdown('show')
            }
            else
            {
              el.classList.add('hide')
            }
          })
        }
      }
    },
    isSecurityParam: function(param) {
      return /(auth.path|(execution|content).policy)/.test(param.VALUE)
    },
    mutable: function(param) {
      return !!param.RULE ? 'toggle off black' : 'toggle on red'
    },
    getParamSpec: function(param) {
      return this.paramlist.filter(p => param.NAME == p.alias)[0]
    },
    getParamType: function(param) {
      return typeof this.getParamSpec(param) !== 'undefined' ? this.getParamSpec(param).type : 'choose a parameter'
    },
    getParamDefault: function(param) {
      return typeof this.getParamSpec(param) !== 'undefined' ? this.getParamSpec(param).default : 'choose a parameter'
    },
    getTooltip: function(param) {
      return this.isSecurityParam(param)
              ? `Security parameters must be edited in the Security Configuration panel above`
              : typeof this.getParamSpec(param) !== 'undefined' ? this.getParamSpec(param).tip : 'choose a parameter'
    },
    getParamName: function(param) {
      return typeof this.getParamSpec(param) !== 'undefined' ? this.getParamSpec(param).name : 'choose a parameter'
    },
  },
  computed: {
    ...mapState(['paramlist','renderedParams','qname','unsavedChanges','confirmAction','unsavedParams']),
    mode() { return this.params.map(p => {return p.MODE}) },
    sortedParams() { return this.params.sort((a,b) => {return parseInt(a.ID) - parseInt(b.ID)})},
    params() { return this.renderedParams }
  },
  watch: {
    confirmAction(neo,old) {
      if(neo === null)
      {
        // this.updateIds()
      }
    },
  },
  mounted() {

  },
  updated() {
    let vm = this
    $('[data-content]').popup()
    $('.params .ui.tiny.modal').modal('attach events', 'button.delete', 'show')
    $('.checkbox.mutability').checkbox({onChange: function() {
      let rule = this.checked ? 0 : 1
      let idx  = this.closest('tr').rowIndex-1
      let param = vm.params[idx]
      param.RULE = rule
      vm.$set(vm.params,idx,param)
      vm.unsaved()
      vm.$store.commit(types.SET_UNSAVEDPARAMS,this.unsavedParams+1)
    }})
    $('.checkbox.paramval').checkbox({
        onChange: function() {
        let value = !!this.checked ? 'true' : 'false'
        let idx  = this.closest('tr').rowIndex-1
        let param = vm.params[idx]
        param.VALUE = value
        // delete param.MODE
        vm.$set(vm.params,idx,param)
        setTimeout(() => {
          delete param.MODE
          vm.$set(vm.params,idx,param)
          vm.unsaved()
          vm.$store.commit(types.SET_UNSAVEDPARAMS,vm.unsavedParams+1)
        },1000)
      }
    })
  }
}
</script>
<style>

  table.dataTable tbody td {
    vertical-align: top;
  }

  .paramtab .ui.popup {
    /* color: rgba(0,0,0,0,87) !important; */
  }

  .paramtab td {
    padding: 5px !important;
  }

  .paramtab td input {
    padding-top: 2px !important;
    padding-bottom: 2px !important;
  }

  .hidden {
    visibility: hidden;
  }

  .hide {
    display: none;
  }

  button {
    padding: 5px;
  }

  tr[draggable] td:first-child {
    cursor: grab;
  }

  tr.dragging td:first-child {
    cursor: grabbing;
  }

  tr.dropzone > td {
    border-top: 12px solid rgb(249,222,212) !important;
    border-bottom: 12px solid rgb(249,222,212) !important;
    background: rgb(249,222,212) !important;
    color: rgb(249,222,212) !important;
  }

  tr.dropzone > td:first-child {
    border-left: 2px solid rgb(249,222,212) !important;
  }

  tr.dropzone > td:last-child {
    border-right: 2px solid rgb(249,222,212) !important;
  }

  .add-row {
    position: absolute;
    right: 11;
    margin-top: -15px !important;
    margin-left: 11px !important;
    border-top-left-radius: 0px !important;
    border-top-right-radius: 0px !important;
  }

</style>
