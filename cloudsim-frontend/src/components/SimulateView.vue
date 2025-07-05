<template>
  <n-card title="CloudSim 资源调度模拟平台" class="max-w-5xl mx-auto mt-6">
    <n-form label-placement="left" label-width="auto" class="flex flex-col gap-4 mb-6">

      <!-- VM MIPS 配置 -->
      <n-card title="虚拟机配置" size="small">
        <div v-for="(vm, index) in vmConfigs" :key="index" class="flex items-center gap-2 mb-2">
          <span>VM{{ index }}</span>
          <n-input-number v-model:value="vm.mips" :min="100" :max="10000" /> MIPS
          <n-button text type="error" @click="removeVm(index)" v-if="vmConfigs.length > 1">删除</n-button>
        </div>
        <n-button size="small" @click="addVm">新增 VM</n-button>
      </n-card>

      <!-- Cloudlet 数量 -->
      <n-form-item label="Cloudlet 数量">
        <n-input-number v-model:value="cloudletCount" :min="1" />
      </n-form-item>

      <!-- 算法选择 -->
      <n-form-item label="调度算法">
        <n-select
          v-model:value="algorithm"
          :options="[
            { label: 'TimeShared', value: 'timeshared' },
            { label: 'SpaceShared', value: 'spaceshared' },
            { label: '最短完成时间调度（MCT）', value: 'mct' }
          ]"
          placeholder="请选择算法"
        />
      </n-form-item>

      <n-button type="primary" @click="runSimulation">运行模拟</n-button>
    </n-form>

    <n-divider>调度结果</n-divider>

    <n-data-table
      v-if="results.length"
      :columns="columns"
      :data="results"
      :pagination="false"
      bordered
    />

    <n-divider>图表可视化</n-divider>
    <ResultChart v-if="results.length > 0" :results="results" />
  </n-card>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import {
  NCard, NForm, NFormItem, NInputNumber, NSelect,
  NButton, NDataTable, NDivider
} from 'naive-ui'
import ResultChart from './ResultChart.vue'

const vmConfigs = ref([
  { mips: 1000 },
  { mips: 1000 }
])
const cloudletCount = ref(4)
const algorithm = ref('timeshared')
const results = ref([])

const addVm = () => {
  vmConfigs.value.push({ mips: 1000 })
}

const removeVm = (index) => {
  vmConfigs.value.splice(index, 1)
}

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
      vmCount: vmConfigs.value.length,
      cloudletCount: cloudletCount.value,
      algorithm: algorithm.value,
      vmMipsList: vmConfigs.value.map(vm => vm.mips)
    })
    results.value = response.data
  } catch (error) {
    console.error('模拟请求失败:', error)
    alert('请求失败，请确认后端服务是否正常运行')
  }
}
</script>
