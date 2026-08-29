import { computed } from 'vue'
import { useQuery } from '@pinia/colada'
import type { PivotRow } from '~/types/compensation'
import { buildPivotRows } from '~/utils/compensation'
import { compensationRatesQuery, useUpdateCompensationRate } from '~/queries/compensationRates'

export function useCompensationRates() {
  const {
    state: ratesState,
    data,
    asyncStatus
  } = useQuery(() => ({ ...compensationRatesQuery }))

  const pivotRows = computed<PivotRow[]>(() => {
    if (!data.value?.content) return []
    return buildPivotRows(data.value.content)
  })

  const pending = computed(() => asyncStatus.value === 'loading')
  const error = computed(() => (ratesState.value.error as Error | null) ?? null)

  const { mutateAsync } = useUpdateCompensationRate()

  async function updateRate(id: number, percentage: number): Promise<void> {
    const rates = data.value?.content
    if (!rates) return

    const target = rates.find(r => r.id === id)
    if (!target) return

    try {
      await mutateAsync({ id, percentage, label: target.label })
    } catch {
      // error toast is shown by the mutation's onError handler
    }
  }

  return { data, pivotRows, pending, error, updateRate }
}
