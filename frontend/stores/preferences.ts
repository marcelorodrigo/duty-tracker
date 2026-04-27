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
    } catch (error: any) {
      // Only clear preferences on 404 (no saved preferences)
      if (error.response?.status === 404) {
        preferences.value = null
      } else {
        // For other errors (network, 5xx, etc.), preserve existing preferences
        // and rethrow so callers can handle transient failures
        throw error
      }
    }
  }

  async function updatePreferences(data: Partial<UserPreferences>) {
    preferences.value = await api.put<UserPreferences>('/preferences', data)
    applyColorMode(preferences.value.colorScheme)
    return preferences.value
  }

  function applyColorMode(scheme: 'DARK' | 'LIGHT' | 'AUTO') {
    const modeMap: Record<string, string> = { DARK: 'dark', LIGHT: 'light', AUTO: 'system' }
    colorMode.preference = modeMap[scheme] ?? 'system'
  }

  return { preferences, fetchPreferences, updatePreferences }
})
