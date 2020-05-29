<template>
  <div
    v-if="candidate"
    class="row"
  >
    <div class="col-xs-12">
      <div
        v-if="checkPermission('canCreateCandidateSeal')"
        class="alert alert-warning"
        role="alert"
      >
        <p>
          {{ $t('msg.candidate.seal.warning') }}
        </p>
        <button
          v-if="!confirmSeal"
          class="btn btn-default"
          @click="confirmSeal = true"
        >
          {{ $t('action.candidate.seal.create') }}
        </button>
      </div>

      <div
        v-if="confirmSeal"
        class="alert alert-danger"
        role="alert"
      >
        <h1>
          {{ $t('msg.candidate.seal.confirm') }}
        </h1>
        <button
          class="btn btn-default"
          @click="confirmSeal = false"
        >
          {{ $t('action.cancel') }}
        </button>
        <button
          class="btn btn-danger"
          @click="seal()"
        >
          {{ $t('action.candidate.seal.create') }}
        </button>
      </div>

      <div
        v-if="candidate.seal"
        class="alert"
        :class="sealIsBroken ? 'alert-warning' : 'alert-info'"
        role="alert"
      >
        {{ $t('msg.candidate.seal.sealed') }}
        <ul>
          <li>
            <span>
              {{ candidate.sealDate }}
            </span>
          </li>
          <li>
            <span>
              {{ candidate.seal }}
            </span>
          </li>
        </ul>
        <p v-if="sealIsBroken">
          {{ $t('msg.candidate.seal.broken') }}
        </p>
      </div>
    </div>
  </div>
</template>

<script>
import ApplicationsAdmissionsApi from '@/api/applicationsAdmissions'

import checkPermission from '@/mixins/checkPermission'

import * as types from '@/store/mutation-types'

import { mapState } from 'vuex'
import moment from 'moment'

export default {
  name: 'CandidateSeal',
  mixins: [checkPermission],
  props: {
    hash: {
      type: String,
      default: undefined
    }
  },
  data: function () {
    return {
      confirmSeal: false
    }
  },
  computed: {
    ...mapState({
      candidate: 'currentCandidate',
      sealIsBroken (state) {
        return !(state.currentCandidate.seal === state.currentCandidate.calculatedDigest)
      }
    })
  },
  methods: {
    format (timestamp) {
      return moment(timestamp).format('YYYY-MM-DD')
    },
    seal () {
      ApplicationsAdmissionsApi.createCandidateSeal(this.candidate.id, this.hash)
        .then(response => {
          this.$store.dispatch('showSuccessMessage', { messageKey: 'feedback.success' })
          this.$store.commit(types.RECEIVE_CANDIDATE, response)
          this.confirmSeal = false
        })
        .catch(error => {
          this.$router.push({ path: `/error/${error.response.status}` })
        })
    }
  }
}
</script>
