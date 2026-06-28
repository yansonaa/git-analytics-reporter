<template>
  <div class="dashboard">
    <el-page-header title="Git Analytics Dashboard" />

    <!-- 筛选栏 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="项目ID">
          <el-input v-model="query.projectId" placeholder="default" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="query.start" type="datetime" placeholder="开始时间" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="query.end" type="datetime" placeholder="结束时间" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadReport">查询</el-button>
          <el-button @click="exportExcel">导出 Excel</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 概览卡片 -->
    <el-row :gutter="20" v-if="report">
      <el-col :span="6">
        <el-card>
          <div class="stat-label">总提交数</div>
          <div class="stat-value">{{ report.teamOverview.totalCommits }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-label">活跃成员</div>
          <div class="stat-value">{{ report.teamOverview.activeMembers }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-label">日均提交</div>
          <div class="stat-value">{{ report.teamOverview.avgDailyCommits }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-label">净增代码行</div>
          <div class="stat-value">{{ report.teamOverview.totalNetLines }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 趋势图 + 热力图 -->
    <el-row :gutter="20" v-if="report" style="margin-top: 20px">
      <el-col :span="12">
        <el-card title="提交趋势">
          <v-chart class="chart" :option="trendOption" autoresize />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card title="提交时段热力图">
          <v-chart class="chart" :option="heatmapOption" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <!-- 成员排名 -->
    <el-card title="成员排名" v-if="report" style="margin-top: 20px">
      <el-table :data="report.memberRanking" style="width: 100%">
        <el-table-column type="index" label="排名" width="80" />
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="commits" label="提交数" sortable />
        <el-table-column prop="netLines" label="净增行数" sortable />
        <el-table-column prop="reviewComments" label="评审评论" sortable />
        <el-table-column prop="activityScore" label="活跃度评分" sortable />
      </el-table>
    </el-card>

    <!-- 异常提交 -->
    <el-card title="异常提交" v-if="report && report.anomalyList.length" style="margin-top: 20px">
      <el-table :data="report.anomalyList" style="width: 100%">
        <el-table-column prop="commitId" label="提交ID" width="320" />
        <el-table-column prop="author" label="作者" />
        <el-table-column prop="time" label="时间" />
        <el-table-column prop="reason" label="原因" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, HeatmapChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, VisualMapComponent, CalendarComponent } from 'echarts/components'
import { fetchReport, exportReport } from '../api'

use([CanvasRenderer, LineChart, HeatmapChart, GridComponent, TooltipComponent, LegendComponent, VisualMapComponent, CalendarComponent])

const query = reactive({
  projectId: 'project-1',
  start: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000),
  end: new Date()
})

const report = ref(null)

const trendOption = computed(() => {
  if (!report.value) return {}
  const t = report.value.trendData || { dates: [], values: [] }
  return {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: t.dates, axisLabel: { rotate: 45 } },
    yAxis: { type: 'value' },
    series: [{
      data: t.values,
      type: 'line',
      smooth: true,
      areaStyle: {}
    }]
  }
})

const heatmapOption = computed(() => {
  if (!report.value) return {}
  const h = report.value.heatmapData || { dayLabels: [], hourLabels: [], values: [] }
  const data = []
  h.values.forEach((row, dayIndex) => {
    row.forEach((val, hourIndex) => {
      data.push([hourIndex, dayIndex, val])
    })
  })
  return {
    tooltip: { position: 'top' },
    grid: { height: '50%', top: '10%' },
    xAxis: { type: 'category', data: h.hourLabels, splitArea: { show: true } },
    yAxis: { type: 'category', data: h.dayLabels, splitArea: { show: true } },
    visualMap: { min: 0, max: Math.max(...data.map(d => d[2]), 1), calculable: true, orient: 'horizontal', left: 'center', bottom: '15%' },
    series: [{
      name: '提交数',
      type: 'heatmap',
      data: data,
      label: { show: false },
      emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0, 0, 0, 0.5)' } }
    }]
  }
})

async function loadReport() {
  try {
    const startIso = new Date(query.start).toISOString()
    const endIso = new Date(query.end).toISOString()
    const res = await fetchReport(query.projectId, startIso, endIso)
    report.value = res.data
    ElMessage.success('报表加载成功')
  } catch (e) {
    ElMessage.error('加载失败: ' + (e.message || '未知错误'))
  }
}

async function exportExcel() {
  try {
    const startIso = new Date(query.start).toISOString()
    const endIso = new Date(query.end).toISOString()
    const res = await exportReport(query.projectId, startIso, endIso)
    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `GitAnalytics_${query.projectId}_${query.start.toISOString().slice(0,10)}_${query.end.toISOString().slice(0,10)}.xlsx`
    a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败: ' + (e.message || '未知错误'))
  }
}
</script>

<style scoped>
.dashboard { padding: 20px; }
.filter-card { margin-bottom: 20px; }
.stat-label { font-size: 14px; color: #666; }
.stat-value { font-size: 28px; font-weight: bold; color: #409EFF; margin-top: 8px; }
.chart { height: 300px; }
</style>
