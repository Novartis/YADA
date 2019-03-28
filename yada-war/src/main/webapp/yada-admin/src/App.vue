<template>
  <div class="nest container-fluid">
    <div class="page-header">
      <div class="background"><img src="static/blox250.png"/><span>Admin</span></div>
    </div>
    <div class="row">
      <div id="tab-panel" ref="tabpanel" class="col-12">
        <ul class="nav nav-tabs">
          <li class="nav-item">
            <a class="nav-link active" id="apps-tab" href="#apps-panel" data-toggle="tab">Apps</a>
          </li>
          <li class="nav-item">
            <a class="nav-link" id="query-tab" ref="querytab" href="#query-panel" data-toggle="tab">Queries</a>
          </li>
          <li class="nav-item">
            <a class="nav-link" id="conf-tab" href="#conf-panel" data-toggle="tab">Configuration</a>
          </li>
        </ul>
        <div class="tab-content" id="myTabContent">
          <div class="tab-pane fade show active" id="apps-panel" role="tabpanel" aria-labelledby="">
            <AppList/>
          </div>
          <div class="tab-pane fade" id="query-panel" role="tabpanel" aria-labelledby="">
            <QueryList />
          </div>
          <div class="tab-pane fade" id="conf-panel" role="tabpanel" aria-labelledby="">
            <AppConfig/>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import AppList from './components/AppList.vue'
import AppConfig from './components/AppConfig.vue'
import QueryList from './components/QueryList.vue'

import { mapGetters } from 'vuex'
export default {
  name: 'App',
  components: { AppList,AppConfig,QueryList },
  computed: mapGetters(['getApp','getConfig','getQueries','getQname','getTableRows']),
  watch: {
    getApp (val, oldVal) { this.$refs.querytab.click() }
  }
}
</script>

<style>
.page-header .background {
  background-color: rgb(241,80,9,0.2);
}

.page-header .background img {
  margin: 3px 8px;
  width: 50px;
}

.copy.btn {
  display: inline-block;
  font-size: 14px;
  vertical-align: top;
  user-select: none;
  padding: 0;
  position: relative;
  top: -3px;
  float: right;
  border: 1px solid rgb(0,0,0,0.2);
  background-image: linear-gradient(#fcfcfc,#eee);
  opacity: 0;
}

.copy.btn:before {
  content:"";
  background-repeat: no-repeat;
  background-image: url('../static/clippy.svg');
  background-position: 50%;
  background-size: 14px;
  display: block;
  height: 26px;
  width: 26px;
  vertical-align: top;
}

*:hover>.copy.btn {
  opacity: 1;
}

#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
</style>
