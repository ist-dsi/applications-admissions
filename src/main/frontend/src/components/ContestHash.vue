<template>
  <div
    v-if="checkPermission('canViewContestHash')"
    class="row"
  >
    <div class="col-xs-6 col-sm-8 col-md-9">
      <label
        class="control-label"
        for="editLink"
      >
        {{ $t('label.contest.link') }}
      </label>
      <p v-if="contest.viewHash">
        <router-link
          v-slot="{ href }"
          :to="{ name:'Contest', params: { contest:contest.id }, query: { hash: contest.viewHash }}"
        >
          <a :href="addBaseToUrl(href)">
            {{ addBaseToUrl(href) }}
          </a>
        </router-link>
      </p>
      <p v-else>
        {{ $t('msg.candidate.hash.none') }}
      </p>
    </div>
    <div class="col-xs-6 col-sm-4 col-md-3 text-right">
      <button
        v-if="checkPermission('canDeleteContestHash')"
        class="btn btn-default"
        @click="deleteContestHash()"
      >
        {{ $t('action.contest.hash.delete') }}
      </button>
      <button
        v-if="checkPermission('canCreateContestHash')"
        class="btn btn-default"
        @click="createContestHash()"
      >
        {{ $t('action.contest.hash.create') }}
      </button>
    </div>
  </div>
</template>

<script>
import ApplicationsAdmissionsApi from '@/api/applicationsAdmissions'
import checkPermission from '@/mixins/checkPermission'

import { mapState } from 'vuex'
import * as types from '@/store/mutation-types'

export default {
  name: 'ContestHash',
  mixins: [checkPermission],
  computed: mapState({
    contest: 'currentContest'
  }),
  methods: {
    addBaseToUrl (url) {
      return new URL(url, window.location.origin).toString()
    },
    createContestHash () {
      ApplicationsAdmissionsApi.createContestHash(this.contest.id)
        .then(response => {
          this.$store.dispatch('showSuccessMessage', { messageKey: 'feedback.success' })
          this.$store.commit(types.RECEIVE_CONTEST, response)
        })
        .catch(error => {
          this.$router.push({ path: `/error/${error.response.status}` })
        })
    },
    deleteContestHash () {
      ApplicationsAdmissionsApi.deleteContestHash(this.contest.id)
        .then(response => {
          this.$store.dispatch('showSuccessMessage', { messageKey: 'feedback.success' })
          this.$store.commit(types.RECEIVE_CONTEST, response)
        })
        .catch(error => {
          this.$router.push({ path: `/error/${error.response.status}` })
        })
    }
  }
}
</script>
