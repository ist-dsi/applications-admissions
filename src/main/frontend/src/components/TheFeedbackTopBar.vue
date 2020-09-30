<template>
  <div
    v-if="topMessage.active"
    class="alert alert-dismissible"
    :class="{ 'alert-success': topMessage.type === 'success', 'alert-info': topMessage.type === 'info', 'alert-warning': topMessage.type === 'warning', 'alert-danger': topMessage.type === 'danger'}"
    role="alert"
  >
    <button
      v-if="topMessage.dismiss"
      type="button"
      class="close"
      aria-label="Dismiss this message"
      @click.prevent="dismissMessage()"
    >
      <span aria-hidden="true">
        &times;
      </span>
    </button>
    {{ topMessage.msg[$i18n.locale()] }}
  </div>
</template>

<script>
import { mapActions, mapState } from 'vuex'

export default {
  name: 'FeedbackTopBar',
  computed: {
    ...mapState({
      topMessage: state => state.topMessage
    })
  },
  methods: {
    ...mapActions([
      'hideTopMessage'
    ]),
    dismissMessage () {
      this.hideTopMessage()
    }
  }
}
</script>
