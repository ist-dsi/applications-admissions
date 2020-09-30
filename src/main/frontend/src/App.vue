<template>
  <div id="app">
    <the-feedback-top-bar />
    <router-view :key="$route.fullPath" />
  </div>
</template>

<script>
import TheFeedbackTopBar from '@/components/TheFeedbackTopBar.vue'

export default {
  name: 'App',
  components: {
    TheFeedbackTopBar
  }
}
</script>

<style>
@import 'bootstrap/dist/css/bootstrap.css'
</style>
