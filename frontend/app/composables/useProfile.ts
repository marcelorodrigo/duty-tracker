import { computed } from 'vue'
import { useQuery } from '@pinia/colada'
import type { UpdateProfileRequest } from '~/types/profile'
import { profileQuery, useUpdateProfile } from '~/queries/profile'

export function useProfile() {
  const {
    state: profileState,
    data: profile,
    asyncStatus
  } = useQuery(() => ({ ...profileQuery }))

  const pending = computed(() => asyncStatus.value === 'loading')
  const error = computed(() => (profileState.value.error as Error | null) ?? null)

  const { mutateAsync } = useUpdateProfile()

  async function save(request: UpdateProfileRequest): Promise<void> {
    try {
      await mutateAsync(request)
    } catch {
      // error toast is shown by the mutation's onError handler
    }
  }

  return { profile, pending, error, save }
}
