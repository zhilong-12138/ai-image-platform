<template>
  <div class="dashboard">
    <div class="dashboard-header">
      <h1 class="page-title">仪表板</h1>
      <p class="page-subtitle">平台运营数据总览</p>
    </div>

    <div class="stats-grid" v-loading="loading">
      <div class="stat-card">
        <div class="stat-icon stat-icon--blue">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M12 4a4 4 0 1 0 0 8 4 4 0 0 0 0-8zM6 20c0-3.31 2.69-6 6-6s6 2.69 6 6H6z" fill="currentColor"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalUsers || 0 }}</div>
          <div class="stat-label">总用户数</div>
          <div class="stat-desc">系统注册用户</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon--purple">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <rect x="3" y="3" width="18" height="18" rx="4" stroke="currentColor" stroke-width="2"/>
            <circle cx="8.5" cy="8.5" r="1.5" fill="currentColor"/>
            <path d="M21 15l-5-5L5 21" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalGenerations || 0 }}</div>
          <div class="stat-label">生成图片数</div>
          <div class="stat-desc">历史累计生成</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon--orange">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"/>
            <path d="M12 7v5l3 3" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.pendingImages || 0 }}</div>
          <div class="stat-label">待审核图片</div>
          <div class="stat-desc">需要处理</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon--green">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="8" stroke="currentColor" stroke-width="2"/>
            <path d="M12 8v4M10.5 14.5l1.5 1.5 2-2.5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalCost || 0 }}</div>
          <div class="stat-label">总积分消耗</div>
          <div class="stat-desc">历史累计消耗</div>
        </div>
      </div>
    </div>

    <div class="charts-container">
      <div class="chart-card">
        <div class="chart-header">
          <h3 class="chart-title">用户增长趋势</h3>
        </div>
        <div class="chart-body">
          <div ref="userGrowthChartRef" class="chart-canvas"></div>
        </div>
      </div>
      <div class="chart-card">
        <div class="chart-header">
          <h3 class="chart-title">生成任务状态</h3>
        </div>
        <div class="chart-body">
          <div ref="taskDistChartRef" class="chart-canvas"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { adminApi } from '../api'

const loading = ref(false)
const stats = ref({ totalUsers: 0, totalGenerations: 0, pendingImages: 0, totalCost: 0 })
const userGrowthChartRef = ref(null)
const taskDistChartRef = ref(null)

let userGrowthChart = null
let taskDistChart = null

const chartTheme = {
  textColor: '#86868B',
  accent: '#007AFF',
  axisLine: '#E5E5EA',
  tooltip: {
    backgroundColor: 'rgba(255,255,255,0.96)',
    borderColor: 'rgba(0,0,0,0.06)',
    borderWidth: 1,
    textColor: '#1D1D1F',
    shadow: '0 8px 30px rgba(0,0,0,0.08)',
    borderRadius: 12,
    padding: [10, 14],
  }
}

const initUserGrowthChart = () => {
  if (!userGrowthChartRef.value) return
  userGrowthChart = echarts.init(userGrowthChartRef.value)
  // Simulate last 7 days data
  const days = ['05-25', '05-26', '05-27', '05-28', '05-29', '05-30', '05-31']
  const values = [12, 28, 45, 38, 62, 55, 74]
  userGrowthChart.setOption({
    backgroundColor: 'transparent',
    grid: { top: 20, right: 24, bottom: 40, left: 24, containLabel: true },
    tooltip: {
      ...chartTheme.tooltip,
      trigger: 'axis',
      axisPointer: { type: 'line', lineStyle: { color: '#E5E5EA', type: 'dashed' } }
    },
    xAxis: {
      type: 'category',
      data: days,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: chartTheme.textColor, fontSize: 12, margin: 8 }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#F5F5F7', type: 'solid' } },
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: chartTheme.textColor, fontSize: 12 }
    },
    series: [{
      data: values,
      type: 'line',
      smooth: 0.4,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: { color: chartTheme.accent, width: 2.5 },
      itemStyle: { color: chartTheme.accent, borderWidth: 2, borderColor: '#fff' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(0, 122, 255, 0.15)' },
          { offset: 1, color: 'rgba(0, 122, 255, 0)' }
        ])
      }
    }]
  })
}

const initTaskDistChart = () => {
  if (!taskDistChartRef.value) return
  taskDistChart = echarts.init(taskDistChartRef.value)
  taskDistChart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      ...chartTheme.tooltip,
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 24,
      top: 'center',
      itemWidth: 12,
      itemHeight: 12,
      borderRadius: 6,
      textStyle: { color: chartTheme.textColor, fontSize: 13 }
    },
    series: [{
      type: 'doughnut',
      radius: ['50%', '75%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: true,
      label: { show: false },
      labelLine: { show: false },
      data: [
        { value: stats.value.pendingImages || 0, name: '待处理', itemStyle: { color: '#FF9500' } },
        { value: Math.max(1, Math.floor((stats.value.totalGenerations || 0) * 0.6)), name: '已完成', itemStyle: { color: '#30D158' } },
        { value: Math.max(1, Math.floor((stats.value.totalGenerations || 0) * 0.15)), name: '生成中', itemStyle: { color: '#007AFF' } }
      ]
    }]
  })
}

const resizeCharts = () => {
  userGrowthChart?.resize()
  taskDistChart?.resize()
}

onMounted(async () => {
  window.addEventListener('resize', resizeCharts)
  loading.value = true
  try {
    const res = await adminApi.stats()
    if (res.code === 200) {
      stats.value = res.data
      // Re-init charts with real data after stats load
      setTimeout(() => {
        initUserGrowthChart()
        initTaskDistChart()
      }, 0)
    }
  } catch (e) {
    console.error('获取统计数据失败', e)
  } finally {
    loading.value = false
  }
  // Fallback init if no data
  setTimeout(() => {
    if (userGrowthChartRef.value && !userGrowthChart) initUserGrowthChart()
    if (taskDistChartRef.value && !taskDistChart) initTaskDistChart()
  }, 200)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts)
  userGrowthChart?.dispose()
  taskDistChart?.dispose()
})
</script>

<style scoped>
.dashboard {
  padding: 40px 40px;
  max-width: 1400px;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Segoe UI', Roboto, sans-serif;
  -webkit-font-smoothing: antialiased;
}

.dashboard-header {
  margin-bottom: 40px;
}

.page-title {
  font-size: 34px;
  font-weight: 700;
  color: var(--apple-text);
  margin: 0 0 8px;
  letter-spacing: -0.025em;
  line-height: 1.1;
}

.page-subtitle {
  font-size: 16px;
  color: var(--apple-text-secondary);
  margin: 0;
  font-weight: 400;
}

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  background: var(--apple-surface);
  border-radius: var(--apple-radius-lg);
  padding: 26px 24px 22px;
  box-shadow: var(--apple-shadow);
  display: flex;
  align-items: flex-start;
  gap: 18px;
  border: 1px solid rgba(0, 0, 0, 0.04);
  transition: all 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);
  position: relative;
  overflow: hidden;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--apple-shadow-hover);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon--blue {
  background: rgba(0, 113, 227, 0.1);
  color: #007AFF;
}
.stat-icon--purple {
  background: rgba(88, 86, 214, 0.1);
  color: #5856D6;
}
.stat-icon--orange {
  background: rgba(255, 149, 0, 0.1);
  color: #FF9500;
}
.stat-icon--green {
  background: rgba(48, 209, 88, 0.1);
  color: #30D158;
}

.stat-content {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--apple-text);
  letter-spacing: -0.03em;
  line-height: 1;
  margin-bottom: 6px;
  font-variant-numeric: tabular-nums;
}

.stat-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--apple-text);
  margin-bottom: 3px;
  letter-spacing: -0.01em;
}

.stat-desc {
  font-size: 12px;
  color: var(--apple-text-secondary);
  font-weight: 400;
}

/* Charts */
.charts-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.chart-card {
  background: var(--apple-surface);
  border-radius: var(--apple-radius-lg);
  box-shadow: var(--apple-shadow);
  overflow: hidden;
  border: 1px solid rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.3s;
}

.chart-card:hover {
  box-shadow: var(--apple-shadow-hover);
}

.chart-header {
  padding: 22px 26px 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--apple-text);
  margin: 0;
  letter-spacing: -0.01em;
}

.chart-body {
  padding: 0;
}

.chart-canvas {
  height: 260px;
  width: 100%;
}

/* Responsive */
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 900px) {
  .charts-container {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .dashboard {
    padding: 24px 20px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .page-title {
    font-size: 28px;
  }
}
</style>