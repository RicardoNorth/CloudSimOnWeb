<template>
  <n-card title="示例运行">
    <n-space vertical>
      <n-select
        v-model:value="selectedId"
        :options="exampleOptions"
        placeholder="请选择示例"
        style="width: 240px"
      />

      <n-button type="success" @click="runExample" :loading="loading">
        运行示例
      </n-button>

      <n-data-table
        :columns="columns"
        :data="results"
        :pagination="false"
        class="mt-4"
      />

      <ResultChart v-if="results.length > 0" :results="results" />
    </n-space>
  </n-card>
</template>

<script setup>
import { ref } from 'vue'
import { NCard, NSpace, NSelect, NButton, NDataTable, useMessage } from 'naive-ui'
import axios from 'axios'
import ResultChart from './ResultChart.vue'

const selectedId = ref(1)
const results = ref([])
const loading = ref(false)
const message = useMessage()

const exampleOptions = [
  { label: 'CloudSim 示例 1', value: 1 },
  { label: 'CloudSim 示例 2', value: 2 },
  { label: 'CloudSim 示例 3', value: 3 }
]

const columns = [
  { title: 'Cloudlet ID', key: 'cloudletId' },
  { title: 'VM ID', key: 'vmId' },
  { title: '开始时间', key: 'startTime' },
  { title: '完成时间', key: 'finishTime' }
]

const runExample = async () => {
  loading.value = true
  try {
    const response = await axios.post('/api/examples/run', null, {
      params: { exampleId: selectedId.value }
    })
    results.value = response.data
    message.success('示例运行成功')
  } catch (e) {
    console.error('运行失败', e)
    message.error('运行失败，请检查后端服务')
  } finally {
    loading.value = false
  }
}
</script>
