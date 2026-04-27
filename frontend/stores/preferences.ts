import { defineStore } from 'pinia'

export interface UserPreferences {
  colorScheme: 'DARK' | 'LIGHT' | 'AUTO'
}

export const usePreferencesStore = defineStore('preferences', () => {
  const preferences = ref<UserPreferences | null>(null)
  const api = useApi()
  const colorMode = useColorMode()

  async function fetchPreferences() {
    try {
      preferences.value = await api.get<UserPreferences>('/preferences')
      applyColorMode(preferences.value.colorScheme)
    } catch {
      preferences.value = null
    }
  }

  async function updatePreferences(data: UserPreferences) {
    preferences.value = await api.put<UserPreferences>('/preferences', data)
    applyColorMode(data.colorScheme)
    return preferences.value
  }

  function applyColorMode(scheme: 'DARK' | 'LIGHT' | 'AUTO') {
    const modeMap: Record<string, string> = { DARK: 'dark', LIGHT: 'light', AUTO: 'system' }
    colorMode.preference = modeMap[scheme] ?? 'system'
  }

  return { preferences, fetchPreferences, updatePreferences }
})
