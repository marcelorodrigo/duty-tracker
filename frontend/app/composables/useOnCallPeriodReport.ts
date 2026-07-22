import type { OnCallPeriodReportResponse } from '~/types/report'

export function useOnCallPeriodReport(periodId: number) {
  const { $api } = useNuxtApp()

  return useApiResource<OnCallPeriodReportResponse>(
    () => $api.get(`/oncall-periods/${periodId}/report`),
    'Failed to generate report'
  )
}
