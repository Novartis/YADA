import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/yada-admin',
      name: 'App'
      Component: 'App'
    }
  ]
})
