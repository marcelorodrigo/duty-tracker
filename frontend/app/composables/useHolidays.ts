import { computed, ref } from 'vue'
import { useQuery } from '@pinia/colada'
import { QUERY_KEYS } from '~/queries/keys'
import type { HolidayResponse } from '~/types/holiday'
import { holidaysQuery, useSaveHolidays } from '~/queries/holidays'

export function useHolidays(periodId: number) {
  const {
    state: holidaysState,
    data: holidays,
    asyncStatus: holidaysStatus,
    refetch: fetchHolidays
  } = useQuery(() => ({ ...holidaysQuery(periodId) }))

  const suggestionStart = ref('')
  const suggestionEnd = ref('')

  const {
    state: suggestionsState,
    data: rawSuggestions,
    asyncStatus: suggestionsStatus,
    refetch: refetchSuggestions
  } = useQuery(() => ({
    key: QUERY_KEYS.holidays.suggestions(),
    query: () =>
      $fetch<HolidayResponse[]>('/api/v1/holidays/suggestions', {
        baseURL: useRuntimeConfig().public.apiBase,
        query: { start: suggestionStart.value, end: suggestionEnd.value }
      }),
    enabled: !!suggestionStart.value && !!suggestionEnd.value
  }))

  const suggestions = computed<HolidayResponse[]>(() => {
    if (suggestionsState.value.status === 'error') return []
    return rawSuggestions.value ?? []
  })

  const pending = computed(() => holidaysStatus.value === 'loading')
  const suggestionsPending = computed(() => suggestionsStatus.value === 'loading')
  const error = computed(
    () => (holidaysState.value.error as Error | null) ?? (suggestionsState.value.error as Error | null)
  )

  const { mutateAsync: saveHolidaysMutation, isLoading: savePending } = useSaveHolidays()

  async function fetchSuggestions(start: string, end: string): Promise<void> {
    suggestionStart.value = start
    suggestionEnd.value = end
    try {
      await refetchSuggestions(true)
    } catch {
      // error is surfaced via the suggestions query state (and the `error` computed)
    }
  }

  async function saveHolidays(updated: HolidayResponse[]): Promise<void> {
    await saveHolidaysMutation({ periodId, holidays: updated })
  }

  return {
    holidays,
    suggestions,
    pending,
    suggestionsPending,
    savePending,
    error,
    fetchHolidays,
    fetchSuggestions,
    saveHolidays
  }
}
