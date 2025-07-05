import { createApp, defineAsyncComponent } from 'vue'
import App from './App.vue'
import naive from 'naive-ui'
import 'echarts'

// 异步导入 vue-echarts
const VueECharts = defineAsyncComponent(() => import('vue-echarts'))

const app = createApp(App)

// 注册 vue-echarts 组件为 <v-chart />
app.component('v-chart', VueECharts)

// 使用 naive-ui
app.use(naive)

// 挂载应用
app.mount('#app')
