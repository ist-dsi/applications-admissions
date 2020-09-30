<template>
  <div class="center">
    <img
      src="~@/assets/images/icon-error.svg"
      alt="Error"
    >
    <h1>
      {{ $t(errorTitleKey) }}
    </h1>
    <p>
      {{ $t(errorMessageKey) }}
    </p>
  </div>
</template>

<script>
export default {
  name: 'PageError',
  props: {
    errorCode: {
      type: String,
      required: true,
      validator: function (value) {
        return [
          '400',
          '403',
          '404',
          '500',
          '504'
        ].indexOf(value) !== -1
      }
    }
  },
  computed: {
    errorTitleKey () {
      return 'error.' + this.errorCode + '.title'
    },
    errorMessageKey () {
      return 'error.' + this.errorCode + '.message'
    }
  }
}
</script>

<style>
.center {
  margin: auto;
  width: 50%;
  text-align: center;
}

.center img {
  display: block;
  margin-left: auto;
  margin-right: auto;
}
</style>
