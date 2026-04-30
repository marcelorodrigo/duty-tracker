import type { EngineerProfileResponse, UpdateProfileRequest } from '~/types/profile'

export function useProfile() {
  const config = useRuntimeConfig()
  const toast = useToast()

  const { data: profile, pending, error } = useFetch<EngineerProfileResponse>(
    '/api/v1/profile',
    { baseURL: config.public.apiBase }
  )

  async function save(request: UpdateProfileRequest): Promise<void> {
    const previous = profile.value
      ? { ...profile.value }
      : null

    if (profile.value) {
      profile.value = { ...profile.value, ...request }
    }

    try {
      const updated = await $fetch<EngineerProfileResponse>('/api/v1/profile', {
        baseURL: config.public.apiBase,
        method: 'PUT',
        body: request
      })
      profile.value = updated
      toast.add({
        title: 'Profile saved',
        color: 'success',
        icon: 'i-lucide-check'
      })
    } catch (err: unknown) {
      if (previous) {
        profile.value = previous
      }
      toast.add({
        title: 'Failed to save profile',
        description: extractErrorDetail(err),
        color: 'error',
        icon: 'i-lucide-x'
      })
    }
  }

  return { profile, pending, error, save }
}
