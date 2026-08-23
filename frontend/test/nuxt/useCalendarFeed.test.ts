import { describe, it, expect, vi, beforeEach } from 'vitest'
import { flushPromises } from '@vue/test-utils'
import { useCalendarFeed } from '~/composables/useCalendarFeed'
import { withComposable } from '../utils/test-composable'
import { setupFetchMock } from '../utils/mock-fetch'
import type { CalendarFeedPreview } from '~/types/calendarFeed'

const mockPreview: CalendarFeedPreview = {
  upcoming: [
    { startDateTime: '2026-02-01T09:00:00', endDateTime: '2026-02-08T09:00:00', summary: 'On-call' }
  ],
  past: []
}

const mockFetch = setupFetchMock(mockPreview)

describe('useCalendarFeed', () => {
  beforeEach(() => {
    mockFetch.mockReset()
    mockFetch.mockResolvedValue(mockPreview)
  })

  it('loads preview via GET /api/v1/calendar-feed/preview', async () => {
    const { fetchPreview, preview } = await withComposable(() => useCalendarFeed())

    await fetchPreview()

    expect(mockFetch).toHaveBeenCalledWith(
      '/api/v1/calendar-feed/preview',
      expect.objectContaining({ baseURL: expect.any(String) })
    )
    expect(preview.value).toEqual(mockPreview)
  })

  it('imports an event via POST /api/v1/oncall-periods and refreshes preview', async () => {
    const { importEvent, preview } = await withComposable(() => useCalendarFeed())
    await flushPromises()
    // POST resolves to undefined, then the preview query is invalidated and refetched
    mockFetch.mockResolvedValueOnce(undefined)
    mockFetch.mockResolvedValueOnce({ upcoming: [], past: [] })

    const event = mockPreview.upcoming[0]!
    await importEvent(event)
    await flushPromises()
    await flushPromises()

    expect(mockFetch).toHaveBeenCalledWith(
      '/api/v1/oncall-periods',
      expect.objectContaining({
        method: 'POST',
        body: { startDateTime: event.startDateTime, endDateTime: event.endDateTime }
      })
    )
    expect(preview.value).toEqual({ upcoming: [], past: [] })
  })

  it('handles preview fetch errors', async () => {
    mockFetch.mockRejectedValue(new Error('Network error'))
    const { fetchPreview, error, preview } = await withComposable(() => useCalendarFeed())

    await fetchPreview()

    expect(error.value).toBeInstanceOf(Error)
    expect(error.value?.message).toBe('Network error')
    expect(preview.value).toBeFalsy()
  })

  it('resolves importEvent to false when the POST request fails', async () => {
    const { importEvent } = await withComposable(() => useCalendarFeed())
    await flushPromises()
    mockFetch.mockRejectedValueOnce(new Error('Import failed'))
    const event = mockPreview.upcoming[0]!

    await expect(importEvent(event)).resolves.toBe(false)
  })
})
