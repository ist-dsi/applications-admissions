<template>
  <div
    v-if="checkPermission('canViewCandidateHash')"
    class="row"
  >
    <div class="col-xs-6 col-sm-8 col-md-9">
      <label
        class="control-label"
        for="editLink"
      >
        {{ $t('label.candidate.link') }}
      </label>
      <p v-if="candidate.editHash">
        <router-link
          v-slot="{ href }"
          :to="{ name:'Candidate', params: { candidate:candidate.id }, query: { hash: candidate.editHash }}"
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
        v-if="checkPermission('canDeleteCandidateHash')"
        class="btn btn-default"
        @click="deleteCandidateHash()"
      >
        {{ $t('action.candidate.hash.delete') }}
      </button>
      <button
        v-if="checkPermission('canCreateCandidateHash')"
        class="btn btn-default"
        @click="createCandidateHash()"
      >
        {{ $t('action.candidate.hash.create') }}
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
  name: 'CandidateHash',
  mixins: [checkPermission],
  computed: mapState({
    candidate: 'currentCandidate'
  }),
  methods: {
    addBaseToUrl (url) {
      return new URL(url, window.location.origin).toString()
    },
    createCandidateHash () {
      ApplicationsAdmissionsApi.createCandidateHash(this.candidate.id)
        .then(response => {
          this.$store.dispatch('showSuccessMessage', { messageKey: 'feedback.success' })
          this.$store.commit(types.RECEIVE_CANDIDATE, response)
        })
        .catch(error => {
          this.$router.push({ path: `/error/${error.response.status}` })
        })
    },
    deleteCandidateHash () {
      ApplicationsAdmissionsApi.deleteCandidateHash(this.candidate.id)
        .then(response => {
          this.$store.dispatch('showSuccessMessage', { messageKey: 'feedback.success' })
          this.$store.commit(types.RECEIVE_CANDIDATE, response)
        })
        .catch(error => {
          this.$router.push({ path: `/error/${error.response.status}` })
        })
    }
  }
}
</script>
