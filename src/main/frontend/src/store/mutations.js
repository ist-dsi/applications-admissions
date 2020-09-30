import * as types from './mutation-types'
import Vue from 'vue'

export default {
  [types.SET_TOP_MESSAGE] (state, { active, msg, dismiss, type }) {
    state.topMessage = { active, msg, dismiss, type }
  },

  [types.RECEIVE_PROFILE] (state, { profile }) {
    Vue.set(state, 'profile', profile)
    if (profile && profile.preferredLocale) {
      Vue.i18n.set(profile.preferredLocale.lang)
    } else {
      Vue.i18n.set('en')
    }
  },

  [types.RECEIVE_PERMISSIONS] (state, permissions) {
    Vue.set(state, 'permissions', permissions)
  },

  [types.RECEIVE_CONTEST_LIST] (state, contests) {
    Vue.set(state, 'contests', contests)
  },
  [types.SORT_CONTEST_LIST] (state, { sort, order }) {
    if (!state.contests) { return }
    state.contests.sort((c1, c2) => c1[sort].localeCompare(c2[sort]) * order)
  },

  [types.RECEIVE_CONTEST] (state, contest) {
    Vue.set(state, 'currentContest', contest)
  },

  [types.RECEIVE_CANDIDATE] (state, candidate) {
    Vue.set(state, 'currentCandidate', candidate)
  }
}
