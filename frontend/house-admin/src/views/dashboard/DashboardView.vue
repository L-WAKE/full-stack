<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { getOverview, getTrend } from '../../api/dashboard'

const overview = ref({})
const trend = ref(null)
const chartRef = ref(null)
let chartInstance = null

const pendingOrders = computed(
  () => (overview.value.pendingMaintenanceCount || 0) + (overview.value.pendingCleaningCount || 0)
)

const occupancyRate = computed(() => Number(overview.value.occupancyRate || 0))
const totalHouses = computed(() => overview.value.totalHouseCount || 0)
const occupiedHouses = computed(() => overview.value.occupiedHouseCount || 0)
const vacantHouses = computed(() => Math.max(totalHouses.value - occupiedHouses.value, 0))

const metricCards = computed(() => [
  {
    eyebrow: '资产总览',
    label: '房源总量',
    value: totalHouses.value,
    meta: '当前纳入统一经营的在管房源资产',
    tone: 'default'
  },
  {
    eyebrow: '出租表现',
    label: '已出租房源',
    value: occupiedHouses.value,
    meta: `当前仍有 ${vacantHouses.value} 套房源可供出租`,
    tone: 'accent'
  },
  {
    eyebrow: '经营效率',
    label: '整体出租率',
    value: `${occupancyRate.value}%`,
    meta: '结合实时运营需求持续跟踪变化',
    tone: 'default'
  },
  {
    eyebrow: '服务工单',
    label: '待处理工单',
    value: pendingOrders.value,
    meta: '包含维修与保洁等服务请求',
    tone: 'success'
  }
])

const strategicNotes = computed(() => [
  {
    title: '租赁节奏',
    desc:
      occupancyRate.value >= 88
        ? '当前出租表现稳定，可以重点优化续租策略与优质房源结构，进一步提升经营质量。'
        : '出租率还有提升空间，建议优先梳理空置房源定价与上架效率，缩短去化周期。'
  },
  {
    title: '服务响应',
    desc:
      pendingOrders.value <= 5
        ? '当前服务压力可控，团队可以把更多精力投入到质量复盘和预防性运营工作中。'
        : '工单量正在上升，建议优先处理高优先级维修与临近预约的保洁任务。'
  },
  {
    title: '经营视角',
    desc: '把资产健康度、服务效率和客户履约表现放在同一经营视图里，帮助团队更快做出判断。'
  }
])

async function loadData() {
  overview.value = await getOverview()
  trend.value = await getTrend()
  await nextTick()
  renderChart()
}

function renderChart() {
  if (!chartRef.value || !trend.value) {
    return
  }

  chartInstance = chartInstance || echarts.init(chartRef.value)
  chartInstance.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(10, 15, 26, 0.92)',
      borderWidth: 0,
      textStyle: {
        color: '#f8fafc'
      }
    },
    legend: {
      top: 0,
      right: 0,
      itemWidth: 10,
      itemHeight: 10,
      textStyle: {
        color: '#64748b'
      },
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
      axisLine: {
        lineStyle: {
          color: 'rgba(148, 163, 184, 0.28)'
        }
      },
      axisLabel: {
        color: '#94a3b8'
      },
      data: trend.value.labels
    },
    yAxis: [
      {
        type: 'value',
        name: '出租率',
        axisLabel: {
          color: '#94a3b8',
          formatter: '{value}%'
        },
        splitLine: {
          lineStyle: {
            color: 'rgba(148, 163, 184, 0.14)'
          }
        }
      },
      {
        type: 'value',
        name: '工单',
        axisLabel: {
          color: '#94a3b8'
        },
        splitLine: {
          show: false
        }
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
        lineStyle: {
          width: 3,
          color: '#2563eb'
        },
        itemStyle: {
          color: '#2563eb',
          borderColor: '#ffffff',
          borderWidth: 2
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(37, 99, 235, 0.24)' },
            { offset: 1, color: 'rgba(37, 99, 235, 0.02)' }
          ])
        }
      },
      {
        name: '新增工单',
        type: 'bar',
        yAxisIndex: 1,
        barWidth: 14,
        data: trend.value.newOrders,
        itemStyle: {
          borderRadius: [10, 10, 3, 3],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#34d399' },
            { offset: 1, color: '#059669' }
          ])
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
    <section class="dashboard-hero">
      <div class="dashboard-hero__main">
        <span class="dashboard-hero__eyebrow">经营总览</span>
        <h2>用更简洁、更高级的方式掌控你的房源经营全局。</h2>
        <p>
          将房源资产、出租表现与服务交付整合到一个统一驾驶舱中，帮助团队更快洞察重点、
          更高效推进日常运营。
        </p>

        <div class="dashboard-hero__stats">
          <div class="hero-stat-card">
            <span>整体出租率</span>
            <strong>{{ occupancyRate }}%</strong>
          </div>
          <div class="hero-stat-card">
            <span>待处理工单</span>
            <strong>{{ pendingOrders }}</strong>
          </div>
          <div class="hero-stat-card">
            <span>当前空置</span>
            <strong>{{ vacantHouses }}</strong>
          </div>
        </div>
      </div>

      <div class="dashboard-hero__aside">
        <div class="hero-aside__label">资产快照</div>
        <div class="hero-aside__value">{{ totalHouses }}</div>
        <div class="hero-aside__meta">当前正在统一经营中的房源总量</div>

        <div class="hero-aside__trend">
          <div class="hero-aside__trend-item">
            <span>已出租</span>
            <strong>{{ occupiedHouses }}</strong>
          </div>
          <div class="hero-aside__trend-item">
            <span>可出租</span>
            <strong>{{ vacantHouses }}</strong>
          </div>
        </div>
      </div>
    </section>

    <section class="dashboard-metrics">
      <article
        v-for="card in metricCards"
        :key="card.label"
        class="dashboard-metric-card"
        :class="`dashboard-metric-card--${card.tone}`"
      >
        <span class="dashboard-metric-card__eyebrow">{{ card.eyebrow }}</span>
        <div class="dashboard-metric-card__label">{{ card.label }}</div>
        <div class="dashboard-metric-card__value">{{ card.value }}</div>
        <div class="dashboard-metric-card__meta">{{ card.meta }}</div>
      </article>
    </section>

    <section class="dashboard-grid">
      <article class="dashboard-panel dashboard-panel--chart">
        <div class="dashboard-panel__header">
          <div>
            <span class="dashboard-panel__eyebrow">Performance</span>
            <h3>近 7 日经营趋势</h3>
            <p>同时观察出租率与新增工单变化，提前识别经营波动与服务压力。</p>
          </div>
        </div>
        <div ref="chartRef" class="chart"></div>
      </article>

      <article class="dashboard-panel dashboard-panel--notes">
        <div class="dashboard-panel__header">
          <div>
            <span class="dashboard-panel__eyebrow">Operating notes</span>
            <h3>经营建议</h3>
            <p>基于当前资产状态与服务动态生成一组更适合运营团队执行的管理建议。</p>
          </div>
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
  gap: 18px;
}

.dashboard-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.7fr) minmax(300px, 0.85fr);
  gap: 18px;
  padding: 28px;
  border: 1px solid rgba(226, 232, 240, 0.9);
  background:
    radial-gradient(circle at top left, rgba(37, 99, 235, 0.16), transparent 26%),
    radial-gradient(circle at 86% 16%, rgba(52, 211, 153, 0.12), transparent 20%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 252, 0.98));
  box-shadow: 0 20px 55px rgba(15, 23, 42, 0.06);
}

.dashboard-hero__main {
  min-width: 0;
}

.dashboard-hero__eyebrow {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  color: #1d4ed8;
  border-radius: 999px;
  background: rgba(37, 99, 235, 0.10);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.dashboard-hero h2 {
  max-width: 760px;
  margin: 18px 0 12px;
  color: #0f172a;
  font-size: clamp(34px, 4vw, 52px);
  line-height: 0.98;
  letter-spacing: -0.05em;
}

.dashboard-hero p {
  max-width: 700px;
  margin: 0;
  color: #64748b;
  font-size: 15px;
  line-height: 1.8;
}

.dashboard-hero__stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-top: 26px;
}

.hero-stat-card {
  padding: 18px 20px;
  border: 1px solid rgba(226, 232, 240, 0.9);
  background: rgba(255, 255, 255, 0.86);
  box-shadow: 0 10px 26px rgba(15, 23, 42, 0.04);
}

.hero-stat-card span {
  color: #64748b;
  font-size: 13px;
}

.hero-stat-card strong {
  display: block;
  margin-top: 10px;
  color: #0f172a;
  font-size: 28px;
  letter-spacing: -0.03em;
}

.dashboard-hero__aside {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 22px;
  color: #f8fafc;
  background:
    radial-gradient(circle at top right, rgba(59, 130, 246, 0.46), transparent 30%),
    linear-gradient(180deg, #0f172a, #111827 65%, #162033);
  box-shadow: 0 20px 44px rgba(15, 23, 42, 0.16);
}

.hero-aside__label {
  color: rgba(226, 232, 240, 0.72);
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.hero-aside__value {
  margin-top: 14px;
  font-size: 64px;
  font-weight: 800;
  line-height: 0.95;
  letter-spacing: -0.06em;
}

.hero-aside__meta {
  margin-top: 12px;
  color: rgba(226, 232, 240, 0.74);
  line-height: 1.65;
}

.hero-aside__trend {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 26px;
}

.hero-aside__trend-item {
  padding: 14px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.hero-aside__trend-item span {
  color: rgba(226, 232, 240, 0.68);
  font-size: 12px;
}

.hero-aside__trend-item strong {
  display: block;
  margin-top: 8px;
  font-size: 26px;
}

.dashboard-metrics {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.dashboard-metric-card {
  position: relative;
  overflow: hidden;
  padding: 20px;
  border: 1px solid rgba(226, 232, 240, 0.86);
  background: linear-gradient(180deg, #ffffff, #f8fbff);
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.05);
}

.dashboard-metric-card::after {
  content: "";
  position: absolute;
  right: -18px;
  bottom: -24px;
  width: 96px;
  height: 96px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(37, 99, 235, 0.15), transparent 68%);
}

.dashboard-metric-card--success::after {
  background: radial-gradient(circle, rgba(16, 185, 129, 0.16), transparent 68%);
}

.dashboard-metric-card__eyebrow,
.dashboard-panel__eyebrow {
  display: inline-flex;
  align-items: center;
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.dashboard-metric-card__label {
  margin-top: 12px;
  color: #475569;
  font-size: 14px;
}

.dashboard-metric-card__value {
  margin-top: 12px;
  color: #0f172a;
  font-size: 34px;
  font-weight: 800;
  letter-spacing: -0.05em;
}

.dashboard-metric-card__meta {
  position: relative;
  z-index: 1;
  margin-top: 10px;
  color: #64748b;
  font-size: 13px;
  line-height: 1.65;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.65fr) minmax(320px, 0.95fr);
  gap: 18px;
  min-height: 0;
}

.dashboard-panel {
  min-height: 0;
  padding: 22px;
  border: 1px solid rgba(226, 232, 240, 0.9);
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.06);
}

.dashboard-panel__header h3 {
  margin: 10px 0 8px;
  color: #0f172a;
  font-size: 24px;
  line-height: 1.1;
  letter-spacing: -0.03em;
}

.dashboard-panel__header p {
  margin: 0;
  color: #64748b;
  line-height: 1.7;
}

.dashboard-panel--chart,
.dashboard-panel--notes {
  display: flex;
  flex-direction: column;
}

.chart {
  flex: 1;
  min-height: 360px;
  margin-top: 10px;
}

.note-list {
  display: grid;
  gap: 12px;
  margin-top: 18px;
}

.note-card {
  padding: 16px;
  border: 1px solid rgba(226, 232, 240, 0.9);
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.96), rgba(255, 255, 255, 0.94));
}

.note-card__title {
  color: #0f172a;
  font-size: 16px;
  font-weight: 700;
}

.note-card__desc {
  margin-top: 8px;
  color: #64748b;
  line-height: 1.7;
}

@media (max-width: 1280px) {
  .dashboard-hero {
    grid-template-columns: 1fr;
  }

  .dashboard-hero__stats,
  .dashboard-metrics,
  .dashboard-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .dashboard-hero,
  .dashboard-panel {
    padding: 18px;
  }

  .dashboard-hero h2 {
    font-size: 34px;
  }

  .dashboard-hero__stats,
  .dashboard-metrics,
  .dashboard-grid,
  .hero-aside__trend {
    grid-template-columns: 1fr;
  }

  .chart {
    min-height: 300px;
  }
}
</style>
