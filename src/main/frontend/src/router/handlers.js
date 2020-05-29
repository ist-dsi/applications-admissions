import store from '@/store'

const profileHandler = async (to, from, next) => {
  try {
    await store.dispatch('fetchProfile')
    next()
  } catch (err) {
    next('/error/' + err.response.status)
  }
}

const permissionsHandler = async (to, from, next) => {
  try {
    const contest = to.params ? to.params.contest : undefined
    const candidate = to.params ? to.params.candidate : undefined
    const hash = to.query ? to.query.hash : undefined

    await store.dispatch('fetchPermissions', { contest, candidate, hash })

    if (to.matched.some(record => record.meta.permission && !store.getters.checkPermission(record.meta.permission))) {
      next('/error/403')
    } else {
      next()
    }
  } catch (err) {
    next('/error/' + err.response.status)
  }
}

const fetchContestListHandler = async (to, from, next) => {
  try {
    await store.dispatch('fetchContestList')
    next()
  } catch (err) {
    next('/error/' + err.response.status)
  }
}

const fetchContestHandler = async (to, from, next) => {
  const id = to.params.contest
  const hash = to.query.hash
  if (id) {
    try {
      await store.dispatch('fetchContest', { id: id, hash: hash })
      next()
    } catch (err) {
      next('/error/' + err.response.status)
    }
  } else {
    next('/error/400')
  }
}

const fetchCandidateHandler = async (to, from, next) => {
  const id = to.params.candidate
  const hash = to.query.hash
  if (id) {
    try {
      await store.dispatch('fetchCandidate', { id: id, hash: hash })
      next()
    } catch (err) {
      next('/error/' + err.response.status)
    }
  } else {
    next('/error/400')
  }
}

const fetchCandidateLogsHandler = async (to, from, next) => {
  const id = to.params.candidate
  const hash = to.query.hash
  if (id) {
    try {
      await store.dispatch('fetchCandidateLogs', { id: id, hash: hash })
      next()
    } catch (err) {
      next('/error/' + err.response.status)
    }
  } else {
    next('/error/400')
  }
}

export default {
  profileHandler,
  permissionsHandler,
  fetchContestListHandler,
  fetchContestHandler,
  fetchCandidateHandler,
  fetchCandidateLogsHandler
}
