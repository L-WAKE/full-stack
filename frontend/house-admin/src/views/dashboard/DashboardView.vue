<script setup>
import { nextTick, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { getOverview, getTrend } from '../../api/dashboard'

const overview = ref({})
const trend = ref(null)
let chart

async function loadData() {
  overview.value = await getOverview()
  trend.value = await getTrend()
  await nextTick()
  renderChart()
}

function renderChart() {
  if (!trend.value) return
  chart = chart || echarts.init(document.getElementById('dashboard-trend'))
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: {
      top: 0,
      data: ['出租率', '新增工单']
    },
    grid: { left: 34, right: 18, top: 44, bottom: 30 },
    xAxis: {
      type: 'category',
      data: trend.value.labels
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '出租率',
        data: trend.value.occupancyRates,
        type: 'line',
        smooth: true,
        areaStyle: { color: 'rgba(11, 114, 133, 0.15)' },
        lineStyle: { color: '#0b7285', width: 3 }
      },
      {
        name: '新增工单',
        data: trend.value.newOrders,
        type: 'bar',
        itemStyle: { color: '#f59f00' }
      }
    ]
  })
}

onMounted(loadData)
</script>

<template>
  <div class="page-shell">
    <div class="page-title">
      <div>
        <h2>首页看板</h2>
        <div class="page-subtitle">展示核心经营指标、出租趋势与待处理工单情况。</div>
      </div>
    </div>

    <div class="stats-grid">
      <div class="stat-card">
        <span>房源总数</span>
        <strong>{{ overview.totalHouseCount }}</strong>
      </div>
      <div class="stat-card">
        <span>已出租房源</span>
        <strong>{{ overview.occupiedHouseCount }}</strong>
      </div>
      <div class="stat-card">
        <span>整体出租率</span>
        <strong>{{ overview.occupancyRate }}%</strong>
      </div>
      <div class="stat-card">
        <span>待处理工单</span>
        <strong>{{ (overview.pendingMaintenanceCount || 0) + (overview.pendingCleaningCount || 0) }}</strong>
      </div>
    </div>

    <div class="chart-card">
      <div class="chart-title">近 7 日运营趋势</div>
      <div id="dashboard-trend" class="chart"></div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  padding: 18px;
}

.stat-card {
  min-height: 126px;
  padding: 24px;
  border-radius: 18px;
  background: linear-gradient(135deg, #ffffff, #f6fbfc);
  border: 1px solid var(--line-color);
}

.stat-card span {
  color: var(--text-muted);
}

.stat-card strong {
  display: block;
  margin-top: 14px;
  font-size: 34px;
}

.chart-card {
  padding: 22px;
  border-radius: 20px;
  background: var(--bg-card);
  border: 1px solid var(--line-color);
}

.chart-title {
  font-weight: 700;
  margin-bottom: 12px;
}

.chart {
  height: 340px;
}

@media (max-width: 960px) {
  .stats-grid {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
