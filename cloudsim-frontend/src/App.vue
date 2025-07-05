<template>
  <n-config-provider>
    <n-message-provider>
      <div class="min-h-screen bg-white py-8 px-4">
        <div class="flex justify-center items-center w-full mb-6 margin">
          <div class="mb-6 ml-[50vw] -translate-x-1/2 w-max flex gap-4">
            <n-button
              :type="view === 'simulate' ? 'primary' : 'default'"
              @click="view = 'simulate'"
              strong
            >
              资源调度
            </n-button>
            <n-button
              :type="view === 'example' ? 'primary' : 'default'"
              @click="view = 'example'"
              strong
            >
              示例运行
            </n-button>
          </div>
        </div>

        <!-- 主内容区域 -->
        <div class="max-w-5xl mx-auto">
          <SimulateView v-if="view === 'simulate'" />
          <ExampleRunner v-if="view === 'example'" />
        </div>
      </div>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import { ref } from 'vue'
import { NConfigProvider, NMessageProvider, NButton } from 'naive-ui'
import SimulateView from './components/SimulateView.vue'
import ExampleRunner from './components/ExampleRunner.vue'

const view = ref('simulate')
</script>
