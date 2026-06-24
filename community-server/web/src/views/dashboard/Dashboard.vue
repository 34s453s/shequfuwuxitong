<template>
  <div class="dashboard-page">
    <!-- 指标卡片 -->
    <div class="stats-cards">
      <div class="stat-card card-blue">
        <div class="stat-value">{{ stats.totalResidents || 0 }}</div>
        <div class="stat-label">总居民数</div>
      </div>
      <div class="stat-card card-orange">
        <div class="stat-value">{{ stats.monthlyRepairs || 0 }}</div>
        <div class="stat-label">本月报修数</div>
      </div>
      <div class="stat-card card-red">
        <div class="stat-value">{{ stats.pendingRepairs || 0 }}</div>
        <div class="stat-label">待处理报修</div>
      </div>
      <div class="stat-card card-green">
        <div class="stat-value">{{ stats.totalIdleItems || 0 }}</div>
        <div class="stat-label">闲置物品数</div>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="charts-row">
      <div class="chart-box">
        <h4>近7天报修趋势</h4>
        <div ref="lineChartRef" class="chart-container"></div>
      </div>
      <div class="chart-box">
        <h4>报修状态分布</h4>
        <div ref="pieChartRef" class="chart-container"></div>
      </div>
      <div class="chart-box">
        <h4>月度活动报名人次</h4>
        <div ref="barChartRef" class="chart-container"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import request from '@/utils/request'

const stats = ref({})

const lineChartRef = ref()
const pieChartRef = ref()
const barChartRef = ref()
let lineChart = null
let pieChart = null
let barChart = null
let timer = null

// 更新图表数据
const updateCharts = () => {
  if (!lineChart || !pieChart || !barChart) return

  // 折线图数据
  const repairTrend = stats.value.repairTrend || []
  const days = repairTrend.map(item => item.date.substring(5)) // 只取 MM-DD
  const values = repairTrend.map(item => item.count || 0)

  lineChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: days, axisLabel: { color: '#bfcbd9' } },
    yAxis: { type: 'value', axisLabel: { color: '#bfcbd9' }, splitLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } } },
    series: [{ data: values, type: 'line', smooth: true, areaStyle: { color: 'rgba(64,158,255,0.3)' }, lineStyle: { color: '#409EFF', width: 2 }, itemStyle: { color: '#409EFF' } }]
  })

  // 饼图数据
  const statusPieData = stats.value.statusPieData || []
  pieChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, textStyle: { color: '#bfcbd9' } },
    series: [{
      type: 'pie', radius: ['40%', '70%'], data: statusPieData,
      label: { color: '#bfcbd9' },
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.5)' } }
    }]
  })

  // 柱状图数据
  const signupTrend = stats.value.signupTrend || []
  const months = signupTrend.map(item => item.month)
  const signupValues = signupTrend.map(item => item.count || 0)
  barChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: months, axisLabel: { color: '#bfcbd9' } },
    yAxis: { type: 'value', axisLabel: { color: '#bfcbd9' }, splitLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } } },
    series: [{ data: signupValues, type: 'bar', barWidth: '40%', itemStyle: { color: '#67C23A', borderRadius: [4, 4, 0, 0] } }]
  })
}

const initCharts = async () => {
  // 动态导入 ECharts
  let echarts
  try {
    echarts = await import('echarts')
  } catch (e) {
    // ECharts 未安装时使用简单占位
    if (lineChartRef.value) lineChartRef.value.innerHTML = '<div style="display:flex;align-items:center;justify-content:center;height:100%;color:#909399">需安装 echarts: npm i echarts</div>'
    if (pieChartRef.value) pieChartRef.value.innerHTML = '<div style="display:flex;align-items:center;justify-content:center;height:100%;color:#909399">需安装 echarts</div>'
    if (barChartRef.value) barChartRef.value.innerHTML = '<div style="display:flex;align-items:center;justify-content:center;height:100%;color:#909399">需安装 echarts</div>'
    return
  }

  // 初始化图表
  if (lineChartRef.value) lineChart = echarts.init(lineChartRef.value)
  if (pieChartRef.value) pieChart = echarts.init(pieChartRef.value)
  if (barChartRef.value) barChart = echarts.init(barChartRef.value)

  // 第一次更新图表
  updateCharts()
}

const fetchStats = async () => {
  try {
    const res = await request.get('/stats/dashboard')
    if (res.code === 200) {
      stats.value = res.data || stats.value
      // 数据获取后更新图表
      nextTick(updateCharts)
    }
  } catch (e) {
    console.error('获取统计数据失败:', e)
  }
}

const handleResize = () => { lineChart?.resize(); pieChart?.resize(); barChart?.resize() }

onMounted(async () => {
  await nextTick()
  await initCharts()
  await fetchStats()
  window.addEventListener('resize', handleResize)
  // 30秒轮询
  timer = setInterval(fetchStats, 30000)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  lineChart?.dispose()
  pieChart?.dispose()
  barChart?.dispose()
  clearInterval(timer)
})
</script>

<style scoped>
.dashboard-page {
  padding: 20px;
  background: #1d2b3a;
  min-height: calc(100vh - 60px);
  margin: -20px;
  padding: 20px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  padding: 24px;
  border-radius: 10px;
  text-align: center;
  color: #fff;
  backdrop-filter: blur(10px);
}
.stat-value { font-size: 36px; font-weight: 700; margin-bottom: 8px; }
.stat-label { font-size: 14px; opacity: 0.85; }

.card-blue   { background: rgba(64,158,255,0.7); }
.card-orange { background: rgba(230,162,60,0.7); }
.card-red    { background: rgba(245,108,108,0.7); }
.card-green  { background: rgba(103,194,58,0.7); }

.charts-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.chart-box {
  background: rgba(255, 255, 255, 0.06);
  border-radius: 10px;
  padding: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}
.chart-box h4 {
  margin: 0 0 12px;
  color: #e0e6ed;
  font-size: 14px;
  font-weight: 500;
}
.chart-container { width: 100%; height: 280px; }
</style>
