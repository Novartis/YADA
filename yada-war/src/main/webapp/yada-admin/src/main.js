// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App.vue'
import router from './router'
import '../node_modules/semantic-ui/dist/semantic.css'
import semantic from 'semantic'
// eslint-disable-next-line
import 'expose-loader?$!expose-loader?jQuery!jquery'
import '@primer/octicons/build/build.css'
import Clipboard from 'clipboard'
import Vuex from 'vuex'
import store from './store'
import DateUtils from './plugins/DateUtils'
import YADAUtils from './plugins/YADAUtils'
import {utils} from './mixins/utils'

Vue.use(Vuex)
Vue.use(DateUtils)
Vue.use(YADAUtils, { debug: true })
Vue.use(new Clipboard('button.copy.btn'))
Vue.use(semantic)
Vue.mixin(utils)

Vue.config.productionTip = false

// eslint-disable no-new
const vue = new Vue({
  el: '#app',
  store,
  router,
  components: { App },
  template: '<App/>'
})

if (window.Cypress && process.env.NODE_ENV_LABEL !== 'PROD')
{
  window.vue = vue
}
