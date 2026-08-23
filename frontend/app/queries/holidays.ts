import { defineMutation, defineQueryOptions, useQueryCache } from '@pinia/colada'
import { QUERY_KEYS } from '~/queries/keys'
import type { HolidayResponse } from '~/types/holiday'
import { extractErrorDetail } from '~/utils/errors'

export const holidaysQuery = (periodId: number) =>
  defineQueryOptions({
    key: QUERY_KEYS.holidays.byPeriod(periodId),
    query: () =>
      $fetch<HolidayResponse[]>(`/api/v1/oncall-periods/${periodId}/holidays`, {
        baseURL: useRuntimeConfig().public.apiBase
      })
  })

export interface SaveHolidaysVars {
  periodId: number
  holidays: HolidayResponse[]
}

export const useSaveHolidays = defineMutation(() => {
  const toast = useToast()
  const queryCache = useQueryCache()

  return useMutation({
    onMutate: (vars: SaveHolidaysVars) => {
      const key = QUERY_KEYS.holidays.byPeriod(vars.periodId)
      const previous = queryCache.getQueryData<HolidayResponse[]>(key)
      queryCache.setQueryData(key, vars.holidays)
      return { previous: previous ?? null, periodId: vars.periodId }
    },
    mutation: (vars: SaveHolidaysVars) =>
      $fetch<HolidayResponse[]>(`/api/v1/oncall-periods/${vars.periodId}/holidays`, {
        baseURL: useRuntimeConfig().public.apiBase,
        method: 'PUT',
        body: vars.holidays
      }),
    onError: (_err: unknown, _vars: SaveHolidaysVars, context) => {
      if (context.previous) {
        queryCache.setQueryData(QUERY_KEYS.holidays.byPeriod(context.periodId), context.previous)
      }
      toast.add({
        title: 'Failed to save holidays',
        description: extractErrorDetail(_err),
        color: 'error',
        icon: 'i-lucide-x'
      })
    },
    onSuccess: (saved: HolidayResponse[], _vars: SaveHolidaysVars, context) => {
      queryCache.setQueryData(QUERY_KEYS.holidays.byPeriod(context.periodId), saved)
      toast.add({
        title: 'Holidays saved',
        color: 'success',
        icon: 'i-lucide-check'
      })
    }
  })
})
