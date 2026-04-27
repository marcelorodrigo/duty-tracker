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
          <UInput v-model="newRate.percentage" type="number" step="0.01" min="0" placeholder="Percentage" />
          <UButton @click="addRate">Add</UButton>
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
  { key: 'label', label: 'Label' },
  { key: 'rateCategory', label: 'Category' },
  {
    key: 'percentage',
    label: 'Percentage (%)',
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
    key: 'actions',
    label: '',
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
  filteredRates.forEach(r => { editValues.value[r.id] = r.percentage })
})

const newRate = reactive({ label: '', timeFrom: '', timeTo: '', percentage: '0.00' })

async function saveRate(row: { id: number; label: string }) {
  await compensationStore.updateRate(row.id, { percentage: editValues.value[row.id], label: row.label })
}

async function removeRate(id: number) {
  await compensationStore.deleteRate(id)
}

async function addRate() {
  await compensationStore.createRate({
    employeeType: profileStore.profile?.employeeType ?? 'INTERNAL',
    rateCategory: 'OVERTIME_ALLOWANCE',
    label: newRate.label,
    timeFrom: newRate.timeFrom || null,
    timeTo: newRate.timeTo || null,
    percentage: newRate.percentage,
  })
  showAddModal.value = false
  Object.assign(newRate, { label: '', timeFrom: '', timeTo: '', percentage: '0.00' })
}
</script>
