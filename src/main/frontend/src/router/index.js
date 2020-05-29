import Vue from 'vue'
import Router from 'vue-router'

import PageError from '@/pages/PageError'
import PageContestList from '@/pages/PageContestList'
import PageCreateContest from '@/pages/PageCreateContest'
import PageContest from '@/pages/PageContest'
import PageCreateCandidate from '@/pages/PageCreateCandidate'
import PageCandidateLogs from '@/pages/PageCandidateLogs'

import PageCandidate from '@/pages/PageCandidate'

import handlers from './handlers'

Vue.use(Router)

const router = new Router({
  base: `${process.env.CTX && !process.env.DEV ? '/' + process.env.CTX : ''}/applications-admissions`,
  routes: [
    {
      path: '/',
      name: 'ContestList',
      component: PageContestList,
      beforeEnter: handlers.fetchContestListHandler
    },
    {
      path: '/contest/create',
      name: 'CreateContest',
      component: PageCreateContest,
      meta: { permission: 'canCreateContest' }
    },
    {
      path: '/contest/:contest(\\d+)',
      name: 'Contest',
      component: PageContest,
      props: (route) => ({ hash: route.query.hash }),
      beforeEnter: handlers.fetchContestHandler,
      meta: { permission: 'canViewContest' }
    },
    {
      path: '/contest/:contest(\\d+)/apply',
      name: 'CreateCandidate',
      component: PageCreateCandidate,
      props: (route) => ({ hash: route.query.hash }),
      beforeEnter: handlers.fetchContestHandler,
      meta: { permission: 'canCreateCandidate' }
    },
    {
      path: '/candidate/:candidate(\\d+)',
      name: 'Candidate',
      component: PageCandidate,
      props: (route) => ({ hash: route.query.hash }),
      beforeEnter: handlers.fetchCandidateHandler,
      meta: { permission: 'canViewCandidate' }
    },
    {
      path: '/candidate/:candidate(\\d+)/logs',
      name: 'CandidateLogs',
      component: PageCandidateLogs,
      props: (route) => ({ hash: route.query.hash }),
      beforeEnter: handlers.fetchCandidateLogsHandler,
      meta: { permission: 'canViewCandidateLogs' }
    },
    {
      path: '/error/400',
      name: 'BadRequest',
      component: PageError,
      props: { errorCode: '400' }
    },
    {
      path: '/error/403',
      name: 'Forbidden',
      component: PageError,
      props: { errorCode: '403' }
    },
    {
      path: '/error/404',
      name: 'NotFound',
      component: PageError,
      props: { errorCode: '404' }
    },
    {
      path: '/error/500',
      name: 'InternalServerError',
      component: PageError,
      props: { errorCode: '500' }
    },
    {
      path: '/error/504',
      name: 'GatewayTimeOut',
      component: PageError,
      props: { errorCode: '504' }
    },
    {
      path: '*',
      redirect: '/error/404'
    }
  ]
})

router.beforeEach(handlers.permissionsHandler)
router.beforeEach(handlers.profileHandler)

export default router
