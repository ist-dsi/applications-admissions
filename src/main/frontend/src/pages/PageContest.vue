<template>
  <div v-if="contest">
    <ContestHeader />

    <ContestManagement />
    <ContestHash />

    <ContestCandidateList :hash="hash" />
  </div>
</template>

<script>
import ContestHeader from '@/components/ContestHeader'
import ContestManagement from '@/components/ContestManagement'
import ContestHash from '@/components/ContestHash'
import ContestCandidateList from '@/components/ContestCandidateList'

import { mapState } from 'vuex'

export default {
  name: 'PageCandidate',
  components: {
    ContestHeader,
    ContestManagement,
    ContestHash,
    ContestCandidateList
  },
  props: {
    hash: {
      type: String,
      default: undefined
    }
  },
  computed: mapState({
    contest: 'currentContest'
  })
}
</script>
