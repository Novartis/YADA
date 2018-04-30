<template>
  <div class="nest container-fluid">
    <div class="page-header">
      <div class="background"><img src="./assets/blox250.png"/><span>Admin</span></div>
    </div>
    <div class="row">
      <div id="tab-panel" class="col-12">
        <ul class="nav nav-tabs">
          <li class="nav-item">
            <a class="nav-link active" id="apps-tab" href="#apps-panel" data-toggle="tab">Apps</a>
          </li>
          <li class="nav-item">
            <a class="nav-link" id="query-tab" href="#query-panel" data-toggle="tab">Queries</a>
          </li>
          <li class="nav-item">
            <a class="nav-link" id="conf-tab"   href="#conf-panel" data-toggle="tab">Configuration</a>
          </li>
        </ul>
        <div class="tab-content" id="myTabContent">
          <div class="tab-pane fade show active" id="apps-panel" role="tabpanel" aria-labelledby="">
            <AppList/>
          </div>
          <div class="tab-pane fade" id="query-panel" role="tabpanel" aria-labelledby="">
            <QueryList/>
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
export default {
  name: 'App',
  components: { AppList,AppConfig,QueryList }
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

#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
</style>
