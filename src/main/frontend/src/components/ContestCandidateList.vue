<template>
  <div>
    <div
      v-if="contest && contest.candidates"
      class="row"
    >
      <div class="col-xs-12 col-sm-10 col-md-8 col-lg-6">
        <h3 v-if="contest.candidates.length == 0">
          No available results.
        </h3>
        <table
          v-else
          class="table tdmiddle"
        >
          <thead>
            <tr>
              <th class="col-xs-2">
                {{ $t('label.candidate.number') }}
              </th>
              <th class="col-xs-8">
                {{ $t('label.candidate.main') }}
              </th>
              <th class="col-xs-2" />
            </tr>
          </thead>
          <tbody>
            <ContestCandidateListItem
              v-for="candidate in contest.candidates"
              :key="candidate.id"
              :candidate="candidate"
              :hash="hash"
            />
          </tbody>
        </table>
      </div>
    </div>
    <div class="row">
      <div class="col-xs-12">
        <router-link
          v-if="checkPermission('canCreateCandidate')"
          :to="{ name: 'CreateCandidate', params: { contest: contest.id }, query: { hash: hash } }"
          class="btn btn-default"
        >
          {{ $t('action.candidate.create') }}
        </router-link>
        <button
          v-if="checkPermission('canExportContestCandidates')"
          class="btn btn-default"
          @click="exportContestCandidates()"
        >
          {{ $t('action.export') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import ApplicationsAdmissionsApi from '@/api/applicationsAdmissions'

import checkPermission from '@/mixins/checkPermission'

import ContestCandidateListItem from './ContestCandidateListItem'

import { mapState } from 'vuex'

export default {
  name: 'ContestCandidateList',
  components: {
    ContestCandidateListItem
  },
  mixins: [checkPermission],
  props: {
    hash: {
      type: String,
      default: undefined
    }
  },
  computed: mapState({
    contest: 'currentContest'
  }),
  methods: {
    exportContestCandidates () {
      ApplicationsAdmissionsApi.exportContestCandidates(this.contest.id)
        .then(response => {
          const fileName = response.headers['content-disposition'].substring(9)
          var fileURL = URL.createObjectURL(response.data)

          const fileLink = document.createElement('a')
          fileLink.href = fileURL
          fileLink.setAttribute('download', fileName)
          document.body.appendChild(fileLink)
          fileLink.click()

          URL.revokeObjectURL(fileURL)
        })
        .catch(error => {
          this.$router.push({ path: `/error/${error.response.status}` })
        })
    }
  }
}
</script>
