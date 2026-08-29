import { computed, ref } from 'vue'
import type { OnCallPeriodResponse } from '~/types/onCallPeriod'
import { getPeriodStatus } from '~/utils/dates'
import { fetchOnCallPeriodsPage, useDeleteOnCallPeriod } from '~/queries/onCallPeriods'
import { useInfiniteList } from '~/composables/useInfiniteList'

export function useOnCallPeriods() {
  const {
    items: periods,
    pending,
    error,
    hasMore,
    loadNext,
    reset,
    sentinelRef
  } = useInfiniteList<OnCallPeriodResponse>({
    fetchPage: (page, size) => fetchOnCallPeriodsPage(page, size)
  })

  const activePeriods = computed<OnCallPeriodResponse[]>(() =>
    periods.value.filter((p) => {
      const status = getPeriodStatus(p.startDateTime, p.endDateTime)
      return status === 'active' || status === 'scheduled'
    })
  )

  const pastPeriods = computed<OnCallPeriodResponse[]>(() =>
    periods.value.filter((p) => {
      const status = getPeriodStatus(p.startDateTime, p.endDateTime)
      return status === 'past'
    })
  )

  const deleteModalOpen = ref(false)
  const deletingPeriod = ref<OnCallPeriodResponse | null>(null)

  function openDeleteModal(period: OnCallPeriodResponse): void {
    deletingPeriod.value = period
    deleteModalOpen.value = true
  }

  function closeDeleteModal(): void {
    deleteModalOpen.value = false
    deletingPeriod.value = null
  }

  const { mutateAsync } = useDeleteOnCallPeriod()

  async function remove(id: number): Promise<void> {
    try {
      await mutateAsync(id)
      reset()
      await loadNext()
      closeDeleteModal()
    } catch {
      // error toast is shown by the mutation's onError handler
    }
  }

  return {
    periods,
    pending,
    error,
    hasMore,
    loadNext,
    reset,
    sentinelRef,
    activePeriods,
    pastPeriods,
    deleteModalOpen,
    deletingPeriod,
    openDeleteModal,
    closeDeleteModal,
    remove
  }
}
