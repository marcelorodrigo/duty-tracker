import type { EngineerProfileResponse } from '~/types/profile'

export function useProfile() {
  const config = useRuntimeConfig()
  const { data: profile, pending, error } = useFetch<EngineerProfileResponse>(
    '/api/v1/profile',
    { baseURL: config.public.apiBase }
  )
  return { profile, pending, error }
}
