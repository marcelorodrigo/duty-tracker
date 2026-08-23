import { defineMutation, defineQueryOptions, useQueryCache } from '@pinia/colada'
import { QUERY_KEYS } from '~/queries/keys'
import type { EngineerProfileResponse, UpdateProfileRequest } from '~/types/profile'
import { extractErrorDetail } from '~/utils/errors'

export const profileQuery = defineQueryOptions({
  key: QUERY_KEYS.profile.root(),
  query: () =>
    $fetch<EngineerProfileResponse>('/api/v1/profile', {
      baseURL: useRuntimeConfig().public.apiBase
    })
})

export const useUpdateProfile = defineMutation(() => {
  const toast = useToast()
  const queryCache = useQueryCache()

  const key = QUERY_KEYS.profile.root()

  return useMutation({
    onMutate: (request: UpdateProfileRequest) => {
      const previous = queryCache.getQueryData<EngineerProfileResponse>(key)
      if (previous) {
        queryCache.setQueryData(key, { ...previous, ...request })
      }
      return { previous: previous ?? null }
    },
    mutation: (request: UpdateProfileRequest) =>
      $fetch<EngineerProfileResponse>('/api/v1/profile', {
        baseURL: useRuntimeConfig().public.apiBase,
        method: 'PUT',
        body: request
      }),
    onError: (_err: unknown, _vars: UpdateProfileRequest, context) => {
      if (context.previous) {
        queryCache.setQueryData(key, context.previous)
      }
      toast.add({
        title: 'Failed to save profile',
        description: extractErrorDetail(_err),
        color: 'error',
        icon: 'i-lucide-x'
      })
    },
    onSuccess: (updated: EngineerProfileResponse) => {
      queryCache.setQueryData(key, updated)
      toast.add({
        title: 'Profile saved',
        color: 'success',
        icon: 'i-lucide-check'
      })
    }
  })
})
