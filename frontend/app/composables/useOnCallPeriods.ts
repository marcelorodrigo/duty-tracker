import type { OnCallPeriodResponse } from '~/types/onCallPeriod'
import { getPeriodStatus } from '~/utils/dates'

export function useOnCallPeriods() {
  const toast = useToast()
  const { $api } = useNuxtApp()

  const { data, pending, error, refresh } = useApiResource<OnCallPeriodResponse[]>(
    async () => {
      const response = await $api.get<{ periods: OnCallPeriodResponse[] }>('/oncall-periods')
      return response.periods
    },
    'Failed to fetch periods'
  )

  const deleteModalOpen = ref(false)
  const deletingPeriod = ref<OnCallPeriodResponse | null>(null)

  const activePeriods = computed(() => {
    return (data.value ?? [])
      .filter((p) => {
        const status = getPeriodStatus(p.startDateTime, p.endDateTime)
        return status === 'active' || status === 'scheduled'
      })
  })

  const pastPeriods = computed(() => {
    return (data.value ?? [])
      .filter((p) => {
        const status = getPeriodStatus(p.startDateTime, p.endDateTime)
        return status === 'past'
      })
  })

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
      await $api.delete(`/oncall-periods/${id}`)
      await refresh()
      closeDeleteModal()
      toast.add({
        title: 'On-call period deleted',
        color: 'success',
        icon: 'i-lucide-check'
      })
    } catch (err: unknown) {
      toast.add({
        title: 'Failed to delete period',
        description: getApiErrorMessage(err),
        color: 'error',
        icon: 'i-lucide-x'
      })
    }
  }

  return {
    data,
    pending,
    error,
    refresh,
    activePeriods,
    pastPeriods,
    deleteModalOpen,
    deletingPeriod,
    openDeleteModal,
    closeDeleteModal,
    remove
  }
}
