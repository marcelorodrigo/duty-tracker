import { defineMutation } from '@pinia/colada'
import type { OnCallPeriodResponse } from '~/types/onCallPeriod'
import type { PageResponse } from '~/types/page'
import { extractErrorDetail } from '~/utils/errors'

export function fetchOnCallPeriodsPage(page: number, size: number): Promise<PageResponse<OnCallPeriodResponse>> {
  return $fetch<PageResponse<OnCallPeriodResponse>>('/api/v1/oncall-periods', {
    baseURL: useRuntimeConfig().public.apiBase,
    params: { page, size }
  })
}

export const useDeleteOnCallPeriod = defineMutation(() => {
  const toast = useToast()

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
  return useMutation({
    mutation: (vars: CreateOnCallPeriodVars) =>
      $fetch<{ id: number }>('/api/v1/oncall-periods', {
        baseURL: useRuntimeConfig().public.apiBase,
        method: 'POST',
        body: { startDateTime: vars.startDateTime, endDateTime: vars.endDateTime }
      })
  })
})

export interface UpdateOnCallPeriodVars {
  id: number
  startDateTime: string
  endDateTime: string
}

export const useUpdateOnCallPeriod = defineMutation(() => {
  return useMutation({
    mutation: (vars: UpdateOnCallPeriodVars) =>
      $fetch(`/api/v1/oncall-periods/${vars.id}`, {
        baseURL: useRuntimeConfig().public.apiBase,
        method: 'PUT',
        body: { startDateTime: vars.startDateTime, endDateTime: vars.endDateTime }
      })
  })
})
