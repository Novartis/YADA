<template>
  <div class="dev">
    <span :class="`status-${unsavedChanges > 0}`">Unsaved: {{unsavedChanges}}</span>
    <span :class="`status-${loading}`"><span v-if="!!!loading">not </span>Loading</span>
    <span :class="`status-${saving}`"><span v-if="!!!saving">not </span>Saving</span>
    <span :class="`status-${creating}`"><span v-if="!!!creating">not </span>Creating</span>
    <span :class="`status-${renaming}`"><span v-if="!!!renaming">not </span>Renaming</span>
    <span :class="`status-${cloning}`"><span v-if="!!!cloning">not </span>Cloning</span>
    <span :class="`status=${typeof config !== 'undefined' && config !== null && config.length > 0}`"
      v-if="typeof config !== 'undefined' && config !== null && config.length > 0">has Config</span>
  </div>
</template>
<script>
import * as types from '@/store/vuex-types'
import { mapState } from 'vuex'

export default {

  computed: {
    ...mapState(['unsavedChanges','config','saving','confirm','creating','loading','renaming','cloning','unsavedChanges','unsavedParams','activeTab','nextTab','confirmAction'])
  }
}
</script>
<style>
.dev {
  margin-top: 3px;
  float: right;
  padding-right: 10px;
  /* overflow-y: scroll; */
}

.status-true {
  color: red;
  font-weight: bold;
  border: 1px solid red;
  padding: 2px;
}

.status-false {
  color:  blue;
  border: 1px solid blue;
  padding: 2px;
}

</style>
