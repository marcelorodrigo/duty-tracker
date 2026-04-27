<template>
  <UModal :open="open" @update:open="emit('update:open', $event)" title="Edit Entry">
    <template #body>
      <div class="p-4 space-y-3" v-if="entry">
        <template v-if="type === 'oncall'">
          <UFormField label="Hours">
            <UInput type="number" step="0.25" v-model="form.hours" />
          </UFormField>
          <UFormField label="Rate Type">
            <USelect v-model="form.rateType" :items="rateTypeOptions" value-attribute="value" label-attribute="label" />
          </UFormField>
        </template>
        <template v-else>
          <UFormField label="Overtime Hours">
            <UInput type="number" step="0.25" v-model="form.overtimeHours" />
          </UFormField>
          <UFormField label="Allowance Hours">
            <UInput type="number" step="0.25" v-model="form.allowanceHours" />
          </UFormField>
          <UFormField label="Allowance %">
            <UInput type="number" step="0.01" v-model="form.allowancePercentage" />
          </UFormField>
        </template>
        <div class="flex gap-2 justify-end mt-4">
          <UButton variant="outline" @click="emit('update:open', false)">Cancel</UButton>
          <UButton @click="onSave" :loading="saving">Save</UButton>
        </div>
      </div>
    </template>
  </UModal>
</template>

<script setup lang="ts">
const props = defineProps<{ open: boolean; entry: unknown; type: 'oncall' | 'overtime' }>()
const emit = defineEmits<{ 'update:open': [boolean]; saved: [] }>()

const reportStore = useReportStore()
const route = useRoute()
const summaryId = Number(route.params.id)
const saving = ref(false)

const rateTypeOptions = [
  { value: 'WEEKDAY_SATURDAY', label: 'Weekday / Saturday' },
  { value: 'SUNDAY_HOLIDAY', label: 'Sunday / Holiday' },
]

const form = reactive({
  hours: '',
  rateType: 'WEEKDAY_SATURDAY',
  overtimeHours: '',
  allowanceHours: '',
  allowancePercentage: '',
})

watch(() => props.entry, (e) => {
  if (!e) return
  const entry = e as Record<string, unknown>
  if (props.type === 'oncall') {
    form.hours = String(entry.hours ?? '')
    form.rateType = String(entry.rateType ?? 'WEEKDAY_SATURDAY')
  } else {
    form.overtimeHours = String(entry.overtimeHours ?? '')
    form.allowanceHours = String(entry.allowanceHours ?? '')
    form.allowancePercentage = String(entry.allowancePercentage ?? '')
  }
}, { immediate: true })

async function onSave() {
  saving.value = true
  const entry = props.entry as Record<string, unknown>
  if (props.type === 'oncall') {
    await reportStore.overrideOnCallEntry(summaryId, entry.id as number, { hours: form.hours, rateType: form.rateType })
  } else {
    await reportStore.overrideOvertimeEntry(summaryId, entry.id as number, {
      overtimeHours: form.overtimeHours,
      allowanceHours: form.allowanceHours,
      allowancePercentage: form.allowancePercentage,
    })
  }
  saving.value = false
  emit('saved')
  emit('update:open', false)
}
</script>
