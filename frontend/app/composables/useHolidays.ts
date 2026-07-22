import type { HolidayResponse } from '~/types/holiday'

export function useHolidays(periodId: number) {
  const toast = useToast()
  const { $api } = useNuxtApp()

  const suggestions = ref<HolidayResponse[]>([])
  const { data, pending, error, refresh } = useApiResource<HolidayResponse[]>(
    () => $api.get(`/oncall-periods/${periodId}/holidays`),
    'Failed to load holidays'
  )

  async function loadSuggestions(start: string, end: string): Promise<void> {
    try {
      suggestions.value = await $api.get<HolidayResponse[]>('/holidays/suggestions', {
        query: { start, end }
      })
    } catch {
      suggestions.value = []
    }
  }

  async function save(updated: HolidayResponse[]): Promise<void> {
    try {
      data.value = await $api.put<HolidayResponse[]>(`/oncall-periods/${periodId}/holidays`, updated)
      toast.add({
        title: 'Holidays saved',
        color: 'success',
        icon: 'i-lucide-check'
      })
    } catch (err) {
      toast.add({
        title: 'Failed to save holidays',
        description: getApiErrorMessage(err),
        color: 'error',
        icon: 'i-lucide-x'
      })
      throw err
    }
  }

  return {
    data,
    pending,
    error,
    refresh,
    suggestions: readonly(suggestions),
    loadSuggestions,
    save
  }
}
