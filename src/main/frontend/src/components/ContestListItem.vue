<template>
  <tr>
    <td>
      <router-link :to="{ name: 'Contest', params: { contest: contest.id } }">
        {{ contest.contestName }}
      </router-link>
    </td>
    <td>
      {{ contest.beginDate }}
    </td>
    <td>
      {{ contest.endDate }}
    </td>
  </tr>
</template>

<script>
export default {
  name: 'ContestListItem',
  props: {
    contest: {
      type: Object,
      default: null
    }
  }
}
</script>
