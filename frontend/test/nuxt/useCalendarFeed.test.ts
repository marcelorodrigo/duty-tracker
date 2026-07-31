import { describe, it, expect, vi, beforeEach } from 'vitest'
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
    mockFetch
      .mockResolvedValueOnce(undefined)
      .mockResolvedValueOnce({ upcoming: [], past: [] })

    const event = mockPreview.upcoming[0]!
    await importEvent(event)

    expect(mockFetch).toHaveBeenNthCalledWith(
      1,
      '/api/v1/oncall-periods',
      expect.objectContaining({
        method: 'POST',
        body: { startDateTime: event.startDateTime, endDateTime: event.endDateTime }
      })
    )
    expect(preview.value).toEqual({ upcoming: [], past: [] })
  })

  it('handles preview fetch errors', async () => {
    mockFetch.mockRejectedValueOnce(new Error('Network error'))
    const { fetchPreview, error, preview } = await withComposable(() => useCalendarFeed())

    await fetchPreview()

    expect(error.value).toBeInstanceOf(Error)
    expect(error.value?.message).toBe('Network error')
    expect(preview.value).toBeNull()
  })
})
