<template>
  <div class="row">
    <div
      v-if="checkPermission('canCreateContest')"
      class="col-xs-12"
    >
      <router-link
        :to="{ name: 'CreateContest' }"
        class="btn btn-default"
      >
        {{ $t('action.contest.create') }}
      </router-link>
    </div>
    <div class="col-xs-12">
      <ContestList />
    </div>
  </div>
</template>

<script>
import ContestList from '@/components/ContestList'
import checkPermission from '@/mixins/checkPermission'

export default {
  name: 'PageContestList',
  components: {
    ContestList
  },
  mixins: [checkPermission]
}
</script>
