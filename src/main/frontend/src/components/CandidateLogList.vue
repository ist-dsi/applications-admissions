<template>
  <div class="row">
    <div class="col-xs-12">
      <div
        v-if="!logs || logs.length == 0"
        class="alert alert-warning"
        role="alert"
      >
        {{ $t('msg.candidate.log.none') }}
      </div>

      <table
        v-else
        class="table"
      >
        <thead>
          <tr>
            <th class="col-md-2">
              {{ $t('label.candidate.log.timestamp') }}
            </th>
            <th class="col-md-2">
              {{ $t('label.candidate.log.type') }}
            </th>
            <th class="col-md-4">
              {{ $t('label.candidate.log.filename') }}
            </th>
            <th class="col-md-2">
              {{ $t('label.candidate.log.size') }}
            </th>
            <th class="col-md-2">
              {{ $t('label.candidate.log.fileId') }}
            </th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="log in logs"
            :key="log.id"
          >
            <td>
              {{ format(log.timestamp) }}
            </td>
            <td>
              {{ log.type }}
            </td>
            <td>
              {{ log.filename }}
            </td>
            <td>
              {{ log.size }}
            </td>
            <td>
              {{ log.fileId }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
import moment from 'moment'

export default {
  name: 'CandidateLogList',
  props: {
    logs: {
      type: Array,
      default: undefined
    }
  },
  methods: {
    format (timestamp) {
      return moment(timestamp).format('YYYY-MM-DD HH:mm')
    }
  }
}
</script>
