<template>
  <form
    class="form-horizontal"
    @submit="send"
  >
    <div
      class="form-group"
      :class="errors.name.hasError ? 'has-error' : ''"
    >
      <label
        for="name"
        class="control-label col-sm-2"
      >
        {{ $t('label.contest.name') }}
      </label>
      <div class="col-sm-10">
        <input
          v-model="name"
          type="text"
          required
          class="form-control"
        >
        <span
          v-if="errors.name.hasError"
          class="help-block"
        >
          {{ errors.name.errorMessage }}
        </span>
      </div>
    </div>
    <div
      class="form-group"
      :class="errors.begin.hasError ? 'has-error' : ''"
    >
      <label
        for="beginDate"
        class="control-label col-sm-2"
      >
        {{ $t('label.contest.begin') }}
      </label>
      <div class="col-sm-10">
        <div class="input-group">
          <input
            v-model="beginDate"
            type="date"
            required
            pattern="\d{4}-\d{2}-\d{2}"
            placeholder="YYYY-MM-DD"
            class="form-control"
          >
          <span class="input-group-addon">
            {{ $t('label.at') }}
          </span>
          <input
            v-model="beginTime"
            type="time"
            required
            pattern="[0-9]{2}:[0-9]{2}"
            placeholder="HH:mm"
            class="form-control"
          >
        </div>
        <span
          v-if="errors.begin.hasError"
          class="help-block"
        >
          {{ errors.begin.errorMessage }}
        </span>
      </div>
    </div>
    <div
      class="form-group"
      :class="errors.end.hasError ? 'has-error' : ''"
    >
      <label
        for="endDate"
        class="control-label col-sm-2"
      >
        {{ $t('label.contest.end') }}
      </label>
      <div class="col-sm-10">
        <div class="input-group">
          <input
            v-model="endDate"
            type="date"
            required
            pattern="\d{4}-\d{2}-\d{2}"
            placeholder="YYYY-MM-DD"
            class="form-control"
          >
          <span class="input-group-addon">
            {{ $t('label.at') }}
          </span>
          <input
            v-model="endTime"
            type="time"
            required
            pattern="[0-9]{2}:[0-9]{2}"
            placeholder="HH:mm"
            class="form-control"
          >
        </div>
        <span
          v-if="errors.end.hasError"
          class="help-block"
        >
          {{ errors.end.errorMessage }}
        </span>
      </div>
    </div>
    <div class="form-group">
      <div class="col-sm-10 col-sm-offset-2">
        <button class="btn btn-default">
          {{ $t('action.submit') }}
        </button>
      </div>
    </div>
  </form>
</template>

<script>
import ApplicationsAdmissionsApi from '@/api/applicationsAdmissions'

import * as types from '@/store/mutation-types'

import moment from 'moment'

export default {
  name: 'ContestForm',
  props: {
    contest: {
      type: Object,
      default: undefined
    }
  },
  data: function () {
    return {
      name: this.contest ? this.contest.contestName : '',
      beginDate: this.contest ? this.contest.beginDate.slice(0, 10) : '',
      beginTime: this.contest ? this.contest.beginDate.slice(11) : '',
      endDate: this.contest ? this.contest.endDate.slice(0, 10) : '',
      endTime: this.contest ? this.contest.endDate.slice(11) : '',
      errors: {
        name: {
          hasError: false,
          errorMessage: ''
        },
        begin: {
          hasError: false,
          errorMessage: ''
        },
        end: {
          hasError: false,
          errorMessage: ''
        }
      }
    }
  },
  computed: {
    beginDateTime: function () {
      return `${this.beginDate} ${this.beginTime}`
    },
    endDateTime: function () {
      return `${this.endDate} ${this.endTime}`
    },
    hasAnyError: function () {
      return this.errors.name.hasError || this.errors.begin.hasError || this.errors.end.hasError
    }
  },
  watch: {
    name: function (val) {
      this.errors.name.hasError = false
      this.errors.name.errorMessage = ''
    },
    beginDate: function (val) {
      this.errors.begin.hasError = false
      this.errors.begin.errorMessage = ''
    },
    beginTime: function (val) {
      this.errors.begin.hasError = false
      this.errors.begin.errorMessage = ''
    },
    endDate: function (val) {
      this.errors.end.hasError = false
      this.errors.end.errorMessage = ''
    },
    endTime: function (val) {
      this.errors.end.hasError = false
      this.errors.end.errorMessage = ''
    }
  },
  methods: {
    clearErrors () {
      this.errors.name.hasError = false
      this.errors.name.errorMessage = ''

      this.errors.begin.hasError = false
      this.errors.begin.errorMessage = ''

      this.errors.end.hasError = false
      this.errors.end.errorMessage = ''
    },
    send: function (e) {
      e.preventDefault()

      this.clearErrors()

      // check input
      if (!this.name) {
        this.errors.name.errorMessage = this.$t('validation.missing')
        this.errors.name.hasError = true
      }

      if (!this.beginDate || !this.beginTime) {
        this.errors.begin.errorMessage = this.$t('validation.missing')
        this.errors.begin.hasError = true
      } else if (!moment(this.beginDateTime, 'YYYY-MM-DD HH:mm', true).isValid()) {
        this.errors.begin.errorMessage = this.$t('validation.malformed', { format: 'YYYY-MM-DD HH:mm' })
        this.errors.begin.hasError = true
      }

      if (!this.endDate || !this.endTime) {
        this.errors.end.errorMessage = this.$t('validation.missing')
        this.errors.end.hasError = true
      } else if (!moment(this.endDateTime, 'YYYY-MM-DD HH:mm', true).isValid()) {
        this.errors.end.errorMessage = this.$t('validation.malformed', { format: 'YYYY-MM-DD HH:mm' })
        this.errors.end.hasError = true
      }

      if (this.hasAnyError) {
        this.$store.dispatch('showErrorMessage', { messageKey: 'feedback.validation' })
        return
      }

      if (!this.contest) {
        ApplicationsAdmissionsApi.createContest(this.name, this.beginDateTime, this.endDateTime)
          .then(response => {
            this.$store.dispatch('showSuccessMessage', { messageKey: 'feedback.success' })
            this.$router.push({ name: 'Contest', params: { id: response.id } })
          })
          .catch(error => {
            this.$router.push({ path: `/error/${error.response.status}` })
          })
      } else {
        ApplicationsAdmissionsApi.editContest(this.contest.id, this.name, this.beginDateTime, this.endDateTime)
          .then(response => {
            this.$store.dispatch('showSuccessMessage', { messageKey: 'feedback.success' })
            this.$store.commit(types.RECEIVE_CONTEST, response)
          })
          .catch(error => {
            this.$router.push({ path: `/error/${error.response.status}` })
          })
      }
    }
  }
}
</script>
