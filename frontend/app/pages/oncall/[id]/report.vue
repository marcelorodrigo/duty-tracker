<script setup lang="ts">
import type { TableColumn } from '@nuxt/ui'
import type { IncidentResponse } from '~/types/incident'
import { formatDate, formatDateTime, formatDuration } from '~/utils/dates'
import { PLAN_STANDBY_ALLOWANCE, PLAN_OVERTIME_HOURS, CAPPED_LABEL } from '~/utils/constants'

const route = useRoute()
const periodId = Number(route.params.id)

const { report, loading, error, fetch } = useOnCallPeriodReport(periodId)
const { fetchById } = useIncidents(periodId)

const incidents = ref<IncidentResponse[]>([])

onMounted(async () => {
  await fetch()
  if (report.value && report.value.incidentIds.length > 0) {
    incidents.value = await Promise.all(report.value.incidentIds.map(id => fetchById(id)))
  }
})

function standbyRateLabel(rateType: string): string {
  return rateType === 'SUNDAY_HOLIDAY' ? 'Sunday/Holiday' : 'Monday–Saturday'
}

function overtimeOptionLabel(entry: { isAllowanceEntry: boolean, allowancePercentage: string | null }): string {
  if (entry.isAllowanceEntry && entry.allowancePercentage != null) {
    return `${entry.allowancePercentage}% allowance`
  }
  return 'Overtime hours'
}

type StandbyRow = { date: string, day: string, plan: string, option: string, hours: string, capped: string }
type OvertimeRow = { date: string, plan: string, option: string, hours: string }
type IncidentRow = { name: string, startDateTime: string, endDateTime: string, duration: string }

const standbyColumns: TableColumn<StandbyRow>[] = [
  { accessorKey: 'date', header: 'Date' },
  { accessorKey: 'day', header: 'Day' },
  { accessorKey: 'plan', header: 'Plan' },
  { accessorKey: 'option', header: 'Option' },
  { accessorKey: 'hours', header: 'Hours' },
  { accessorKey: 'capped', header: 'Capped' }
]

const overtimeColumns: TableColumn<OvertimeRow>[] = [
  { accessorKey: 'date', header: 'Date' },
  { accessorKey: 'plan', header: 'Plan' },
  { accessorKey: 'option', header: 'Option' },
  { accessorKey: 'hours', header: 'Hours' }
]

const standbySelection = ref<Record<string, boolean>>({})
const overtimeSelection = ref<Record<string, boolean>>({})

function onStandbySelect(_e: Event, row: any) {
  row.toggleSelected()
}

function onOvertimeSelect(_e: Event, row: any) {
  row.toggleSelected()
}

const standbyMeta = {
  class: {
    tr: (row: any) => `cursor-pointer ${row.getIsSelected() ? 'line-through opacity-50' : ''}`
  }
}
const overtimeMeta = {
  class: {
    tr: (row: any) => `cursor-pointer ${row.getIsSelected() ? 'line-through opacity-50' : ''}`
  }
}

const incidentColumns: TableColumn<IncidentRow>[] = [
  { accessorKey: 'name', header: 'Name' },
  { accessorKey: 'startDateTime', header: 'Start' },
  { accessorKey: 'endDateTime', header: 'End' },
  { accessorKey: 'duration', header: 'Duration' }
]
</script>

<template>
  <UContainer>
    <div class="py-6">
      <!-- Back + title -->
      <div class="flex items-center gap-2 mb-6">
        <NuxtLink :to="`/oncall/${periodId}`">
          <UButton
            icon="i-lucide-arrow-left"
            variant="ghost"
            color="neutral"
            aria-label="Back to period"
          />
        </NuxtLink>
        <h1 class="text-2xl font-semibold flex-1">
          On-call Period Report
        </h1>
      </div>

      <!-- Loading -->
      <div
        v-if="loading"
        class="flex justify-center py-12"
      >
        <UIcon
          name="i-lucide-loader-circle"
          class="animate-spin text-2xl text-(--ui-text-muted)"
        />
      </div>

      <!-- Error -->
      <UAlert
        v-else-if="error"
        color="error"
        icon="i-lucide-alert-circle"
        title="Failed to generate report"
        :description="error.message"
      />

      <!-- Report content -->
      <template v-else-if="report">
        <!-- Part 1: Summary -->
        <UCard class="mb-6">
          <template #header>
            <h2 class="text-lg font-semibold">
              Summary
            </h2>
          </template>

          <div class="space-y-1 text-sm">
            <p>
              <span class="text-(--ui-text-muted)">Period:</span>
              {{ formatDateTime(report.periodStart) }} → {{ formatDateTime(report.periodEnd) }}
            </p>
            <p>
              <span class="text-(--ui-text-muted)">Incidents:</span>
              {{ report.incidentCount === 0 ? 'No incidents reported' : report.incidentCount }}
            </p>
            <p v-if="report.holidays.length > 0">
              <span class="text-(--ui-text-muted)">Holidays:</span>
              {{ report.holidays.map(h => `${formatDate(h.date)}${h.name ? ` (${h.name})` : ''}`).join(', ') }}
            </p>
          </div>
        </UCard>

        <!-- Incident breakdown -->
        <UCard
          v-if="incidents.length > 0"
          class="mb-6"
        >
          <template #header>
            <h2 class="text-lg font-semibold">
              Incident Breakdown
            </h2>
          </template>

          <UTable
            :columns="incidentColumns"
            :data="incidents.map(s => ({
              name: s.name,
              startDateTime: formatDateTime(s.startDateTime),
              endDateTime: formatDateTime(s.endDateTime),
              duration: formatDuration(s.startDateTime, s.endDateTime)
            }))"
          />
        </UCard>

        <!-- Part 2: MyHR lines — Standby -->
        <UCard class="mb-6">
          <template #header>
            <h2 class="text-lg font-semibold">
              MyHR — Standby Lines
            </h2>
          </template>

          <UTable
            v-model:row-selection="standbySelection"
            :columns="standbyColumns"
            :data="report.standbyLines.map(e => ({
              date: formatDate(e.date),
              day: e.dayLabel,
              plan: PLAN_STANDBY_ALLOWANCE,
              option: standbyRateLabel(e.rateType),
              hours: e.hours,
              capped: e.capped ? CAPPED_LABEL : 'No'
            }))"
            :meta="standbyMeta"
            @select="onStandbySelect"
          />
        </UCard>

        <!-- Part 2: MyHR lines — Overtime -->
        <UCard class="mb-6">
          <template #header>
            <h2 class="text-lg font-semibold">
              MyHR — Overtime Lines
            </h2>
          </template>

          <div
            v-if="report.overtimeLines.length === 0"
            class="py-4 text-center text-sm text-(--ui-text-muted)"
          >
            No overtime entries.
          </div>

          <UTable
            v-else
            v-model:row-selection="overtimeSelection"
            :columns="overtimeColumns"
            :data="report.overtimeLines.map(e => ({
              date: formatDate(e.date),
              plan: PLAN_OVERTIME_HOURS,
              option: overtimeOptionLabel(e),
              hours: e.hours
            }))"
            :meta="overtimeMeta"
            @select="onOvertimeSelect"
          />
        </UCard>
      </template>
    </div>
  </UContainer>
</template>
