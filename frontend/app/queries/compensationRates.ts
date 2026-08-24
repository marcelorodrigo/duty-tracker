import { defineMutation, defineQueryOptions, useQueryCache } from '@pinia/colada'
import { QUERY_KEYS } from '~/queries/keys'
import type { CompensationRateResponse } from '~/types/compensation'
import { extractErrorDetail } from '~/utils/errors'
import { isLatestGeneration, nextGeneration } from '~/utils/mutationGuard'

export interface CompensationRateTableResponse {
  rates: CompensationRateResponse[]
}

export interface UpdateCompensationRateVars {
  id: number
  percentage: number
  label: string
}

export const compensationRatesQuery = defineQueryOptions({
  key: QUERY_KEYS.compensationRates.root(),
  query: () =>
    $fetch<CompensationRateTableResponse>('/api/v1/compensation-rates', {
      baseURL: useRuntimeConfig().public.apiBase,
      timeout: 10_000
    })
})

export const useUpdateCompensationRate = defineMutation(() => {
  const toast = useToast()
  const queryCache = useQueryCache()

  const key = QUERY_KEYS.compensationRates.root()

  return useMutation({
    onMutate: (vars: UpdateCompensationRateVars) => {
      const previous = queryCache.getQueryData<CompensationRateTableResponse>(key)
      if (previous) {
        const rates = previous.rates.map(r =>
          r.id === vars.id ? { ...r, percentage: vars.percentage } : r
        )
        queryCache.setQueryData(key, { rates })
      }
      return { previous: previous ?? null, generation: nextGeneration(key) }
    },
    mutation: (vars: UpdateCompensationRateVars) =>
      $fetch(`/api/v1/compensation-rates/${vars.id}`, {
        baseURL: useRuntimeConfig().public.apiBase,
        method: 'PUT',
        body: { rateId: vars.id, percentage: vars.percentage, label: vars.label }
      }),
    onError: (_err: unknown, _vars: UpdateCompensationRateVars, context) => {
      if (context.generation !== undefined && isLatestGeneration(key, context.generation) && context.previous) {
        queryCache.setQueryData(key, context.previous)
      }
      toast.add({
        title: 'Failed to save',
        description: extractErrorDetail(_err),
        color: 'error',
        icon: 'i-lucide-x'
      })
    },
    onSuccess: (_data: unknown, _vars: UpdateCompensationRateVars, context) => {
      if (context.generation !== undefined && isLatestGeneration(key, context.generation)) {
        queryCache.invalidateQueries({ key, exact: true })
      }
      toast.add({
        title: 'Saved',
        color: 'success',
        icon: 'i-lucide-check'
      })
    }
  })
})
