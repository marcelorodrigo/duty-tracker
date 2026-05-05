import type { OnCallPeriodResponse } from '~/types/onCallPeriod'
import { getPeriodStatus } from '~/utils/dates'

export function useOnCallPeriods() {
  const config = useRuntimeConfig()
  const toast = useToast()

  const periods = ref<OnCallPeriodResponse[]>([])
  const pending = ref(false)
  const error = ref<Error | null>(null)

  const deleteModalOpen = ref(false)
  const deletingPeriod = ref<OnCallPeriodResponse | null>(null)

  const activePeriods = computed(() => {
    return periods.value
      .filter((p) => {
        const status = getPeriodStatus(p.startDateTime, p.endDateTime)
        return status === 'active' || status === 'scheduled'
      })
  })

  const pastPeriods = computed(() => {
    return periods.value
      .filter((p) => {
        const status = getPeriodStatus(p.startDateTime, p.endDateTime)
        return status === 'past'
      })
  })

  async function fetchPeriods(): Promise<void> {
    pending.value = true
    error.value = null
    try {
      const response = await $fetch<{ periods: OnCallPeriodResponse[] }>('/api/v1/oncall-periods', {
        baseURL: config.public.apiBase
      })
      periods.value = response.periods
    } catch (err) {
      error.value = err instanceof Error ? err : new Error('Failed to fetch periods')
    } finally {
      pending.value = false
    }
  }

  function openDeleteModal(period: OnCallPeriodResponse): void {
    deletingPeriod.value = period
    deleteModalOpen.value = true
  }

  function closeDeleteModal(): void {
    deleteModalOpen.value = false
    deletingPeriod.value = null
  }

  async function remove(id: number): Promise<void> {
    try {
      await $fetch(`/api/v1/oncall-periods/${id}`, {
        baseURL: config.public.apiBase,
        method: 'DELETE'
      })
      await fetchPeriods()
      closeDeleteModal()
      toast.add({
        title: 'On-call period deleted',
        color: 'success',
        icon: 'i-lucide-check'
      })
    } catch (err: unknown) {
      toast.add({
        title: 'Failed to delete period',
        description: extractErrorDetail(err),
        color: 'error',
        icon: 'i-lucide-x'
      })
    }
  }

  return {
    periods,
    pending,
    error,
    activePeriods,
    pastPeriods,
    deleteModalOpen,
    deletingPeriod,
    fetchPeriods,
    openDeleteModal,
    closeDeleteModal,
    remove
  }
}
