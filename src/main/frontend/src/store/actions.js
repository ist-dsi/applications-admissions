import * as types from './mutation-types'
import ProfileAPI from '@/api/profile'
import ApplicationsAdmissionsApi from '@/api/applicationsAdmissions'

import Vue from 'vue'

export const setTopMessage = ({ commit }, { active, msg, dismiss, type }) => {
  commit(types.SET_TOP_MESSAGE, { active, msg, dismiss, type })
}
export const showTopMessage = ({ dispatch }, { messageKey, msg, params, type }) => {
  const message = msg ? { pt: msg, en: msg } : { pt: Vue.i18n.translateIn('pt', messageKey, params || {}), en: Vue.i18n.translateIn('en', messageKey, params || {}) }
  dispatch('setTopMessage', { active: true, msg: message, dismiss: true, type })
}
export const hideTopMessage = ({ dispatch }) => {
  dispatch('setTopMessage', { active: false, msg: { pt: '', en: '' }, dismiss: false, type: '' })
}
export const showErrorMessage = ({ dispatch }, { messageKey, msg, params }) => {
  dispatch('showTopMessage', { messageKey, msg, params, type: 'danger' })
}
export const showWarningMessage = ({ dispatch }, { messageKey, msg, params }) => {
  dispatch('showTopMessage', { messageKey, msg, params, type: 'warning' })
}
export const showInfoMessage = ({ dispatch }, { messageKey, msg, params }) => {
  dispatch('showTopMessage', { messageKey, msg, params, type: 'info' })
}
export const showSuccessMessage = ({ dispatch }, { messageKey, msg, params }) => {
  dispatch('showTopMessage', { messageKey, msg, params, type: 'success' })
}

export const fetchProfile = async ({ commit }) => {
  return ProfileAPI.get()
    .then(response => commit(types.RECEIVE_PROFILE, { profile: response }))
}

export const fetchPermissions = async ({ commit }, { contest, candidate, hash }) => {
  return ApplicationsAdmissionsApi.getPermissions(contest, candidate, hash)
    .then(response => commit(types.RECEIVE_PERMISSIONS, response))
}

export const fetchContestList = async ({ commit }) => {
  return ApplicationsAdmissionsApi.getContestList()
    .then(response => commit(types.RECEIVE_CONTEST_LIST, response))
}

export const sortContestList = ({ commit }, { sort, order }) => {
  commit(types.SORT_CONTEST_LIST, { sort, order })
}

export const fetchContest = async ({ commit }, { id, hash }) => {
  return ApplicationsAdmissionsApi.getContest(id, hash)
    .then(response => commit(types.RECEIVE_CONTEST, response))
}

export const fetchCandidate = async ({ commit }, { id, hash }) => {
  return ApplicationsAdmissionsApi.getCandidate(id, hash)
    .then(response => commit(types.RECEIVE_CANDIDATE, response))
}

export const fetchCandidateLogs = async ({ commit }, { id, hash }) => {
  return ApplicationsAdmissionsApi.getCandidateLogs(id, hash)
    .then(response => commit(types.RECEIVE_CANDIDATE, response))
}
