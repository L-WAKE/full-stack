<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { getOverview, getTrend } from '../../api/dashboard'

const overview = ref({})
const trend = ref(null)
const chartRef = ref(null)
let chartInstance = null

const metricCards = computed(() => {
  const pendingOrders = (overview.value.pendingMaintenanceCount || 0) + (overview.value.pendingCleaningCount || 0)
  const occupied = overview.value.occupiedHouseCount || 0
  const total = overview.value.totalHouseCount || 0
  const vacancy = Math.max(total - occupied, 0)

  return [
    {
      label: '房源总量',
      value: total,
      meta: '覆盖当前租赁资产底盘'
    },
    {
      label: '已出租房源',
      value: occupied,
      meta: `空置房源 ${vacancy} 套`
    },
    {
      label: '整体出租率',
      value: `${overview.value.occupancyRate || 0}%`,
      meta: '建议与项目、区域维度联动追踪'
    },
    {
      label: '待处理工单',
      value: pendingOrders,
      meta: '包含维修与保洁任务'
    }
  ]
})

const actionTips = computed(() => {
  const occupancyRate = Number(overview.value.occupancyRate || 0)
  const pendingOrders = (overview.value.pendingMaintenanceCount || 0) + (overview.value.pendingCleaningCount || 0)

  return [
    {
      title: '出租节奏',
      desc: occupancyRate >= 88 ? '整体出租率表现稳定，可重点关注高租金房源的续租策略。' : '出租率还有提升空间，建议优先梳理空置房源的价格与上架效率。'
    },
    {
      title: '工单响应',
      desc: pendingOrders <= 5 ? '当前待处理工单压力较低，适合推进服务质量复盘。' : '待处理工单偏多，建议优先清理高优先级报修与临近预约单。'
    },
    {
      title: '经营视图',
      desc: '将看板、台账与人员任务放在同一视觉体系下，便于运营团队日常高频切换。'
    }
  ]
})

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
      backgroundColor: 'rgba(15, 23, 42, 0.88)',
      borderWidth: 0,
      textStyle: {
        color: '#fff'
      }
    },
    legend: {
      top: 0,
      itemWidth: 10,
      itemHeight: 10,
      textStyle: {
        color: '#516174'
      },
      data: ['出租率', '新增工单']
    },
    grid: {
      left: 18,
      right: 14,
      top: 56,
      bottom: 8,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      axisLine: {
        lineStyle: {
          color: 'rgba(148, 163, 184, 0.24)'
        }
      },
      axisLabel: {
        color: '#718198'
      },
      data: trend.value.labels
    },
    yAxis: [
      {
        type: 'value',
        name: '出租率',
        axisLabel: {
          color: '#718198',
          formatter: '{value}%'
        },
        splitLine: {
          lineStyle: {
            color: 'rgba(148, 163, 184, 0.16)'
          }
        }
      },
      {
        type: 'value',
        name: '工单',
        axisLabel: {
          color: '#718198'
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
        symbolSize: 8,
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
            { offset: 0, color: 'rgba(37, 99, 235, 0.26)' },
            { offset: 1, color: 'rgba(37, 99, 235, 0.03)' }
          ])
        }
      },
      {
        name: '新增工单',
        type: 'bar',
        yAxisIndex: 1,
        barWidth: 18,
        data: trend.value.newOrders,
        itemStyle: {
          borderRadius: [10, 10, 4, 4],
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
    <section class="hero-card dashboard-hero">
      <div class="dashboard-hero__copy">
        <span class="page-eyebrow">Command Center</span>
        <h2>用更现代的经营后台方式查看租赁资产全局。</h2>
        <p>
          把房源状态、出租率变化和服务工单放在统一视图里，
          让运营团队第一眼就能知道今天应该优先处理什么。
        </p>
      </div>
      <div class="dashboard-hero__chips">
        <div class="hero-chip">
          <span>出租率</span>
          <strong>{{ overview.occupancyRate || 0 }}%</strong>
        </div>
        <div class="hero-chip">
          <span>待办工单</span>
          <strong>{{ (overview.pendingMaintenanceCount || 0) + (overview.pendingCleaningCount || 0) }}</strong>
        </div>
      </div>
    </section>

    <section class="metric-strip">
      <article v-for="card in metricCards" :key="card.label" class="metric-card">
        <div class="metric-label">{{ card.label }}</div>
        <div class="metric-value">{{ card.value }}</div>
        <div class="metric-meta">{{ card.meta }}</div>
      </article>
    </section>

    <section class="dashboard-grid">
      <article class="panel-card chart-panel">
        <div class="panel-card__header">
          <div>
            <h3 class="panel-card__title">近 7 日经营趋势</h3>
            <div class="panel-card__desc">将出租率与新增工单叠加展示，便于同时观察收益与服务压力。</div>
          </div>
        </div>
        <div ref="chartRef" class="chart"></div>
      </article>

      <article class="panel-card insight-panel">
        <div class="panel-card__header">
          <div>
            <h3 class="panel-card__title">经营提示</h3>
            <div class="panel-card__desc">结合当前数据给出一组更适合运营节奏的页面提示。</div>
          </div>
        </div>

        <div class="insight-list">
          <div v-for="item in actionTips" :key="item.title" class="insight-item">
            <div class="insight-item__title">{{ item.title }}</div>
            <div class="insight-item__desc">{{ item.desc }}</div>
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
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.dashboard-hero__copy {
  max-width: 720px;
}

.dashboard-hero__copy h2 {
  margin: 18px 0 12px;
  font-size: clamp(28px, 3.3vw, 40px);
  line-height: 1.12;
  letter-spacing: -0.04em;
}

.dashboard-hero__copy p {
  margin: 0;
  color: var(--text-muted);
  font-size: 15px;
  line-height: 1.8;
}

.dashboard-hero__chips {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(2, minmax(140px, 1fr));
  min-width: 320px;
}

.hero-chip {
  padding: 18px 20px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.76);
  box-shadow: 0 14px 32px rgba(15, 23, 42, 0.06);
}

.hero-chip span {
  color: var(--text-muted);
  font-size: 13px;
}

.hero-chip strong {
  display: block;
  margin-top: 12px;
  font-size: 30px;
  letter-spacing: -0.04em;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.8fr) minmax(320px, 0.95fr);
  gap: 18px;
  min-height: 0;
}

.chart-panel,
.insight-panel {
  min-height: 0;
}

.chart {
  height: 100%;
  min-height: 360px;
}

.insight-list {
  display: grid;
  gap: 14px;
}

.insight-item {
  padding: 18px;
  border-radius: 20px;
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.92), rgba(255, 255, 255, 0.92));
  border: 1px solid rgba(148, 163, 184, 0.14);
}

.insight-item__title {
  font-size: 15px;
  font-weight: 700;
}

.insight-item__desc {
  margin-top: 8px;
  color: var(--text-muted);
  line-height: 1.75;
}

@media (max-width: 1120px) {
  .dashboard-hero,
  .dashboard-grid {
    grid-template-columns: 1fr;
  }

  .dashboard-hero {
    flex-direction: column;
    align-items: stretch;
  }

  .dashboard-hero__chips {
    min-width: 0;
  }
}

@media (max-width: 768px) {
  .dashboard-hero__chips {
    grid-template-columns: 1fr;
  }
}
</style>
