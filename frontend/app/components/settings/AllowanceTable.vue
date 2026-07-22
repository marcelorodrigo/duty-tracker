<script setup lang="ts">
import type { TableColumn } from '@nuxt/ui'
import type { AllowanceSavePayload, AllowanceSaveState, PivotRow } from '~/types/compensation'

const props = defineProps<{
  rows: PivotRow[]
  saveStates: Readonly<Record<number, AllowanceSaveState>>
}>()

const emit = defineEmits<{
  save: [payload: AllowanceSavePayload]
}>()

function handleSave(payload: AllowanceSavePayload) {
  emit('save', payload)
}

function getSaveState(id: number): AllowanceSaveState {
  return props.saveStates[id] ?? { status: 'idle' }
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
        :save-state="getSaveState(row.original.weekday.id)"
        @save="handleSave"
      />
    </template>

    <template #saturday-cell="{ row }">
      <SettingsAllowanceCell
        v-if="row.original.saturday"
        :cell="row.original.saturday"
        :save-state="getSaveState(row.original.saturday.id)"
        @save="handleSave"
      />
    </template>

    <template #sundayHoliday-cell="{ row }">
      <SettingsAllowanceCell
        v-if="row.original.sundayHoliday"
        :cell="row.original.sundayHoliday"
        :save-state="getSaveState(row.original.sundayHoliday.id)"
        @save="handleSave"
      />
    </template>
  </UTable>
</template>
