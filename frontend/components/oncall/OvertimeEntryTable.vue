<template>
  <UTable :data="entries" :columns="columns" class="text-sm">
    <template #isAllowanceEntry-cell="{ row }">
      <UBadge :color="row.original.isAllowanceEntry ? 'success' : 'info'">
        {{ row.original.isAllowanceEntry ? 'Allowance' : 'Base' }}
      </UBadge>
    </template>
    <template #manualOverride-cell="{ row }">
      <UBadge v-if="row.original.manualOverride" color="neutral">Manual</UBadge>
    </template>
    <template #timeRange-cell="{ row }">
      <span v-if="row.original.timeFrom && row.original.timeTo">{{ row.original.timeFrom }}–{{ row.original.timeTo }}</span>
      <span v-else class="text-gray-400">—</span>
    </template>
  </UTable>
</template>

<script setup lang="ts">
import type { OvertimeEntry } from '~/stores/oncall'

defineProps<{ entries: OvertimeEntry[] }>()

const columns = [
  { accessorKey: 'isAllowanceEntry', header: 'Type' },
  { accessorKey: 'timeRange', header: 'Time Range' },
  { accessorKey: 'overtimeHours', header: 'Overtime Hours' },
  { accessorKey: 'allowanceHours', header: 'Allowance Hours' },
  { accessorKey: 'allowancePercentage', header: 'Allowance %' },
  { accessorKey: 'manualOverride', header: 'Override' },
]
</script>
