<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { getOverview, getTrend } from '../../api/dashboard'

const overview = ref({})
const trend = ref(null)
const chartRef = ref(null)
let chartInstance = null

function getThemeColor(name, fallback) {
  if (typeof window === 'undefined') return fallback
  return getComputedStyle(document.documentElement).getPropertyValue(name).trim() || fallback
}

const pendingOrders = computed(
  () => (overview.value.pendingMaintenanceCount || 0) + (overview.value.pendingCleaningCount || 0)
)

const occupancyRate = computed(() => Number(overview.value.occupancyRate || 0))
const totalHouses = computed(() => overview.value.totalHouseCount || 0)
const occupiedHouses = computed(() => overview.value.occupiedHouseCount || 0)
const vacantHouses = computed(() => Math.max(totalHouses.value - occupiedHouses.value, 0))

const metricCards = computed(() => [
  {
    eyebrow: 'Portfolio',
    label: '在管房源',
    value: totalHouses.value,
    meta: '当前纳入统一经营视图的全部房源资产。'
  },
  {
    eyebrow: 'Occupancy',
    label: '已出租房源',
    value: occupiedHouses.value,
    meta: `仍有 ${vacantHouses.value} 套处于可出租状态。`
  },
  {
    eyebrow: 'Performance',
    label: '整体出租率',
    value: `${occupancyRate.value}%`,
    meta: '从经营视角快速理解当前出租表现。'
  },
  {
    eyebrow: 'Service',
    label: '待处理工单',
    value: pendingOrders.value,
    meta: '包含维修和保洁在内的全部服务请求。'
  }
])

const strategicNotes = computed(() => [
  {
    title: '出租节奏',
    desc:
      occupancyRate.value >= 88
        ? '出租率保持在较高水平，更适合围绕续租、优质房源升级和租金结构优化做精细化运营。'
        : '当前出租率仍有提升空间，建议优先梳理空置房源定价、上架速度和带看转化链路。'
  },
  {
    title: '服务响应',
    desc:
      pendingOrders.value <= 5
        ? '工单压力处于可控区间，可以把更多精力投入到预防性保养和服务体验复盘。'
        : '工单总量正在走高，建议优先清理高优先级维修和临近预约时间的保洁任务。'
  },
  {
    title: '经营视图',
    desc: '把房源健康度、服务节奏和履约表现放进同一块看板里，能帮助团队更快形成统一判断。'
  }
])

async function loadData() {
  overview.value = await getOverview()
  trend.value = await getTrend()
  await nextTick()
  renderChart()
}

function renderChart() {
  if (!chartRef.value || !trend.value) return

  const primary = getThemeColor('--primary', '#1c69d4')
  const lineColor = getThemeColor('--border', '#dfdfdf')
  const textMuted = getThemeColor('--text-muted', '#757575')

  chartInstance = chartInstance || echarts.init(chartRef.value)
  chartInstance.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(26, 26, 26, 0.96)',
      borderWidth: 0,
      textStyle: { color: '#ffffff' }
    },
    legend: {
      top: 0,
      right: 0,
      textStyle: { color: textMuted },
      data: ['出租率', '新增工单']
    },
    grid: {
      left: 8,
      right: 8,
      top: 52,
      bottom: 6,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      axisTick: { show: false },
      axisLine: { lineStyle: { color: lineColor } },
      axisLabel: { color: textMuted },
      data: trend.value.labels
    },
    yAxis: [
      {
        type: 'value',
        name: '出租率',
        axisLabel: { color: textMuted, formatter: '{value}%' },
        splitLine: { lineStyle: { color: lineColor } }
      },
      {
        type: 'value',
        name: '工单',
        axisLabel: { color: textMuted },
        splitLine: { show: false }
      }
    ],
    series: [
      {
        name: '出租率',
        type: 'line',
        smooth: true,
        yAxisIndex: 0,
        symbol: 'circle',
        symbolSize: 7,
        data: trend.value.occupancyRates,
        lineStyle: { width: 3, color: primary },
        itemStyle: {
          color: primary,
          borderColor: '#ffffff',
          borderWidth: 2
        }
      },
      {
        name: '新增工单',
        type: 'bar',
        yAxisIndex: 1,
        barWidth: 14,
        data: trend.value.newOrders,
        itemStyle: {
          color: '#262626'
        }
      }
    ]
  })
}

function handleResize() {
  chartInstance?.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
  chartInstance = null
})
</script>

<template>
  <div class="page-shell dashboard-shell">
    <section class="hero-card dashboard-hero">
      <div class="dashboard-hero__main">
        <span class="page-eyebrow">Overview</span>
        <h2>用更清晰、更硬朗的方式掌控房源经营全局。</h2>
        <p class="page-subtitle">
          将房源资产、出租表现与服务交付组织到一块统一看板里，让团队更快读懂变化、识别重点并推进运营动作。
        </p>
      </div>

      <div class="dashboard-hero__aside">
        <div class="hero-aside__label">Property Coverage</div>
        <div class="hero-aside__value">{{ totalHouses }}</div>
        <div class="hero-aside__meta">当前正在统一经营中的房源总量。</div>
      </div>
    </section>

    <section class="metric-strip">
      <article v-for="card in metricCards" :key="card.label" class="metric-card">
        <div class="metric-label">{{ card.eyebrow }}</div>
        <div class="metric-value">{{ card.value }}</div>
        <div class="metric-meta">{{ card.label }} / {{ card.meta }}</div>
      </article>
    </section>

    <section class="dashboard-grid">
      <article class="panel-card">
        <div class="panel-head">
          <span class="page-eyebrow">Performance</span>
          <h3>近 7 日经营趋势</h3>
          <p>同步观察出租率和新增工单变化，帮助团队提前识别去化波动与服务压力。</p>
        </div>
        <div ref="chartRef" class="chart"></div>
      </article>

      <article class="panel-card">
        <div class="panel-head">
          <span class="page-eyebrow">Operating Notes</span>
          <h3>经营建议</h3>
          <p>基于当前资产状态和服务动态，生成更适合运营团队执行的管理提示。</p>
        </div>

        <div class="note-list">
          <div v-for="item in strategicNotes" :key="item.title" class="note-card">
            <div class="note-card__title">{{ item.title }}</div>
            <div class="note-card__desc">{{ item.desc }}</div>
          </div>
        </div>
      </article>
    </section>
  </div>
</template>

<style scoped lang="scss">
.dashboard-shell {
  grid-template-rows: auto auto minmax(0, 1fr);
}

.dashboard-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.6fr) minmax(280px, 0.8fr);
  gap: var(--space-4);
}

.dashboard-hero h2 {
  margin: 10px 0 8px;
  font-size: clamp(30px, 4vw, 52px);
  font-weight: 300;
  line-height: 1.06;
  text-transform: uppercase;
}

.dashboard-hero__aside {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 18px;
  color: var(--white);
  background: var(--dark);
}

.hero-aside__label {
  color: var(--silver);
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.hero-aside__value {
  margin-top: 10px;
  font-size: 56px;
  font-weight: 300;
  line-height: 0.95;
}

.hero-aside__meta {
  margin-top: 10px;
  color: rgba(255, 255, 255, 0.72);
  line-height: 1.6;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.55fr) minmax(320px, 1fr);
  gap: var(--space-4);
}

.panel-head h3 {
  margin: 8px 0 6px;
  font-size: 24px;
  font-weight: 300;
  text-transform: uppercase;
}

.panel-head p,
.note-card__desc {
  color: var(--text-muted);
  line-height: 1.6;
}

.chart {
  min-height: 360px;
  margin-top: 12px;
}

.note-list {
  display: grid;
  gap: var(--space-3);
  margin-top: 16px;
}

.note-card {
  padding: 14px;
  border: 1px solid var(--border);
  background: var(--white);
}

.note-card__title {
  font-weight: 900;
}

@media (max-width: 1100px) {
  .dashboard-hero,
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}
</style>
