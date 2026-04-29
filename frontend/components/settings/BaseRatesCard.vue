<template>
  <UCard variant="subtle">
    <template #header>
      <h3 class="text-sm font-semibold text-gray-700 dark:text-gray-300">Base Rates</h3>
    </template>

    <div class="divide-y divide-gray-100 dark:divide-gray-800">
      <div
        v-for="rate in rates"
        :key="rate.id"
        class="flex items-center justify-between py-3 px-1"
      >
        <span class="text-sm text-gray-700 dark:text-gray-300">{{ labelFor(rate.rateCategory) }}</span>
        <InlinePercentageInput
          :model-value="rate.percentage"
          :rate-id="rate.id"
          :on-save="onSave"
        />
      </div>
    </div>
  </UCard>
</template>

<script setup lang="ts">
import type { CompensationRate } from '~/stores/compensation'

interface Props {
  rates: CompensationRate[]
  onSave: (id: number, percentage: string) => Promise<void>
}

defineProps<Props>()

function labelFor(category: CompensationRate['rateCategory']): string {
  switch (category) {
    case 'ONCALL_WEEKDAY_SATURDAY': return 'On-call Weekday / Saturday'
    case 'ONCALL_SUNDAY_HOLIDAY': return 'On-call Sunday / Holiday'
    case 'OVERTIME_BASE': return 'Overtime Base Rate'
    default: return category
  }
}
</script>
