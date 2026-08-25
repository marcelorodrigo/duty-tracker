import { defineMutation, defineQueryOptions, useQueryCache } from '@pinia/colada'
import { QUERY_KEYS } from '~/queries/keys'
import type { OnCallPeriodResponse } from '~/types/onCallPeriod'
import { extractErrorDetail } from '~/utils/errors'

export const onCallPeriodsListQuery = defineQueryOptions({
  key: QUERY_KEYS.onCallPeriods.list(),
  query: () =>
    $fetch<{ periods: OnCallPeriodResponse[] }>('/api/v1/oncall-periods', {
      baseURL: useRuntimeConfig().public.apiBase
    })
})

export const useDeleteOnCallPeriod = defineMutation(() => {
  const toast = useToast()
  const queryCache = useQueryCache()

  return useMutation({
    mutation: (id: number) =>
      $fetch(`/api/v1/oncall-periods/${id}`, {
        baseURL: useRuntimeConfig().public.apiBase,
        method: 'DELETE'
      }),
    onSuccess: () => {
      toast.add({
        title: 'On-call period deleted',
        color: 'success',
        icon: 'i-lucide-check'
      })
      queryCache.invalidateQueries({
        key: QUERY_KEYS.onCallPeriods.list(),
        exact: true
      })
    },
    onError: (err: unknown) => {
      toast.add({
        title: 'Failed to delete period',
        description: extractErrorDetail(err),
        color: 'error',
        icon: 'i-lucide-x'
      })
    }
  })
})

export interface CreateOnCallPeriodVars {
  startDateTime: string
  endDateTime: string
}

export const useCreateOnCallPeriod = defineMutation(() => {
  const queryCache = useQueryCache()

  return useMutation({
    mutation: (vars: CreateOnCallPeriodVars) =>
      $fetch<{ id: number }>('/api/v1/oncall-periods', {
        baseURL: useRuntimeConfig().public.apiBase,
        method: 'POST',
        body: { startDateTime: vars.startDateTime, endDateTime: vars.endDateTime }
      }),
    onSuccess: () => {
      queryCache.invalidateQueries({
        key: QUERY_KEYS.onCallPeriods.list(),
        exact: true
      })
    }
  })
})

export interface UpdateOnCallPeriodVars {
  id: number
  startDateTime: string
  endDateTime: string
}

export const useUpdateOnCallPeriod = defineMutation(() => {
  const queryCache = useQueryCache()

  return useMutation({
    mutation: (vars: UpdateOnCallPeriodVars) =>
      $fetch(`/api/v1/oncall-periods/${vars.id}`, {
        baseURL: useRuntimeConfig().public.apiBase,
        method: 'PUT',
        body: { startDateTime: vars.startDateTime, endDateTime: vars.endDateTime }
      }),
    onSuccess: () => {
      queryCache.invalidateQueries({
        key: QUERY_KEYS.onCallPeriods.list(),
        exact: true
      })
    }
  })
})
