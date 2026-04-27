<template>
  <UTable :data="entries" :columns="computedColumns">
    <template #manualOverride-cell="{ row }">
      <UBadge v-if="(row.original as any).manualOverride" color="warning">Overridden</UBadge>
    </template>
    <template #rateType-cell="{ row }">
      <UBadge v-if="(row.original as any).rateType" :color="(row.original as any).rateType === 'SUNDAY_HOLIDAY' ? 'warning' : 'info'">
        {{ (row.original as any).rateType }}
      </UBadge>
    </template>
    <template #isAllowanceEntry-cell="{ row }">
      <UBadge v-if="type === 'overtime'" :color="(row.original as any).isAllowanceEntry ? 'success' : 'neutral'">
        {{ (row.original as any).isAllowanceEntry ? 'Allowance' : 'Base' }}
      </UBadge>
    </template>
    <template #actions-cell="{ row }">
      <div class="flex gap-1">
        <UButton size="xs" @click="emit('edit', row.original)">Edit</UButton>
        <UButton size="xs" color="error" @click="emit('delete', (row.original as any).id)">Delete</UButton>
      </div>
    </template>
  </UTable>
</template>

<script setup lang="ts">
const props = defineProps<{ entries: unknown[]; type: 'oncall' | 'overtime' }>()
const emit = defineEmits<{ edit: [row: unknown]; delete: [id: number] }>()

const oncallColumns = [
  { accessorKey: 'date', header: 'Date' },
  { accessorKey: 'hours', header: 'Hours' },
  { accessorKey: 'rateType', header: 'Rate Type' },
  { accessorKey: 'manualOverride', header: 'Override' },
  { accessorKey: 'actions', header: '' },
]

const overtimeColumns = [
  { accessorKey: 'overtimeHours', header: 'OT Hours' },
  { accessorKey: 'allowanceHours', header: 'Allowance Hours' },
  { accessorKey: 'allowancePercentage', header: 'Allowance %' },
  { accessorKey: 'isAllowanceEntry', header: 'Type' },
  { accessorKey: 'manualOverride', header: 'Override' },
  { accessorKey: 'actions', header: '' },
]

const computedColumns = computed(() => props.type === 'oncall' ? oncallColumns : overtimeColumns)
</script>
