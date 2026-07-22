<script setup lang="ts">
import type { TableColumn } from '@nuxt/ui'
import { formatDate, formatDateTime } from '~/utils/dates'
import { createCommonTableColumns, formatCappedLabel } from '~/utils/table'

const route = useRoute()
const periodId = Number(route.params.id)

const { earnings, loading, error, fetch } = useEarnings(periodId)

onMounted(async () => {
  await fetch()
})

type StandbyRow = { date: string, day: string, compensation: string, hours: string, amount: string, capped: string }
type IncidentRow = { incident: string, hours: string, subtotal: string }

const standbyColumns: TableColumn<StandbyRow>[] = [
  ...createCommonTableColumns<StandbyRow>(['date', 'day']),
  { accessorKey: 'compensation', header: 'Compensation' },
  ...createCommonTableColumns<StandbyRow>(['hours']),
  { accessorKey: 'amount', header: 'Amount' },
  ...createCommonTableColumns<StandbyRow>(['capped'])
]

const incidentColumns: TableColumn<IncidentRow>[] = [
  { accessorKey: 'incident', header: 'Incident' },
  ...createCommonTableColumns<IncidentRow>(['hours']),
  { accessorKey: 'subtotal', header: 'Subtotal' }
]

function formatAmount(amount: number | string): string {
  const num = typeof amount === 'string' ? Number(amount) : amount
  return `€${num.toFixed(2)}`
}

const standbyTotal = computed(() => {
  if (!earnings.value) return 0
  return earnings.value.standbyLines.reduce((sum, line) => sum + Number(line.amount), 0)
})

const incidentTotal = computed(() => {
  if (!earnings.value) return 0
  return earnings.value.incidentLines.reduce((sum, line) => sum + Number(line.subtotal), 0)
})
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
          My Earnings
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
        title="Failed to load earnings"
        :description="error.message"
      />

      <!-- Earnings content -->
      <template v-else-if="earnings">
        <!-- Summary -->
        <UCard class="mb-6">
          <template #header>
            <h2 class="text-lg font-semibold">
              Summary
            </h2>
          </template>

          <div class="space-y-1 text-sm">
            <p>
              <span class="text-(--ui-text-muted)">Period:</span>
              {{ formatDateTime(earnings.periodStart) }} → {{ formatDateTime(earnings.periodEnd) }}
            </p>
          </div>
        </UCard>

        <!-- Standby Earnings -->
        <UCard class="mb-6">
          <template #header>
            <h2 class="text-lg font-semibold">
              Standby Earnings
            </h2>
          </template>

          <UTable
            :columns="standbyColumns"
            :data="earnings.standbyLines.map(e => ({
              date: formatDate(e.date),
              day: e.dayLabel,
              compensation: e.compensationLabel,
              hours: e.hours,
              amount: formatAmount(e.amount),
              capped: formatCappedLabel(e.capped)
            }))"
          />
        </UCard>

        <!-- Incident Earnings -->
        <UCard class="mb-6">
          <template #header>
            <h2 class="text-lg font-semibold">
              Incident Earnings
            </h2>
          </template>

          <div
            v-if="earnings.incidentLines.length === 0"
            class="py-4 text-center text-sm text-(--ui-text-muted)"
          >
            No incident entries.
          </div>

          <template v-else>
            <UTable
              :columns="incidentColumns"
              :data="earnings.incidentLines.map(e => ({
                incident: e.incidentName,
                hours: e.hoursSummary,
                subtotal: formatAmount(e.subtotal)
              }))"
            />
          </template>
        </UCard>

        <!-- Summary Totals -->
        <div class="grid grid-cols-3 gap-4">
          <!-- Standby Subtotal -->
          <div class="border border-(--ui-border) rounded-lg px-6 py-4">
            <p class="text-sm text-(--ui-text-muted) mb-1">
              Standby Subtotal
            </p>
            <p class="text-2xl font-semibold">
              {{ formatAmount(standbyTotal) }}
            </p>
          </div>

          <!-- Incident Subtotal -->
          <div class="border border-(--ui-border) rounded-lg px-6 py-4">
            <p class="text-sm text-(--ui-text-muted) mb-1">
              Incident Subtotal
            </p>
            <p class="text-2xl font-semibold">
              {{ formatAmount(incidentTotal) }}
            </p>
          </div>

          <!-- Grand Total -->
          <div class="border border-(--ui-border) rounded-lg px-6 py-4 bg-accent/10">
            <p class="text-sm text-(--ui-text-muted) mb-1">
              Grand Total (bruto)
            </p>
            <p class="text-2xl font-semibold">
              {{ formatAmount(earnings.grandTotal) }}
            </p>
          </div>
        </div>
      </template>
    </div>
  </UContainer>
</template>
