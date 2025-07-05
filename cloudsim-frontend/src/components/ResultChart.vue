<template>
  <n-card title="执行时间图表" class="mt-4">
    <v-chart :option="chartOption" style="height: 400px; width: 100%;" />
  </n-card>
</template>

<script setup>
import { computed } from 'vue'
import { use } from 'echarts/core'
import { BarChart } from 'echarts/charts'
import {
  GridComponent,
  TooltipComponent,
  TitleComponent,
  LegendComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

use([
  BarChart,
  GridComponent,
  TooltipComponent,
  TitleComponent,
  LegendComponent,
  CanvasRenderer
])

const props = defineProps({
  results: {
    type: Array,
    required: true
  }
})

const chartOption = computed(() => {
  const cloudletIds = props.results.map(r => 'Cloudlet ' + r.cloudletId)
  const durations = props.results.map(r => (r.finishTime - r.startTime).toFixed(2))

  return {
    title: {
      text: '各 Cloudlet 执行时间'
    },
    tooltip: {},
    xAxis: {
      type: 'category',
      data: cloudletIds
    },
    yAxis: {
      name: '执行时间（s）'
    },
    series: [
      {
        name: '执行时长',
        type: 'bar',
        data: durations,
        itemStyle: {
          color: '#52c41a' // ✅ 绿色（也可用 'green'）
        }
      }
    ]
  }
})
</script>
