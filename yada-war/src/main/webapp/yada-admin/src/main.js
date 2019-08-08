// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App.vue'
import router from './router'
import 'bootstrap'
import 'bootstrap/dist/css/bootstrap.min.css'
// eslint-disable-next-line
import 'expose-loader?$!expose-loader?jQuery!jquery'
// eslint-disable-next-line
import 'expose-loader?DataTables!datatables'
import 'datatables/media/css/jquery.dataTables.min.css'
import Clipboard from 'clipboard'
import Octicon from 'vue-octicon/components/Octicon.vue'
import Vuex from 'vuex'
import store from './store'
import DateUtils from './plugins/DateUtils'
import YADAUtils from './plugins/YADAUtils'
import 'vue-octicon/icons'

Vue.use(Vuex)
Vue.use(DateUtils)
Vue.use(YADAUtils,{debug:true})
Vue.use(new Clipboard('button.copy.btn'))

Vue.config.productionTip = false
Vue.component('octicon', Octicon)

/* eslint-disable no-new */
new Vue({
  el: '#app',
  store,
  router,
  components: { App },
  template: '<App/>'
})
