import { defineMutation, useQueryCache } from '@pinia/colada'
import { QUERY_KEYS } from '~/queries/keys'
import type { IncidentResponse, CreateIncidentRequest, UpdateIncidentRequest } from '~/types/incident'
import type { PageResponse } from '~/types/page'
import { extractErrorDetail } from '~/utils/errors'

export function fetchIncidentsPage(
  onCallPeriodId: number,
  page: number,
  size: number
): Promise<PageResponse<IncidentResponse>> {
  return $fetch<PageResponse<IncidentResponse>>('/api/v1/incidents', {
    baseURL: useRuntimeConfig().public.apiBase,
    params: { onCallPeriodId, page, size }
  })
}

export const useCreateIncident = defineMutation(() => {
  const toast = useToast()
  const queryCache = useQueryCache()

  return useMutation({
    mutation: (request: CreateIncidentRequest) =>
      $fetch<IncidentResponse>('/api/v1/incidents', {
        baseURL: useRuntimeConfig().public.apiBase,
        method: 'POST',
        body: request
      }),
    onSuccess: (_data, request) => {
      toast.add({
        title: 'Incident logged',
        color: 'success',
        icon: 'i-lucide-check'
      })
      queryCache.invalidateQueries({
        key: QUERY_KEYS.incidents.byPeriod(request.onCallPeriodId),
        exact: true
      })
    },
    onError: (err: unknown) => {
      toast.add({
        title: 'Failed to log incident',
        description: extractErrorDetail(err),
        color: 'error',
        icon: 'i-lucide-x'
      })
    }
  })
})

export interface UpdateIncidentVars {
  id: number
  request: UpdateIncidentRequest
  onCallPeriodId: number
}

export const useUpdateIncident = defineMutation(() => {
  const toast = useToast()
  const queryCache = useQueryCache()

  return useMutation({
    mutation: (vars: UpdateIncidentVars) =>
      $fetch<IncidentResponse>(`/api/v1/incidents/${vars.id}`, {
        baseURL: useRuntimeConfig().public.apiBase,
        method: 'PUT',
        body: vars.request
      }),
    onSuccess: (_data, vars) => {
      toast.add({
        title: 'Incident updated',
        color: 'success',
        icon: 'i-lucide-check'
      })
      queryCache.invalidateQueries({
        key: QUERY_KEYS.incidents.byPeriod(vars.onCallPeriodId),
        exact: true
      })
    },
    onError: (err: unknown) => {
      toast.add({
        title: 'Failed to update incident',
        description: extractErrorDetail(err),
        color: 'error',
        icon: 'i-lucide-x'
      })
    }
  })
})

export interface DeleteIncidentVars {
  id: number
  onCallPeriodId: number
}

export const useDeleteIncident = defineMutation(() => {
  const toast = useToast()
  const queryCache = useQueryCache()

  return useMutation({
    mutation: (vars: DeleteIncidentVars) =>
      $fetch(`/api/v1/incidents/${vars.id}`, {
        baseURL: useRuntimeConfig().public.apiBase,
        method: 'DELETE'
      }),
    onSuccess: (_data, vars) => {
      toast.add({
        title: 'Incident deleted',
        color: 'success',
        icon: 'i-lucide-check'
      })
      queryCache.invalidateQueries({
        key: QUERY_KEYS.incidents.byPeriod(vars.onCallPeriodId),
        exact: true
      })
    },
    onError: (err: unknown) => {
      toast.add({
        title: 'Failed to delete incident',
        description: extractErrorDetail(err),
        color: 'error',
        icon: 'i-lucide-x'
      })
    }
  })
})
