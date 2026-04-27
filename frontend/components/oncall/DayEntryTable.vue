<template>
  <UTable :rows="entries" :columns="columns">
    <template #rateType-data="{ row }">
      <UBadge :color="row.rateType === 'SUNDAY_HOLIDAY' ? 'warning' : 'info'">
        {{ row.rateType === 'SUNDAY_HOLIDAY' ? 'Sun/Holiday' : 'Weekday/Sat' }}
      </UBadge>
    </template>
    <template #hours-data="{ row }">
      <span :class="row.capped ? 'text-warning font-medium' : ''">
        {{ row.hours }}h <span v-if="row.capped" class="text-xs">(capped)</span>
      </span>
    </template>
    <template #timeForTimeFlag-data="{ row }">
      <UBadge v-if="row.timeForTimeFlag" color="warning">Time-for-time</UBadge>
      <span v-else class="text-gray-400">—</span>
    </template>
    <template #manualOverride-data="{ row }">
      <UBadge v-if="row.manualOverride" color="neutral">Manual</UBadge>
    </template>
    <template #dayOff-data="{ row }">
      <UToggle
        :model-value="row.timeForTimeFlag"
        label="Day off"
        @update:model-value="val => emit('toggleDayOff', row.id, val)"
      />
    </template>
  </UTable>
</template>

<script setup lang="ts">
import type { OnCallDayEntry } from '~/stores/oncall'

defineProps<{ entries: OnCallDayEntry[] }>()
const emit = defineEmits<{
  toggleDayOff: [entryId: number, value: boolean]
}>()

const columns = [
  { key: 'date', label: 'Date' },
  { key: 'hours', label: 'Hours' },
  { key: 'rateType', label: 'Rate Type' },
  { key: 'timeForTimeFlag', label: 'Status' },
  { key: 'manualOverride', label: 'Override' },
  { key: 'dayOff', label: 'Mark Day Off' },
]
</script>
