import type { OnCallPeriodResponse } from '~/types/onCallPeriod'

export function useOnCallPeriod(periodId: number) {
  const { $api } = useNuxtApp()

  return useApiResource<OnCallPeriodResponse>(
    () => $api.get(`/oncall-periods/${periodId}`),
    'Failed to load period'
  )
}
