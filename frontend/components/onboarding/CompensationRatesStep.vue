<template>
  <div class="space-y-4">
    <UAlert color="warning" title="WCA Placeholder Values"
      description="Compensation percentages are placeholders (0.0000). Update them from the WCA PDF (Jumbo Logistics WCA, version P7-2025) before recording any registrations." />
    <UTable :data="filteredRates" :columns="columns" />
    <UButton @click="showAddModal = true">Add Overtime Allowance Row</UButton>
    <UModal v-model:open="showAddModal" title="Add Overtime Allowance">
      <template #body>
        <div class="space-y-2 p-4">
          <UInput v-model="newRate.label" placeholder="Label (e.g. Weekday evening)" />
          <UInput v-model="newRate.timeFrom" type="time" placeholder="Time From" />
          <UInput v-model="newRate.timeTo" type="time" placeholder="Time To" />
           <UInput v-model="newRate.percentage" type="number" step="0.0001" min="0" placeholder="0.0000" />
           <UButton @click="addRate" :disabled="!newRate.label || !newRate.percentage || isNaN(Number(newRate.percentage))">Add</UButton>
        </div>
      </template>
    </UModal>
    <UButton @click="emit('saved')" class="mt-4">Finish</UButton>
  </div>
</template>

<script setup lang="ts">
import { h } from 'vue'

const emit = defineEmits<{ saved: [] }>()
const compensationStore = useCompensationStore()
const profileStore = useProfileStore()
const showAddModal = ref(false)

const editValues = ref<Record<number, string>>({})

const columns = [
  { accessorKey: 'label', header: 'Label' },
  { accessorKey: 'rateCategory', header: 'Category' },
  {
    accessorKey: 'percentage',
    header: 'Percentage (%)',
    class: 'w-24',
    render(row: any) {
      return h('input', {
        type: 'number',
        step: '0.01',
        min: '0',
        class: 'w-24 px-2 py-1 border rounded',
        modelValue: editValues.value[row.original.id],
        'onUpdate:modelValue': (val: string) => {
          editValues.value[row.original.id] = val
        },
      })
    },
  },
  {
    accessorKey: 'actions',
    header: '',
    render(row: any) {
      return h('div', { class: 'flex gap-1' }, [
        h(UButton, {
          size: 'sm',
          onClick: () => saveRate(row.original),
        }, () => 'Save'),
        row.original.rateCategory === 'OVERTIME_ALLOWANCE'
          ? h(UButton, {
              size: 'sm',
              color: 'error',
              onClick: () => removeRate(row.original.id),
            }, () => 'Delete')
          : null,
      ])
    },
  },
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
