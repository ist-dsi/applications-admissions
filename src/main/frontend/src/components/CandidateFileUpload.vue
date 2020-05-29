<template>
  <div
    v-if="checkPermission('canUploadCandidateFile')"
    class="row"
  >
    <div class="col-xs-12">
      <form
        ref="form"
        enctype="multipart/form-data"
        class="form-inline"
        @submit.prevent="send()"
      >
        <input
          v-model="hash"
          type="hidden"
          name="hash"
        >
        <label>
          {{ $t('action.candidate.file.upload') }}
        </label>
        <div class="form-group">
          <label
            class="sr-only"
            for="name"
          >
            {{ $t('label.candidate.file.name') }}
          </label>
          <input
            v-model="name"
            class="form-control"
            type="text"
            name="name"
            required="required"
            :placeholder="$t('label.candidate.file.name')"
          >
        </div>
        <div class="form-group">
          <label
            class="sr-only"
            for="name"
          >
            {{ $t('label.candidate.file.main') }}
          </label>
          <input
            ref="file"
            class="form-control"
            type="file"
            name="file"
            required="required"
            @change="fileUploaded()"
          >
        </div>
        <button
          type="submit"
          class="btn btn-default"
          :disabled="!canSubmit"
        >
          {{ $t('action.upload') }}
        </button>
      </form>
    </div>
  </div>
</template>

<script>
import ApplicationsAdmissionsApi from '@/api/applicationsAdmissions'

import checkPermission from '@/mixins/checkPermission'

import * as types from '@/store/mutation-types'

import { mapState } from 'vuex'

export default {
  name: 'CandidateFileUpload',
  mixins: [checkPermission],
  props: {
    hash: {
      type: String,
      default: undefined
    }
  },
  data: function () {
    return {
      name: '',
      file: undefined
    }
  },
  computed: {
    canSubmit () {
      return !!this.name && !!this.file
    },
    ...mapState({
      candidate: 'currentCandidate'
    })
  },
  methods: {
    fileUploaded () {
      this.file = this.$refs.file.files[0]
    },
    send () {
      if (!this.canSubmit) {
        return
      }

      const formData = new FormData()
      formData.append('hash', this.hash)
      formData.append('name', this.name)
      formData.append('file', this.file)

      ApplicationsAdmissionsApi.uploadCandidateFile(this.candidate.id, formData)
        .then(response => {
          this.$store.dispatch('showSuccessMessage', { messageKey: 'feedback.success' })
          this.$store.commit(types.RECEIVE_CANDIDATE, response)
          this.reset()
        })
        .catch(error => {
          this.$router.push({ path: `/error/${error.response.status}` })
        })
    },
    reset () {
      this.name = undefined
      this.file = undefined
      this.$refs.form.reset()
    }
  }
}
</script>
