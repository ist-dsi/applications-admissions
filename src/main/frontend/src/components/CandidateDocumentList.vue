<template>
  <div class="row">
    <div class="col-xs-12">
      <div
        v-if="!items || items.length == 0"
        class="alert alert-warning"
        role="alert"
      >
        {{ $t('msg.candidate.file.none') }}
      </div>

      <table
        v-else
        class="table"
      >
        <thead>
          <tr>
            <th class="col-md-4">
              {{ $t('label.candidate.file.name') }}
            </th>
            <th class="col-md-2">
              {{ $t('label.candidate.file.size') }}
            </th>
            <th class="col-md-2">
              {{ $t('label.candidate.file.created') }}
            </th>
            <th class="col-md-2">
              {{ $t('label.candidate.file.modified') }}
            </th>
            <th class="col-md-2" />
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="item in items"
            :key="item.id"
          >
            <td>
              {{ item.name }}
            </td>
            <td>
              {{ item.size }}
            </td>
            <td>
              {{ format(item.created) }}
            </td>
            <td>
              {{ format(item.created) }}
            </td>
            <td>
              <button
                v-if="checkPermission('canDownloadSingleCandidateFile')"
                class="btn btn-default"
                @click="downloadFile(item)"
              >
                {{ $t('action.download') }}
              </button>
              <button
                v-if="showDeleteButton"
                class="btn btn-default"
                @click="deleteFile(item)"
              >
                {{ $t('action.delete') }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
import ApplicationsAdmissionsApi from '@/api/applicationsAdmissions'

import checkPermission from '@/mixins/checkPermission'

import * as types from '@/store/mutation-types'

import { mapState } from 'vuex'
import moment from 'moment'

export default {
  name: 'CandidateDocumentsList',
  mixins: [checkPermission],
  props: {
    items: {
      type: Array,
      default: undefined
    },
    hash: {
      type: String,
      default: undefined
    },
    showDeleteButton: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    ...mapState({
      candidate: 'currentCandidate',
      sealIsBroken (state) {
        return !(state.currentCandidate.seal === state.currentCandidate.calculatedDigest)
      }
    })
  },
  methods: {
    format (timestamp) {
      return moment(timestamp).format('YYYY-MM-DD')
    },
    downloadFile (file) {
      ApplicationsAdmissionsApi.downloadSingleCandidateFile(this.candidate.id, file.id, this.hash)
        .then(response => {
          const fileURL = URL.createObjectURL(response.data)

          const fileLink = document.createElement('a')
          fileLink.href = fileURL
          fileLink.setAttribute('download', file.name)
          document.body.appendChild(fileLink)
          fileLink.click()

          URL.revokeObjectURL(fileURL)
        })
        .catch(error => {
          this.$router.push({ path: `/error/${error.response.status}` })
        })
    },
    deleteFile (file) {
      ApplicationsAdmissionsApi.deleteCandidateFile(this.candidate.id, file.id, this.hash)
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
