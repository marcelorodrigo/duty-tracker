import { computed, toValue, type MaybeRefOrGetter } from 'vue'
import { useQuery } from '@pinia/colada'
import { QUERY_KEYS } from '~/queries/keys'
import { calendarFeedPreviewQuery, useImportCalendarEvent } from '~/queries/calendarFeed'
import type { CalendarFeedEvent } from '~/types/calendarFeed'

export function useCalendarFeed(enabled: MaybeRefOrGetter<boolean> = true) {
  const {
    state: previewState,
    data: preview,
    asyncStatus,
    refresh: fetchPreview,
  } = useQuery(() => ({
    ...calendarFeedPreviewQuery,
    enabled: toValue(enabled),
  }))

  const {
    mutateAsync,
    asyncStatus: importStatus,
  } = useImportCalendarEvent()

  const importEvent = (event: CalendarFeedEvent): Promise<boolean> =>
    mutateAsync(event)
      .then(() => true)
      .catch(() => false)

  const pending = computed(() => asyncStatus.value === 'loading')
  const error = computed(() => previewState.value.error)
  const importing = computed(() => importStatus.value === 'loading')

  return {
    preview,
    pending,
    error,
    importing,
    fetchPreview,
    importEvent,
  }
}
