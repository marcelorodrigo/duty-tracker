<template>
  <UCard variant="subtle" class="overflow-hidden">
    <template #header>
      <h3 class="text-sm font-semibold text-gray-700 dark:text-gray-300">Overtime Allowance Rates</h3>
    </template>

    <div class="overflow-x-auto">
      <table class="w-full text-sm">
        <thead>
          <tr class="border-b border-gray-200 dark:border-gray-700">
            <th class="text-left py-2 px-3 font-medium text-gray-500 dark:text-gray-400 w-32">Hour</th>
            <th class="text-center py-2 px-3 font-medium text-gray-500 dark:text-gray-400">Weekday</th>
            <th class="text-center py-2 px-3 font-medium text-gray-500 dark:text-gray-400">Saturday</th>
            <th class="text-center py-2 px-3 font-medium text-gray-500 dark:text-gray-400">Sun / Holiday</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="row in rows"
            :key="row.timeFrom"
            class="border-b border-gray-100 dark:border-gray-800 last:border-0"
          >
            <td class="py-1.5 px-3 text-gray-500 dark:text-gray-400 tabular-nums text-xs">
              {{ formatTime(row.timeFrom) }} – {{ formatTime(row.timeTo) }}
            </td>
            <td :class="['text-center py-1.5 px-3', cellColor(row.weekday.percentage)]">
              <InlinePercentageInput
                :model-value="row.weekday.percentage"
                :rate-id="row.weekday.id"
                :on-save="onSave"
              />
            </td>
            <td :class="['text-center py-1.5 px-3', cellColor(row.saturday.percentage)]">
              <InlinePercentageInput
                :model-value="row.saturday.percentage"
                :rate-id="row.saturday.id"
                :on-save="onSave"
              />
            </td>
            <td :class="['text-center py-1.5 px-3', cellColor(row.sundayHoliday.percentage)]">
              <InlinePercentageInput
                :model-value="row.sundayHoliday.percentage"
                :rate-id="row.sundayHoliday.id"
                :on-save="onSave"
              />
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </UCard>
</template>

<script setup lang="ts">
import type { CompensationRate } from '~/stores/compensation'

export interface GridRow {
  timeFrom: string
  timeTo: string
  weekday: CompensationRate
  saturday: CompensationRate
  sundayHoliday: CompensationRate
}

interface Props {
  rows: GridRow[]
  onSave: (id: number, percentage: string) => Promise<void>
}

defineProps<Props>()

function formatTime(time: string): string {
  return time.length >= 5 ? time.slice(0, 5) : time
}

function cellColor(percentage: string): string {
  const n = parseFloat(percentage)
  if (isNaN(n) || n === 0) return ''
  if (n <= 35) return 'bg-yellow-50 dark:bg-yellow-900/20'
  return 'bg-amber-100 dark:bg-amber-900/30'
}
</script>
