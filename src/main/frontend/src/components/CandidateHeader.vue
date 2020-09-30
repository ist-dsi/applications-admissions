<template>
  <div
    v-if="candidate"
    class="page-header"
  >
    <h1>
      <span>
        [{{ $t('label.candidate.number') }} {{ candidate.candidateNumber }}]
      </span>
      <span>
        {{ candidate.name }}
      </span>
    </h1>
    <h4>
      {{ $t('label.candidate.email') }}:
      <span>
        {{ candidate.email }}
      </span>
    </h4>
    <h4>
      {{ $t('label.contest.main') }}
      <router-link
        :to="{ name: 'Contest', params: { contest: candidate.contest.id }, query: { hash: hash } }"
        class="secondaryLink"
      >
        <span style="font-weight: bold;">
          {{ candidate.contest.contestName }}
        </span>
      </router-link>
      <span style="color: gray;">
        {{ candidate.contest.beginDate }} - {{ candidate.contest.endDate }}
      </span>
    </h4>
  </div>
</template>

<script>
import { mapState } from 'vuex'

export default {
  name: 'CandidateHeader',
  props: {
    hash: {
      type: String,
      default: undefined
    }
  },
  computed: mapState({
    candidate: 'currentCandidate'
  })
}
</script>
