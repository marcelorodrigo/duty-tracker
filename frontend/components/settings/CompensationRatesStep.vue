<template>
  <div class="space-y-6">
    <div v-if="loading" class="flex justify-center py-12">
      <UIcon name="i-lucide-loader-2" class="animate-spin text-gray-400 size-6" />
    </div>

    <template v-else>
      <BaseRatesCard
        :rates="baseRates"
        :on-save="handleUpdate"
      />

      <AllowanceRatesGrid
        :rows="gridRows"
        :on-save="handleUpdate"
      />
    </template>
  </div>
</template>

<script setup lang="ts">
import type { GridRow } from '~/components/settings/AllowanceRatesGrid.vue'

const compensationStore = useCompensationStore()
const profileStore = useProfileStore()
const toast = useToast()

const loading = ref(true)

onMounted(async () => {
  try {
    await compensationStore.fetchRates(profileStore.profile?.employeeType)
  } finally {
    loading.value = false
  }
})

const baseRates = computed(() =>
  compensationStore.rates.filter(r => r.rateCategory !== 'OVERTIME_ALLOWANCE')
)

const gridRows = computed<GridRow[]>(() => {
  const allowance = compensationStore.rates
    .filter(r => r.rateCategory === 'OVERTIME_ALLOWANCE')
    .sort((a, b) => (a.timeFrom ?? '').localeCompare(b.timeFrom ?? ''))

  const slots = [...new Set(allowance.map(r => r.timeFrom))]

  return slots
    .map(timeFrom => {
      const slot = allowance.filter(r => r.timeFrom === timeFrom)
      const weekday = slot.find(r => r.overtimeDayType === 'WEEKDAY')
      const saturday = slot.find(r => r.overtimeDayType === 'SATURDAY')
      const sundayHoliday = slot.find(r => r.overtimeDayType === 'SUNDAY_HOLIDAY')
      if (!weekday || !saturday || !sundayHoliday) return null
      return {
        timeFrom: timeFrom ?? '',
        timeTo: slot[0]?.timeTo ?? '',
        weekday,
        saturday,
        sundayHoliday,
      }
    })
    .filter((row): row is GridRow => row !== null)
})

async function handleUpdate(id: number, percentage: string): Promise<void> {
  const rate = compensationStore.rates.find(r => r.id === id)
  if (!rate) return
  try {
    await compensationStore.updateRate(id, { percentage, label: rate.label })
    toast.add({ title: 'Rate saved', color: 'success' })
  } catch (e: unknown) {
    const problemDetail = (e as { data?: { detail?: string; title?: string } }).data
    const message = problemDetail?.detail || problemDetail?.title || (e instanceof Error ? e.message : 'Failed to save rate.')
    toast.add({ title: 'Failed to save rate', description: message, color: 'error' })
  }
}
</script>
