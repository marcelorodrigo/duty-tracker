import { computed, ref, watch } from 'vue'
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

  // When the list begins with only active/scheduled periods there is no past
  // period to render, so keep loading pages until a past period appears or the
  // list is exhausted. This runs client-side; the server returns everything
  // sorted by startDateTime descending.
  //
  // We react on `pending` flipping back to false (a page finished loading)
  // rather than on the item count changing, because the count updates while a
  // request is still in flight. Triggering then would call `loadNext` while it
  // is already pending and the call would be ignored. We also stop on error so
  // a failed page does not retry forever.
  watch(
    () => [pending.value, hasMore.value, pastPeriods.value.length] as const,
    async ([isPending, more, pastCount]) => {
      if (!isPending && !error.value && more && pastCount === 0) {
        await loadNext()
      }
    }
  )

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
