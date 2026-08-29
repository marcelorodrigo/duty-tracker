import { defineMutation, defineQueryOptions, useQueryCache } from '@pinia/colada'
import { QUERY_KEYS } from '~/queries/keys'
import type { CompensationRateResponse } from '~/types/compensation'
import type { PageResponse } from '~/types/page'
import { extractErrorDetail } from '~/utils/errors'
import { nextGeneration, settleGeneration } from '~/utils/mutationGuard'

export type CompensationRateTableResponse = PageResponse<CompensationRateResponse>

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
      params: { page: 0, size: 100 },
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
        const rates = previous.content.map(r =>
          r.id === vars.id ? { ...r, percentage: vars.percentage } : r
        )
        queryCache.setQueryData(key, { ...previous, content: rates })
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
      if (context.generation !== undefined) {
        const isFinal = settleGeneration(key, context.generation)
        if (isFinal) {
          queryCache.invalidateQueries({ key, exact: true })
        } else if (context.previous) {
          queryCache.setQueryData(key, context.previous)
        }
      }
      toast.add({
        title: 'Failed to save',
        description: extractErrorDetail(_err),
        color: 'error',
        icon: 'i-lucide-x'
      })
    },
    onSuccess: (_data: unknown, _vars: UpdateCompensationRateVars, context) => {
      if (context.generation !== undefined) {
        settleGeneration(key, context.generation)
      }
      queryCache.invalidateQueries({ key, exact: true })
      toast.add({
        title: 'Saved',
        color: 'success',
        icon: 'i-lucide-check'
      })
    }
  })
})
