<script setup lang="ts">
import type { TableColumn } from '@nuxt/ui'
import type { PivotRow } from '~/types/compensation'

defineProps<{
  rows: PivotRow[]
  onSave: (id: number, percentage: number) => Promise<void>
}>()

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
        :on-save="onSave"
      />
    </template>

    <template #saturday-cell="{ row }">
      <SettingsAllowanceCell
        v-if="row.original.saturday"
        :cell="row.original.saturday"
        :on-save="onSave"
      />
    </template>

    <template #sundayHoliday-cell="{ row }">
      <SettingsAllowanceCell
        v-if="row.original.sundayHoliday"
        :cell="row.original.sundayHoliday"
        :on-save="onSave"
      />
    </template>
  </UTable>
</template>
