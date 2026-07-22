import type { EngineerProfileResponse, UpdateProfileRequest } from '~/types/profile'

export function useProfile() {
  const toast = useToast()
  const { $api } = useNuxtApp()

  const { data, pending, error, refresh } = useApiResource<EngineerProfileResponse>(
    () => $api.get('/profile'),
    'Failed to load profile'
  )

  async function save(request: UpdateProfileRequest): Promise<void> {
    const previous = data.value
      ? { ...data.value }
      : null

    if (data.value) {
      data.value = { ...data.value, ...request }
    }

    try {
      const updated = await $api.put<EngineerProfileResponse>('/profile', request)
      data.value = updated
      toast.add({
        title: 'Profile saved',
        color: 'success',
        icon: 'i-lucide-check'
      })
    } catch (err: unknown) {
      if (previous) {
        data.value = previous
      }
      toast.add({
        title: 'Failed to save profile',
        description: extractErrorDetail(err),
        color: 'error',
        icon: 'i-lucide-x'
      })
    }
  }

  return { data, pending, error, refresh, save }
}
