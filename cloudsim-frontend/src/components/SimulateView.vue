<template>
  <n-card title="CloudSim 资源调度模拟平台" class="max-w-5xl mx-auto mt-6">
    <n-form label-placement="left" label-width="auto" class="flex flex-wrap gap-4 mb-6">
      <n-form-item label="VM 数量">
        <n-input-number v-model:value="vmCount" :min="1" />
      </n-form-item>

      <n-form-item label="Cloudlet 数量">
        <n-input-number v-model:value="cloudletCount" :min="1" />
      </n-form-item>

      <n-form-item label="调度算法">
        <n-select
          v-model:value="algorithm"
          :options="[
            { label: 'TimeShared', value: 'timeshared' },
            { label: 'SpaceShared', value: 'spaceshared' }
          ]"
          placeholder="请选择算法"
        />
      </n-form-item>

      <n-form-item>
        <n-button type="primary" @click="runSimulation">运行模拟</n-button>
      </n-form-item>
    </n-form>

    <n-divider>调度结果</n-divider>

    <n-data-table
      v-if="results.length"
      :columns="columns"
      :data="results"
      :pagination="false"
      bordered
    />

    <n-divider>图表可视化（开发中）</n-divider>

    <div class="h-64 bg-gray-100 rounded flex items-center justify-center text-gray-400">
      图表加载区域（待集成 ECharts）
    </div>
  </n-card>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { NCard, NForm, NFormItem, NInputNumber, NSelect, NButton, NDataTable, NDivider } from 'naive-ui'

const vmCount = ref(2)
const cloudletCount = ref(4)
const algorithm = ref('timeshared')
const results = ref([])

const columns = [
  { title: 'Cloudlet ID', key: 'cloudletId' },
  { title: 'VM ID', key: 'vmId' },
  {
    title: '开始时间',
    key: 'startTime',
    render: (row) => row.startTime.toFixed(2)
  },
  {
    title: '完成时间',
    key: 'finishTime',
    render: (row) => row.finishTime.toFixed(2)
  }
]

const runSimulation = async () => {
  try {
    const response = await axios.post('/api/simulate', {
      vmCount: vmCount.value,
      cloudletCount: cloudletCount.value,
      algorithm: algorithm.value
    })
    results.value = response.data
  } catch (error) {
    console.error('模拟请求失败:', error)
    alert('请求失败，请确认后端服务是否正常运行')
  }
}
</script>

<style scoped>
</style>
