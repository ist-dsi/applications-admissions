<template>
  <div class="row">
    <div class="col-xs-12">
      <h3 v-if="!contests || contests.length == 0">
        {{ $t('msg.contest.none') }}
      </h3>
      <table
        v-else
        class="table tdmiddle"
      >
        <thead>
          <tr>
            <th class="header">
              <a @click="sortBy('contestName')">
                {{ $t('label.contest.main') }}
              </a>
            </th>
            <th class="header">
              <a @click="sortBy('beginDate')">
                {{ $t('label.contest.begin') }}
              </a>
            </th>
            <th class="header">
              <a @click="sortBy('endDate')">
                {{ $t('label.contest.end') }}
              </a>
            </th>
          </tr>
        </thead>
        <tbody>
          <contest-list-item
            v-for="contest in contests"
            :key="contest.id"
            :contest="contest"
          />
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
import ContestListItem from './ContestListItem.vue'
import { mapState } from 'vuex'

export default {
  name: 'ContestList',
  components: {
    ContestListItem
  },
  data: function () {
    return {
      sort: 'beginDate',
      order: -1
    }
  },
  computed: mapState(['contests']),
  methods: {
    sortBy: function (sort) {
      if (this.sort === sort) {
        this.order = -this.order
      } else {
        this.sort = sort
        this.order = 1
      }

      this.$store.dispatch('sortContestList', { sort: this.sort, order: this.order })
    }
  }
}
</script>
