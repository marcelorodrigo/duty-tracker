<script setup lang="ts">
import type { TableColumn } from '@nuxt/ui'
import type { IncidentResponse } from '~/types/incident'
import { formatDate, formatDateTime, formatTime, formatDuration } from '~/utils/dates'

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

function overtimeOptionLabel(entry: { isAllowanceEntry: boolean; allowancePercentage: string | null }): string {
  if (entry.isAllowanceEntry && entry.allowancePercentage != null) {
    return `${entry.allowancePercentage}% allowance`
  }
  return 'Overtime hours'
}

type StandbyRow = { date: string; plan: string; option: string; hours: string; capped: string }
type OvertimeRow = { incident: string; date: string; time: string; plan: string; option: string; hours: string | null }
type IncidentRow = { name: string; startDateTime: string; endDateTime: string; duration: string }

const standbyColumns: TableColumn<StandbyRow>[] = [
  { accessorKey: 'date', header: 'Date' },
  { accessorKey: 'plan', header: 'Plan' },
  { accessorKey: 'option', header: 'Option' },
  { accessorKey: 'hours', header: 'Hours' },
  { accessorKey: 'capped', header: 'Capped' },
]

const overtimeColumns: TableColumn<OvertimeRow>[] = [
  { accessorKey: 'incident', header: 'Incident' },
  { accessorKey: 'date', header: 'Date' },
  { accessorKey: 'time', header: 'Time' },
  { accessorKey: 'plan', header: 'Plan' },
  { accessorKey: 'option', header: 'Option' },
  { accessorKey: 'hours', header: 'Hours' },
]

const incidentColumns: TableColumn<IncidentRow>[] = [
  { accessorKey: 'name', header: 'Name' },
  { accessorKey: 'startDateTime', header: 'Start' },
  { accessorKey: 'endDateTime', header: 'End' },
  { accessorKey: 'duration', header: 'Duration' },
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
              duration: formatDuration(s.startDateTime, s.endDateTime),
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
            :columns="standbyColumns"
            :data="report.standbyLines.map(e => ({
              date: formatDate(e.date),
              plan: 'NL Allowances - Standby allowance',
              option: standbyRateLabel(e.rateType),
              hours: e.hours,
              capped: e.capped ? 'Yes (capped at 15h)' : 'No',
            }))"
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
            :columns="overtimeColumns"
            :data="report.overtimeLines.map(e => ({
              incident: e.incidentName,
              date: formatDate(e.date),
              time: `${formatTime(e.timeFrom)}–${formatTime(e.timeTo)}`,
              plan: 'NL Overtime Hours',
              option: overtimeOptionLabel(e),
              hours: e.isAllowanceEntry ? e.allowanceHours : e.overtimeHours,
            }))"
          />
        </UCard>
      </template>
    </div>
  </UContainer>
</template>
