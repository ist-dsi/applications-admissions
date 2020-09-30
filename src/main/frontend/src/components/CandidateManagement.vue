<template>
  <div
    v-if="checkPermission('canDownloadAllCandidateFiles') || checkPermission('canViewCandidateLogs') || checkPermission('canDeleteCandidate')"
    class="row"
  >
    <div
      v-if="!showDeleteWarning"
      class="col-xs-12"
    >
      <p>
        <button
          v-if="checkPermission('canDownloadAllCandidateFiles')"
          class="btn btn-default"
          @click="downloadAllFiles()"
        >
          {{ $t('action.candidate.download') }}
        </button>

        <router-link
          v-if="checkPermission('canViewCandidateLogs')"
          :to="{ name: 'CandidateLogs', params: { candidate: candidate.id }, query: { hash: hash }}"
          class="btn btn-default"
        >
          {{ $t('action.candidate.log.view') }}
        </router-link>

        <button
          v-if="checkPermission('canDeleteCandidate')"
          class="btn btn-danger"
          @click="showDeleteWarning = true"
        >
          {{ $t('action.candidate.delete') }}
        </button>
      </p>
    </div>

    <div
      v-if="showDeleteWarning"
      class="col-xs-12"
    >
      <div class="panel panel-danger">
        <div class="panel-heading">
          <div class="row">
            <div class="col-xs-6">
              <h1 class="panel-title">
                {{ $t('action.candidate.delete') }}
              </h1>
            </div>
            <div class="col-xs-6">
              <button
                type="button"
                class="close"
                aria-label="Dismiss"
                @click.prevent="showDeleteWarning = false"
              >
                <span aria-hidden="true">
                  &times;
                </span>
              </button>
            </div>
          </div>
        </div>
        <div class="panel-body">
          <h1>
            {{ $t('msg.warning') }}
          </h1>
          <h3>
            {{ $t('msg.candidate.delete.warning') }}
            <br>
            <small>
              {{ $t('msg.candidate.delete.confirm') }}
            </small>
          </h3>

          <div class="form-group">
            <input
              v-model="deleteConfirmationText"
              type="text"
              class="form-control"
            >
          </div>
          <button
            class="btn btn-danger"
            :disabled="!deleteConfirmation"
            @click="deleteCandidate()"
          >
            {{ $t('action.candidate.delete') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import ApplicationsAdmissionsApi from '@/api/applicationsAdmissions'

import checkPermission from '@/mixins/checkPermission'

import { mapState } from 'vuex'

export default {
  name: 'CandidateManagement',
  mixins: [checkPermission],
  props: {
    hash: {
      type: String,
      default: undefined
    }
  },
  data: function () {
    return {
      showDeleteWarning: false,
      deleteConfirmationText: ''
    }
  },
  computed: {
    deleteConfirmation () {
      return this.deleteConfirmationText === this.candidate.name
    },
    ...mapState({
      candidate: 'currentCandidate'
    })
  },
  methods: {
    downloadAllFiles () {
      ApplicationsAdmissionsApi.downloadAllCandidateFiles(this.candidate.id, this.hash)
        .then(response => {
          const fileName = response.headers['content-disposition'].substring(9)
          const fileURL = URL.createObjectURL(response.data)

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
    },
    deleteCandidate () {
      ApplicationsAdmissionsApi.deleteCandidate(this.candidate.id)
        .then(response => {
          this.$store.dispatch('showSuccessMessage', { messageKey: 'feedback.success' })
          this.$router.push({ name: 'Contest', params: { contest: this.candidate.contest.id }, query: { hash: this.hash } })
        })
        .catch(error => {
          this.$router.push({ path: `/error/${error.response.status}` })
        })
    }
  }
}
</script>
