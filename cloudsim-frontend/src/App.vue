<template>
  <n-config-provider>
    <n-message-provider>
      <SimulateView />
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import { NConfigProvider, NMessageProvider } from 'naive-ui'
import SimulateView from './components/SimulateView.vue'
</script>
