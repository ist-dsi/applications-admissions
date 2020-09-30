<template>
  <div
    v-if="contest"
    class="page-header"
  >
    <h1>
      {{ $t('label.contest.main') }}
      <span>
        {{ contest.contestName }}
      </span>
    </h1>
    <h4>
      <span>
        <span style="color: gray;">
          {{ contest.beginDate }}
        </span>
        -
        <span style="color: gray;">
          {{ contest.endDate }}
        </span>
      </span>
    </h4>
  </div>
</template>

<script>
import { mapState } from 'vuex'

export default {
  name: 'ContestHeader',
  computed: mapState({
    contest: 'currentContest'
  })
}
</script>
