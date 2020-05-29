<template>
  <div>
    <CandidateHeader :hash="hash" />

    <CandidateManagement :hash="hash" />
    <CandidateHash />

    <h4>
      {{ $t('label.candidate.file.variant.documents') }}
    </h4>
    <CandidateDocumentList
      :items="candidate.items"
      :hash="hash"
      :show-delete-button="checkPermission('canDeleteCandidateFile')"
    />
    <CandidateFileUpload :hash="hash" />
    <p />
    <CandidateSeal :hash="hash" />

    <h4 v-if="candidate.letterItems">
      {{ $t('label.candidate.file.variant.recommendations') }}
    </h4>
    <CandidateDocumentList
      v-if="candidate.letterItems"
      :items="candidate.letterItems"
      :hash="hash"
      :show-delete-button="false"
    />
  </div>
</template>

<script>
import CandidateHeader from '@/components/CandidateHeader.vue'
import CandidateManagement from '@/components/CandidateManagement.vue'
import CandidateHash from '@/components/CandidateHash.vue'
import CandidateDocumentList from '@/components/CandidateDocumentList.vue'
import CandidateFileUpload from '@/components/CandidateFileUpload.vue'
import CandidateSeal from '@/components/CandidateSeal.vue'

import checkPermission from '@/mixins/checkPermission'

import { mapState } from 'vuex'

export default {
  name: 'PageContest',
  components: {
    CandidateHeader,
    CandidateManagement,
    CandidateHash,
    CandidateDocumentList,
    CandidateFileUpload,
    CandidateSeal
  },
  mixins: [checkPermission],
  props: {
    hash: {
      type: String,
      default: undefined
    }
  },
  computed: {
    ...mapState({
      candidate: 'currentCandidate'
    })
  }
}
</script>
