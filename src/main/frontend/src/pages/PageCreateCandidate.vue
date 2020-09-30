<template>
  <div>
    <div class="page-header">
      <h1>
        {{ $t('action.candidate.create') }}
      </h1>
    </div>

    <div class="page-body">
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
            {{ $t('label.candidate.name') }}
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
          :class="errors.email.hasError ? 'has-error' : ''"
        >
          <label
            for="email"
            class="control-label col-sm-2"
          >
            {{ $t('label.candidate.email') }}
          </label>
          <div class="col-sm-10">
            <input
              v-model="email"
              type="email"
              required
              class="form-control"
            >
            <span
              v-if="errors.email.hasError"
              class="help-block"
            >
              {{ errors.email.errorMessage }}
            </span>
          </div>
        </div>

        <div
          v-if="!isLoggedIn && recaptcha.key"
          class="form-group"
          :class="errors.recaptcha.hasError ? 'has-error' : ''"
        >
          <div class="col-sm-10 col-sm-offset-2">
            <VueRecaptcha
              :sitekey="recaptcha.key"
              :load-recaptcha-script="true"
              @verify="onVerify"
              @expired="onExpired"
            />
            <span
              v-if="errors.recaptcha.hasError"
              class="help-block"
            >
              {{ errors.recaptcha.errorMessage }}
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
    </div>
  </div>
</template>

<script>
import VueRecaptcha from 'vue-recaptcha'

import ApplicationsAdmissionsApi from '@/api/applicationsAdmissions'

import { mapState, mapGetters } from 'vuex'

export default {
  name: 'PageCreateCandidate',
  components: {
    VueRecaptcha
  },
  data: function () {
    return {
      name: '',
      email: '',
      recaptcha: {
        key: undefined,
        response: undefined
      },
      errors: {
        name: {
          hasError: false,
          errorMessage: ''
        },
        email: {
          hasError: false,
          errorMessage: ''
        },
        recaptcha: {
          hasError: false,
          errorMessage: ''
        }
      }
    }
  },
  computed: {
    ...mapState({
      contest: 'currentContest'
    }),
    hasAnyError: function () {
      return this.errors.name.hasError || this.errors.email.hasError || this.errors.recaptcha.hasError
    },
    ...mapGetters([
      'isLoggedIn'
    ])
  },
  watch: {
    name: function (val) {
      this.errors.name.hasError = false
      this.errors.name.errorMessage = ''
    },
    email: function (val) {
      this.errors.email.hasError = false
      this.errors.email.errorMessage = ''
    }
  },
  methods: {
    clearErrors () {
      this.errors.name.hasError = false
      this.errors.name.errorMessage = ''

      this.errors.email.hasError = false
      this.errors.email.errorMessage = ''

      this.errors.recaptcha.hasError = false
      this.errors.recaptcha.errorMessage = ''
    },
    send (e) {
      e.preventDefault()

      this.clearErrors()

      // check input
      if (!this.name) {
        this.errors.name.errorMessage = this.$t('validation.missing')
        this.errors.name.hasError = true
      }

      if (!this.email) {
        this.errors.email.errorMessage = this.$t('validation.missing')
        this.errors.email.hasError = true
      } else if (!this.isValidEmail(this.email)) {
        this.errors.email.errorMessage = this.$t('validation.malformed', { format: '<username>@<domain>.<tld>' })
        this.errors.email.hasError = true
      }

      if (!this.isLoggedIn && !this.recaptcha.response) {
        this.errors.recaptcha.errorMessage = this.$t('validation.missing')
        this.errors.recaptcha.hasError = true
      }

      if (this.hasAnyError) {
        this.$store.dispatch('showErrorMessage', { messageKey: 'feedback.validation' })
        return
      }

      ApplicationsAdmissionsApi.createCandidate(this.contest.id, this.name, this.email, this.recaptcha.response)
        .then(response => {
          this.$store.dispatch('showSuccessMessage', { messageKey: 'feedback.candidate.create' })
          this.$router.push({ name: 'Candidate', params: { id: response.id } })
        })
        .catch(error => {
          this.$router.push({ path: `/error/${error.response.status}` })
        })
    },
    isValidEmail (email) {
      const emailRegex = /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)+$/
      return emailRegex.test(email)
    },
    onVerify: function (response) {
      this.recaptcha.response = response
    },
    onExpired: function () {
      this.recaptcha.response = undefined
    }
  },
  beforeRouteEnter (to, from, next) {
    next(vm => {
      ApplicationsAdmissionsApi.getRecaptchaConfig()
        .then(response => {
          vm.recaptcha.key = response.siteKey
        })
        .catch(error => {
          this.$router.push({ path: `/error/${error.response.status}` })
        })
    })
  }
}
</script>
