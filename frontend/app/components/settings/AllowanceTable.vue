<script setup lang="ts">
import type { TableColumn } from '@nuxt/ui'
import type { AllowanceSavePayload, PivotRow } from '~/types/compensation'

defineProps<{
  rows: PivotRow[]
}>()

const emit = defineEmits<{
  save: [payload: AllowanceSavePayload]
}>()

function handleSave(payload: AllowanceSavePayload) {
  emit('save', payload)
}

const columns: TableColumn<PivotRow>[] = [
  {
    accessorKey: 'slot',
    header: 'Time Slot'
  },
  {
    accessorKey: 'weekday',
    header: 'Monday to Friday'
  },
  {
    accessorKey: 'saturday',
    header: 'Saturday'
  },
  {
    accessorKey: 'sundayHoliday',
    header: 'Sunday / Holiday'
  }
]
</script>

<template>
  <UTable
    :data="rows"
    :columns="columns"
  >
    <template #weekday-cell="{ row }">
      <SettingsAllowanceCell
        v-if="row.original.weekday"
        :cell="row.original.weekday"
        @save="handleSave"
      />
    </template>

    <template #saturday-cell="{ row }">
      <SettingsAllowanceCell
        v-if="row.original.saturday"
        :cell="row.original.saturday"
        @save="handleSave"
      />
    </template>

    <template #sundayHoliday-cell="{ row }">
      <SettingsAllowanceCell
        v-if="row.original.sundayHoliday"
        :cell="row.original.sundayHoliday"
        @save="handleSave"
      />
    </template>
  </UTable>
</template>
