import { computed, ref } from 'vue'
import { useQuery } from '@pinia/colada'
import type { OnCallPeriodResponse } from '~/types/onCallPeriod'
import { getPeriodStatus } from '~/utils/dates'
import { onCallPeriodsListQuery, useDeleteOnCallPeriod } from '~/queries/onCallPeriods'

export function useOnCallPeriods() {
  const {
    state: periodsState,
    data: periodsData,
    asyncStatus
  } = useQuery(() => ({ ...onCallPeriodsListQuery }))

  const periods = computed<OnCallPeriodResponse[]>(() => periodsData.value?.periods ?? [])

  const pending = computed(() => asyncStatus.value === 'loading')
  const error = computed(() => periodsState.value.error as Error | null)

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
      closeDeleteModal()
    } catch {
      // error toast is shown by the mutation's onError handler
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
    openDeleteModal,
    closeDeleteModal,
    remove
  }
}
