import type { CompensationRateResponse, PivotRow } from '~/types/compensation'
import { buildPivotRows } from '~/utils/compensation'

interface CompensationRateTableResponse {
  rates: CompensationRateResponse[]
}

export function useCompensationRates() {
  const toast = useToast()

  const { data, pending, error } = useFetch<CompensationRateTableResponse>(
    apiPath('/compensation-rates'),
    {
      timeout: 10_000
    }
  )

  const pivotRows = computed<PivotRow[]>(() => {
    if (!data.value?.rates) return []
    return buildPivotRows(data.value.rates)
  })

  async function updateRate(id: number, percentage: number): Promise<void> {
    const rates = data.value?.rates
    if (!rates) return

    const target = rates.find(r => r.id === id)
    if (!target) return

    const original = target.percentage
    target.percentage = percentage
    triggerRef(data)

    try {
      await $fetch(apiPath(`/compensation-rates/${id}`), {
        method: 'PUT',
        body: { rateId: id, percentage, label: target.label }
      })
      toast.add({
        title: 'Saved',
        color: 'success',
        icon: 'i-lucide-check'
      })
    } catch (err: unknown) {
      target.percentage = original
      triggerRef(data)
      toast.add({
        title: 'Failed to save',
        description: extractErrorDetail(err),
        color: 'error',
        icon: 'i-lucide-x'
      })
    }
  }

  return { data, pivotRows, pending, error, updateRate }
}
