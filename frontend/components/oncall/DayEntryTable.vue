<template>
  <UTable :data="entries" :columns="columns">
    <template #rateType-cell="{ row }">
      <UBadge :color="row.original.rateType === 'SUNDAY_HOLIDAY' ? 'warning' : 'info'">
        {{ row.original.rateType === 'SUNDAY_HOLIDAY' ? 'Sun/Holiday' : 'Weekday/Sat' }}
      </UBadge>
    </template>
    <template #hours-cell="{ row }">
      <span :class="row.original.capped ? 'text-warning font-medium' : ''">
        {{ row.original.hours }}h <span v-if="row.original.capped" class="text-xs">(capped)</span>
      </span>
    </template>
    <template #timeForTimeFlag-cell="{ row }">
      <UBadge v-if="row.original.timeForTimeFlag" color="warning">Time-for-time</UBadge>
      <span v-else class="text-gray-400">—</span>
    </template>
    <template #manualOverride-cell="{ row }">
      <UBadge v-if="row.original.manualOverride" color="neutral">Manual</UBadge>
    </template>
    <template #dayOff-cell="{ row }">
      <USwitch
        :model-value="row.original.timeForTimeFlag"
        label="Day off"
        @update:model-value="val => emit('toggleDayOff', row.original.id, val)"
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
  { accessorKey: 'date', header: 'Date' },
  { accessorKey: 'hours', header: 'Hours' },
  { accessorKey: 'rateType', header: 'Rate Type' },
  { accessorKey: 'timeForTimeFlag', header: 'Status' },
  { accessorKey: 'manualOverride', header: 'Override' },
  { accessorKey: 'dayOff', header: 'Mark Day Off' },
]
</script>
