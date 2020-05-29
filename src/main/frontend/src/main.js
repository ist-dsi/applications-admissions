import Vue from 'vue'
import App from './App'
import store from './store'
import router from './router'
import client from '@/api/client'
import 'bootstrap'

Vue.use({
  router,
  store,
  axios: client,
  errorHandler: () => {
    store.dispatch('showErrorMessage', { messageKey: 'message.error.internalError' })
  }
})

Vue.mixin({
  methods: {
    async setLocale (locale) {
      this.$i18n.set(locale)
      // await ProfileAPI.changeLocale(locale)
      // Refresh data previously provided on the latest locale
      router.go()
    }
  }
})

window.addEventListener('offline', () => {
  store.dispatch('showWarningMessage', { messageKey: 'feedback.noNetwork' })
}, false)

window.addEventListener('online', () => {
  if (store.state.topMessage.active && store.state.topMessage.msg[Vue.i18n.locale()] === Vue.i18n.translate('feedback.noNetwork')) {
    store.dispatch('hideTopMessage')
  }
}, false)

Vue.config.productionTip = false

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  store,
  render (createElement) {
    return createElement(App, {})
  }
})
