<template>
  <div>
    <CandidateHeader :hash="hash" />

    <h4>
      {{ $t('label.candidate.log.variant.documents') }}
    </h4>
    <CandidateLogList :logs="candidate.logs.logs" />

    <h4>
      {{ $t('label.candidate.log.variant.recommendations') }}
    </h4>
    <CandidateLogList :logs="candidate.recommendationLogs.logs" />
  </div>
</template>

<script>
import CandidateHeader from '@/components/CandidateHeader.vue'
import CandidateLogList from '@/components/CandidateLogList.vue'

import { mapState } from 'vuex'

export default {
  name: 'PageContest',
  components: {
    CandidateHeader,
    CandidateLogList
  },
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
  },
  methods: {
  }
}
</script>
