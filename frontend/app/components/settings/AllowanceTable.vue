<script setup lang="ts">
import type { TableColumn } from '@nuxt/ui'
import type { PivotRow } from '~/types/compensation'

const props = defineProps<{
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
    header: 'Mon–Fri'
  },
  {
    accessorKey: 'saturday',
    header: 'Saturday'
  },
  {
    accessorKey: 'sundayHoliday',
    header: 'Sun / PH'
  }
]
</script>

<template>
  <UTable :data="rows" :columns="columns">
    <template #weekday-cell="{ row }">
      <SettingsAllowanceCell
        :cell="row.original.weekday"
        :on-save="onSave"
      />
    </template>

    <template #saturday-cell="{ row }">
      <SettingsAllowanceCell
        :cell="row.original.saturday"
        :on-save="onSave"
      />
    </template>

    <template #sundayHoliday-cell="{ row }">
      <SettingsAllowanceCell
        :cell="row.original.sundayHoliday"
        :on-save="onSave"
      />
    </template>
  </UTable>
</template>
