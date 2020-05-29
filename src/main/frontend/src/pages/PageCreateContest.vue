<template>
  <div>
    <div class="page-header">
      <h1>
        {{ $t('action.contest.create') }}
      </h1>
    </div>

    <div class="page-body">
      <ContestForm />
    </div>
  </div>
</template>

<script>
import ContestForm from '@/components/ContestForm'

export default {
  name: 'PageCreateContest',
  components: {
    ContestForm
  }
}
</script>
