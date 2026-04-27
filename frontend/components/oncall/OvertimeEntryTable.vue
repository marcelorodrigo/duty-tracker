<template>
  <UTable :rows="entries" :columns="columns" class="text-sm">
    <template #isAllowanceEntry-data="{ row }">
      <UBadge :color="row.isAllowanceEntry ? 'success' : 'info'">
        {{ row.isAllowanceEntry ? 'Allowance' : 'Base' }}
      </UBadge>
    </template>
    <template #manualOverride-data="{ row }">
      <UBadge v-if="row.manualOverride" color="neutral">Manual</UBadge>
    </template>
    <template #timeRange-data="{ row }">
      <span v-if="row.timeFrom && row.timeTo">{{ row.timeFrom }}–{{ row.timeTo }}</span>
      <span v-else class="text-gray-400">—</span>
    </template>
  </UTable>
</template>

<script setup lang="ts">
import type { OvertimeEntry } from '~/stores/oncall'

defineProps<{ entries: OvertimeEntry[] }>()

const columns = [
  { key: 'isAllowanceEntry', label: 'Type' },
  { key: 'timeRange', label: 'Time Range' },
  { key: 'overtimeHours', label: 'Overtime Hours' },
  { key: 'allowanceHours', label: 'Allowance Hours' },
  { key: 'allowancePercentage', label: 'Allowance %' },
  { key: 'manualOverride', label: 'Override' },
]
</script>
