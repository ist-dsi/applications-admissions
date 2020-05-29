<template>
  <div>
    <div
      class="alert alert-dismissible alert-warning"
      role="alert"
    >
      MANAGE
    </div>
    <ContestList />
  </div>
</template>

<script>
import ContestList from '@/components/ContestList.vue'

export default {
  name: 'PageManageContestList',
  components: {
    ContestList
  }
}
</script>
