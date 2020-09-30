<template>
  <tr>
    <td>
      {{ candidate.candidateNumber }}
    </td>
    <td>
      <router-link :to="{ name: 'Candidate', params: { candidate: candidate.id }, query: { hash: hash } }">
        {{ candidate.name }}
      </router-link>
    </td>
    <td>
      <button
        v-if="checkPermission('canDownloadAllCandidateFiles')"
        class="btn btn-default"
        @click="downloadAllFiles()"
      >
        {{ $t('action.download') }}
      </button>
    </td>
  </tr>
</template>

<script>
import ApplicationsAdmissionsApi from '@/api/applicationsAdmissions'
import checkPermission from '@/mixins/checkPermission'

export default {
  name: 'ContestListItem',
  mixins: [checkPermission],
  props: {
    candidate: {
      type: Object,
      default: undefined
    },
    hash: {
      type: String,
      default: undefined
    }
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
    }
  }
}
</script>
