<template>
  <div class="space-y-6">
    <UAlert
      color="warning"
      icon="i-lucide-alert-triangle"
      title="WCA Placeholder Values"
      description="Compensation percentages are placeholders (0.0000). Update them from the WCA PDF (Jumbo Logistics WCA, version P7-2025) before recording any registrations."
    />

    <UCard variant="subtle" class="overflow-hidden">
      <UTable :data="filteredRates" :columns="columns" class="w-full">
        <template #percentage-cell="{ row }">
          <UInput
            type="number"
            step="0.01"
            min="0"
            v-model="editValues[row.original.id]"
            class="w-24"
            icon="i-lucide-percent"
            :ui="{ icon: { trailing: { wrapper: 'pe-2' } } }"
          />
        </template>
        <template #actions-cell="{ row }">
          <div class="flex items-center gap-2 justify-end">
            <UButton size="sm" color="neutral" variant="ghost" icon="i-lucide-save" @click="saveRate(row.original)">
              Save
            </UButton>
            <UButton
              v-if="row.original.rateCategory === 'OVERTIME_ALLOWANCE'"
              size="sm"
              color="error"
              variant="ghost"
              icon="i-lucide-trash-2"
              @click="removeRate(row.original.id)"
            />
          </div>
        </template>
      </UTable>
    </UCard>

    <div class="flex justify-between items-center">
      <UButton @click="showAddModal = true" color="neutral" variant="outline" icon="i-lucide-plus">
        Add Overtime Allowance
      </UButton>
    </div>

    <UModal v-model:open="showAddModal">
      <template #content>
        <UCard>
          <template #header>
            <h3 class="text-lg font-medium">Add Overtime Allowance</h3>
          </template>
          
          <UForm :state="newRate" @submit="addRate" class="space-y-4">
            <UFormField label="Label" name="label" required>
              <UInput v-model="newRate.label" placeholder="e.g. Weekday evening" icon="i-lucide-tag" class="w-full" />
            </UFormField>
            
            <div class="grid grid-cols-2 gap-4">
              <UFormField label="Time From" name="timeFrom">
                <UInput v-model="newRate.timeFrom" type="time" icon="i-lucide-clock" class="w-full" />
              </UFormField>
              <UFormField label="Time To" name="timeTo">
                <UInput v-model="newRate.timeTo" type="time" icon="i-lucide-clock" class="w-full" />
              </UFormField>
            </div>
            
            <UFormField label="Percentage" name="percentage" required>
              <UInput v-model="newRate.percentage" type="number" step="0.0001" min="0" placeholder="0.0000" icon="i-lucide-percent" class="w-full" />
            </UFormField>
            
            <div class="flex justify-end gap-3 mt-6 pt-4 border-t border-gray-200 dark:border-gray-800">
              <UButton color="neutral" variant="ghost" @click="showAddModal = false">Cancel</UButton>
              <UButton type="submit" color="primary" :disabled="!newRate.label || !newRate.percentage || isNaN(Number(newRate.percentage))">
                Add Allowance
              </UButton>
            </div>
          </UForm>
        </UCard>
      </template>
    </UModal>

    <div class="flex justify-end pt-4 border-t border-gray-200 dark:border-gray-800">
      <UButton @click="emit('saved')" trailing-icon="i-lucide-check" color="primary" size="lg">
        Finish Setup
      </UButton>
    </div>
  </div>
</template>

<script setup lang="ts">
const emit = defineEmits<{ saved: [] }>()
const compensationStore = useCompensationStore()
const profileStore = useProfileStore()
const showAddModal = ref(false)

const editValues = ref<Record<number, string>>({})

const columns = [
  { accessorKey: 'label', header: 'Label' },
  { accessorKey: 'rateCategory', header: 'Category' },
  { accessorKey: 'percentage', header: 'Percentage (%)' },
  { accessorKey: 'actions', header: '' },
]

const filteredRates = computed(() =>
  profileStore.profile
    ? compensationStore.rates.filter(r => r.employeeType === profileStore.profile!.employeeType)
    : compensationStore.rates
)

onMounted(async () => {
  await compensationStore.fetchRates(profileStore.profile?.employeeType)
})

watch(filteredRates, (rates) => {
  rates.forEach(r => { editValues.value[r.id] = r.percentage })
}, { immediate: true })

const newRate = reactive({ label: '', timeFrom: '', timeTo: '', percentage: '0.0000' })

async function saveRate(row: { id: number; label: string }) {
  const percentage = editValues.value[row.id]
  if (!percentage || isNaN(Number(percentage))) {
    console.error('Invalid percentage value')
    return
  }
  if (!row.label || row.label.trim() === '') {
    console.error('Label cannot be empty')
    return
  }
  await compensationStore.updateRate(row.id, { percentage: parseFloat(percentage), label: row.label })
}

async function removeRate(id: number) {
  await compensationStore.deleteRate(id)
}

async function addRate() {
  if (!newRate.label || newRate.label.trim() === '') {
    console.error('Label cannot be empty')
    return
  }
  if (!newRate.percentage || isNaN(Number(newRate.percentage))) {
    console.error('Percentage must be a valid number')
    return
  }
  if ((newRate.timeFrom && !newRate.timeTo) || (!newRate.timeFrom && newRate.timeTo)) {
    console.error('Time From and Time To must both be set or both be empty')
    return
  }
  await compensationStore.createRate({
    employeeType: profileStore.profile?.employeeType ?? 'INTERNAL',
    rateCategory: 'OVERTIME_ALLOWANCE',
    label: newRate.label,
    timeFrom: newRate.timeFrom || null,
    timeTo: newRate.timeTo || null,
    percentage: parseFloat(newRate.percentage),
  })
  showAddModal.value = false
  Object.assign(newRate, { label: '', timeFrom: '', timeTo: '', percentage: '0.0000' })
}
</script>
