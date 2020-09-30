<template>
  <div
    v-if="checkPermission('canEditContest') || checkPermission('canDeleteContest')"
    class="row"
  >
    <div
      v-if="!showEditForm && !showDeleteWarning"
      class="col-xs-12"
    >
      <p>
        <button
          v-if="checkPermission('canEditContest')"
          class="btn btn-default"
          @click="showEditForm = true"
        >
          {{ $t('action.contest.edit') }}
        </button>
        <button
          v-if="checkPermission('canDeleteContest')"
          class="btn btn-danger"
          @click="showDeleteWarning = true"
        >
          {{ $t('action.contest.delete') }}
        </button>
      </p>
    </div>
    <div
      v-if="showEditForm"
      class="col-xs-12"
    >
      <div class="panel panel-default">
        <div class="panel-heading">
          <div class="row">
            <div class="col-xs-6">
              <h1 class="panel-title">
                {{ $t('action.contest.edit') }}
              </h1>
            </div>
            <div class="col-xs-6">
              <button
                type="button"
                class="close"
                aria-label="Dismiss"
                @click.prevent="showEditForm = false"
              >
                <span aria-hidden="true">
                  &times;
                </span>
              </button>
            </div>
          </div>
        </div>
        <div class="panel-body">
          <ContestForm :contest="contest" />
        </div>
      </div>
    </div>
    <div
      v-if="showDeleteWarning"
      class="col-xs-12"
    >
      <div class="panel panel-danger">
        <div class="panel-heading">
          <div class="row">
            <div class="col-xs-6">
              <h1 class="panel-title">
                {{ $t('action.contest.delete') }}
              </h1>
            </div>
            <div class="col-xs-6">
              <button
                type="button"
                class="close"
                aria-label="Dismiss"
                @click.prevent="showDeleteWarning = false"
              >
                <span aria-hidden="true">
                  &times;
                </span>
              </button>
            </div>
          </div>
        </div>
        <div class="panel-body">
          <h1>
            {{ $t('msg.warning') }}
          </h1>
          <h3>
            {{ $t('msg.contest.delete.warning') }}
            <br>
            <small>
              {{ $t('msg.contest.delete.confirm') }}
            </small>
          </h3>

          <div class="form-group">
            <input
              v-model="deleteConfirmationText"
              type="text"
              class="form-control"
            >
          </div>
          <button
            class="btn btn-danger"
            :disabled="!deleteConfirmation"
            @click="deleteContest()"
          >
            {{ $t('action.contest.delete') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import ApplicationsAdmissionsApi from '@/api/applicationsAdmissions'

import ContestForm from '@/components/ContestForm'

import checkPermission from '@/mixins/checkPermission'

import { mapState } from 'vuex'

export default {
  name: 'ContestManagement',
  components: {
    ContestForm
  },
  mixins: [checkPermission],
  data: function () {
    return {
      showEditForm: false,
      showDeleteWarning: false,
      deleteConfirmationText: ''
    }
  },
  computed: {
    deleteConfirmation () {
      return this.deleteConfirmationText === this.contest.contestName
    },
    ...mapState({
      contest: 'currentContest'
    })
  },
  methods: {
    deleteContest () {
      ApplicationsAdmissionsApi.deleteContest(this.contest.id)
        .then(response => {
          this.$store.dispatch('showSuccessMessage', { messageKey: 'feedback.success' })
          this.$router.push({ name: 'ContestList' })
        })
        .catch(error => {
          this.$router.push({ path: `/error/${error.response.status}` })
        })
    }
  }
}
</script>
