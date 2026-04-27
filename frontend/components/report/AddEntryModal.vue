<template>
  <UModal :open="open" @update:open="emit('update:open', $event)" title="Add Entry">
    <template #body>
      <div class="p-4 space-y-3">
        <template v-if="type === 'oncall'">
          <UFormField label="Date"><UInput type="date" v-model="form.date" /></UFormField>
          <UFormField label="Hours"><UInput type="number" step="0.25" v-model="form.hours" /></UFormField>
          <UFormField label="Rate Type">
            <USelect v-model="form.rateType" :items="rateTypeOptions" value-attribute="value" label-attribute="label" />
          </UFormField>
        </template>
        <template v-else>
          <UFormField label="Incident ID"><UInput type="number" v-model="form.incidentId" /></UFormField>
          <UFormField label="Overtime Hours"><UInput type="number" step="0.25" v-model="form.overtimeHours" /></UFormField>
          <UFormField label="Allowance Hours"><UInput type="number" step="0.25" v-model="form.allowanceHours" /></UFormField>
          <UFormField label="Allowance %"><UInput type="number" step="0.01" v-model="form.allowancePercentage" /></UFormField>
          <UFormField label="Time From"><UInput type="time" v-model="form.timeFrom" /></UFormField>
          <UFormField label="Time To"><UInput type="time" v-model="form.timeTo" /></UFormField>
          <label class="flex items-center gap-2">
            <input type="checkbox" v-model="form.isAllowanceEntry" />
            Allowance Entry
          </label>
        </template>
        <div class="flex gap-2 justify-end">
          <UButton variant="outline" @click="emit('update:open', false)">Cancel</UButton>
          <UButton @click="onAdd" :loading="adding">Add</UButton>
        </div>
      </div>
    </template>
  </UModal>
</template>

<script setup lang="ts">
const props = defineProps<{ open: boolean; type: 'oncall' | 'overtime'; summaryId: number }>()
const emit = defineEmits<{ 'update:open': [boolean]; added: [] }>()

const reportStore = useReportStore()
const adding = ref(false)

const rateTypeOptions = [
  { value: 'WEEKDAY_SATURDAY', label: 'Weekday / Saturday' },
  { value: 'SUNDAY_HOLIDAY', label: 'Sunday / Holiday' },
]

const form = reactive({
  date: '', hours: '', rateType: 'WEEKDAY_SATURDAY',
  incidentId: null as number | null, overtimeHours: '', allowanceHours: '', allowancePercentage: '',
  timeFrom: '', timeTo: '', isAllowanceEntry: false,
})

async function onAdd() {
  adding.value = true
  try {
    if (props.type === 'oncall') {
      await reportStore.addOnCallEntry(props.summaryId, { date: form.date, hours: form.hours, rateType: form.rateType })
    } else {
      // Validate incidentId is provided and not 0
      if (!form.incidentId || form.incidentId === 0) {
        console.error('Incident ID is required')
        adding.value = false
        return
      }
      await reportStore.addOvertimeEntry(props.summaryId, {
        incidentId: form.incidentId, overtimeHours: form.overtimeHours,
        allowanceHours: form.allowanceHours, allowancePercentage: form.allowancePercentage,
        timeFrom: form.timeFrom, timeTo: form.timeTo, isAllowanceEntry: form.isAllowanceEntry,
      })
    }
    adding.value = false
    emit('added')
    emit('update:open', false)
  } catch (error) {
    adding.value = false
    throw error
  }
}
</script>
