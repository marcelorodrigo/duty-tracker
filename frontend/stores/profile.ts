import { defineStore } from 'pinia'

export interface EngineerProfile {
  id: number
  employeeType: 'INTERNAL'
  workingDays: string[]
  workStartTime: string
  workEndTime: string
  locked: boolean
}

export const useProfileStore = defineStore('profile', () => {
  const profile = ref<EngineerProfile | null>(null)
  const api = useApi()

  const isLocked = computed(() => profile.value?.locked ?? false)

  async function fetchProfile() {
    try {
      profile.value = await api.get<EngineerProfile>('/profile')
    } catch {
      profile.value = null
    }
  }

  async function createProfile(data: Omit<EngineerProfile, 'id' | 'locked'>) {
    profile.value = await api.post<EngineerProfile>('/profile', data)
    return profile.value
  }

  async function updateProfile(data: Omit<EngineerProfile, 'id' | 'locked'>) {
    profile.value = await api.put<EngineerProfile>('/profile', data)
    return profile.value
  }

  return { profile, isLocked, fetchProfile, createProfile, updateProfile }
})
