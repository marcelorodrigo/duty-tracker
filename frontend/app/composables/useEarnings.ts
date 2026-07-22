import type { EarningsResponse } from '~/types/earnings'

export function useEarnings(periodId: number) {
  const { $api } = useNuxtApp()

  return useApiResource<EarningsResponse>(
    () => $api.get(`/oncall-periods/${periodId}/earnings`),
    'Failed to load earnings'
  )
}
