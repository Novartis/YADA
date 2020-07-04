<template>
  <div
    class="filter"
    :class="{hide:hide}">
    <div class="ui right labeled input">
      <input
        type="text"
        placeholder="Filter row by reg exp...">
      <div class="ui right label">
        {{ filterLabel }}
      </div>
    </div>
  </div>
</template>

<script>
import * as types from '../store/vuex-types'
import { mapState } from 'vuex'

export default {
  name: 'TableFilter',
  data () {
    return {
      filterSel: 'div.filter > div.ui.input > input',
      filterLabel: '',
      rows: []
    }
  },
  methods: {
    clearFilter: function () {
      let filter = document.querySelector(this.filterSel)
      filter.value = ''
      Array.from(document.querySelectorAll('.filtered-out'))
        .forEach(row => {
          row.classList.remove('filtered-out', 'stripe')
        })
      filter.dispatchEvent(new Event('clear'))
    },
    filtering: function (e) {
      // console.log(e.currentTarget.value)
      try
      {
        let rx = new RegExp(e.currentTarget.value, this.filter.rxflags)
        this.rows = Array.from(document.querySelectorAll(this.filter.selector))
        this.rows.forEach(row => {
          row.classList.remove('filtered-out', 'stripe')
        })
        this.rows
          .filter(row => !rx.test(row.textContent))
          .forEach(row => row.classList.add('filtered-out'))
        if (this.filter.stripe)
          this.stripe()
        this.filterLabel = this.getFilterLabel()
      }
      catch (err)
      {
        console.log(err)
      }
    },
    stripe: function () {
      let filtered = Array.from(document.querySelectorAll(`${this.filter.selector}:not(.filtered-out)`))
      for (let i = 0; i < filtered.length; i++)
      {
        if (i % 2 === 1)
          filtered[i].classList.add('stripe')
      }
    },
    getFilterLabel: function () {
      let filtered = document.querySelectorAll('.filtered-out').length
      let all = this.rows.length
      // console.log(filtered,all)
      let label = `${all}`
      if (filtered > 0)
        label = `${this.rows.length - filtered} of ${this.rows.length} (${filtered})`
      return label
    }
  },
  computed: {
    ...mapState(['filter', 'activeTab', 'tabs', 'apps', 'queries']),
    hide () { return this.filter.selector === '' }
  },
  mounted () {
    let vm = this
    // filter keyup handler
    let filter = document.querySelector(this.filterSel)
    filter.addEventListener('keyup', (e) => vm.debounce(250, vm.filtering(e)))
    filter.addEventListener('clear', (e) => {
      vm.filtering(e)
      filter.blur()
    })
  },
  watch: {
    rows (neo, old) {
      this.filterLabel = this.getFilterLabel()
    },
    apps (neo, old) {
      if (this.filter.selector !== '')
        this.rows = Array.from(document.querySelectorAll(this.filter.selector))
    },
    queries (neo, old) {
      if (this.filter.selector !== '')
        this.rows = Array.from(document.querySelectorAll(this.filter.selector))
    },
    activeTab (neo, old) {
      this.$store.commit(types.SET_FILTER, this.tabs[`${this.activeTab.replace(/-tab/, '')}`].filter)
      if (this.filter.selector === '')
        this.rows = []
      else
      {
        this.rows = Array.from(document.querySelectorAll(this.filter.selector))
        this.clearFilter()
      }
    }
  }

}
</script>
<style>
  .hide {
    display: none;
  }
</style>
