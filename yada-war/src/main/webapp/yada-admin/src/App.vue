<template>
  <div class="nest container-fluid">
    <div class="page-header">
      <div class="background"><img src="./assets/blox250.png"/><span>Admin</span></div>
    </div>
    <div class="row">
      <div id="app-list" class="col-3">
        <AppList/>
      </div>
      <div id="tab-panel" class="col-9">
        <ul class="nav nav-tabs">
          <li class="nav-item">
            <a class="nav-link active" href="#" data-toggle="tab">Queries</a>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="#" data-toggle="tab">Configuration</a>
          </li>
      </ul>
      </div>
    </div>
  </div>
</template>

<script>
import AppList from './components/AppList.vue'
export default {
  name: 'App',
  components: { AppList }
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
