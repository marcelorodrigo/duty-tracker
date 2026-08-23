import { defineQueryOptions } from '@pinia/colada'
import { QUERY_KEYS } from '~/queries/keys'
import type { EarningsResponse } from '~/types/earnings'

export const earningsQuery = (periodId: number) =>
  defineQueryOptions({
    key: QUERY_KEYS.earnings.byPeriod(periodId),
    query: () =>
      $fetch<EarningsResponse>(`/api/v1/oncall-periods/${periodId}/earnings`, {
        baseURL: useRuntimeConfig().public.apiBase
      })
  })
