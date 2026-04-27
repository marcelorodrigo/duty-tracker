<template>
  <UTable :rows="entries" :columns="computedColumns">
    <template #manualOverride-data="{ row }">
      <UBadge v-if="(row as any).manualOverride" color="warning">Overridden</UBadge>
    </template>
    <template #rateType-data="{ row }">
      <UBadge v-if="(row as any).rateType" :color="(row as any).rateType === 'SUNDAY_HOLIDAY' ? 'warning' : 'info'">
        {{ (row as any).rateType }}
      </UBadge>
    </template>
    <template #isAllowanceEntry-data="{ row }">
      <UBadge v-if="type === 'overtime'" :color="(row as any).isAllowanceEntry ? 'success' : 'neutral'">
        {{ (row as any).isAllowanceEntry ? 'Allowance' : 'Base' }}
      </UBadge>
    </template>
    <template #actions-data="{ row }">
      <div class="flex gap-1">
        <UButton size="xs" @click="emit('edit', row)">Edit</UButton>
        <UButton size="xs" color="error" @click="emit('delete', (row as any).id)">Delete</UButton>
      </div>
    </template>
  </UTable>
</template>

<script setup lang="ts">
const props = defineProps<{ entries: unknown[]; type: 'oncall' | 'overtime' }>()
const emit = defineEmits<{ edit: [row: unknown]; delete: [id: number] }>()

const oncallColumns = [
  { key: 'date', label: 'Date' },
  { key: 'hours', label: 'Hours' },
  { key: 'rateType', label: 'Rate Type' },
  { key: 'manualOverride', label: 'Override' },
  { key: 'actions', label: '' },
]

const overtimeColumns = [
  { key: 'overtimeHours', label: 'OT Hours' },
  { key: 'allowanceHours', label: 'Allowance Hours' },
  { key: 'allowancePercentage', label: 'Allowance %' },
  { key: 'isAllowanceEntry', label: 'Type' },
  { key: 'manualOverride', label: 'Override' },
  { key: 'actions', label: '' },
]

const computedColumns = computed(() => props.type === 'oncall' ? oncallColumns : overtimeColumns)
</script>
