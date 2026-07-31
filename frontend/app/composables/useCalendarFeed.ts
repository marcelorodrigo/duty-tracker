import type { CalendarFeedPreview } from '~/types/calendarFeed'

export function useCalendarFeed() {
  const config = useRuntimeConfig()
  const toast = useToast()

  const preview = ref<CalendarFeedPreview | null>(null)
  const pending = ref(false)
  const error = ref<Error | null>(null)

  async function fetchPreview(): Promise<void> {
    pending.value = true
    error.value = null
    try {
      const response = await $fetch<CalendarFeedPreview>('/api/v1/calendar-feed/preview', {
        baseURL: config.public.apiBase
      })
      preview.value = response
    } catch (err) {
      error.value = err instanceof Error ? err : new Error('Failed to fetch calendar feed preview')
    } finally {
      pending.value = false
    }
  }

  async function importEvent(event: { startDateTime: string, endDateTime: string }): Promise<boolean> {
    try {
      await $fetch('/api/v1/oncall-periods', {
        baseURL: config.public.apiBase,
        method: 'POST',
        body: {
          startDateTime: event.startDateTime,
          endDateTime: event.endDateTime
        }
      })
      toast.add({
        title: 'On-call period imported',
        color: 'success',
        icon: 'i-lucide-check'
      })
      await fetchPreview()
      return true
    } catch (err: unknown) {
      toast.add({
        title: 'Failed to import period',
        description: extractErrorDetail(err),
        color: 'error',
        icon: 'i-lucide-x'
      })
      return false
    }
  }

  return {
    preview,
    pending,
    error,
    fetchPreview,
    importEvent
  }
}
