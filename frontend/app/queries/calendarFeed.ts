import { defineMutation, defineQueryOptions, useQueryCache } from '@pinia/colada'
import { QUERY_KEYS } from '~/queries/keys'
import type { CalendarFeedEvent, CalendarFeedPreview } from '~/types/calendarFeed'
import { extractErrorDetail } from '~/utils/errors'

export const calendarFeedPreviewQuery = defineQueryOptions({
  key: QUERY_KEYS.calendarFeed.preview(),
  query: () =>
    $fetch<CalendarFeedPreview>('/api/v1/calendar-feed/preview', {
      baseURL: useRuntimeConfig().public.apiBase,
    }),
})

export const useImportCalendarEvent = defineMutation(() => {
  const toast = useToast()
  const queryCache = useQueryCache()

  return useMutation({
    mutation: (event: { startDateTime: string, endDateTime: string }) =>
      $fetch('/api/v1/oncall-periods', {
        baseURL: useRuntimeConfig().public.apiBase,
        method: 'POST',
        body: {
          startDateTime: event.startDateTime,
          endDateTime: event.endDateTime,
        },
      }),
    onSuccess: () => {
      toast.add({
        title: 'On-call period imported',
        color: 'success',
        icon: 'i-lucide-check',
      })
      queryCache.invalidateQueries({
        key: QUERY_KEYS.calendarFeed.preview(),
        exact: true,
      })
    },
    onError: (err: unknown) => {
      toast.add({
        title: 'Failed to import period',
        description: extractErrorDetail(err),
        color: 'error',
        icon: 'i-lucide-x',
      })
    },
  })
})
