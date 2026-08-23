import { defineQueryOptions } from '@pinia/colada'
import { QUERY_KEYS } from '~/queries/keys'
import type { OnCallPeriodReportResponse } from '~/types/report'

export const onCallPeriodReportQuery = (periodId: number) =>
  defineQueryOptions({
    key: QUERY_KEYS.onCallPeriodReport.byPeriod(periodId),
    query: () =>
      $fetch<OnCallPeriodReportResponse>(`/api/v1/oncall-periods/${periodId}/report`, {
        baseURL: useRuntimeConfig().public.apiBase
      })
  })
