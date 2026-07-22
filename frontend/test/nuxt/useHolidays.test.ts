import { describe, it, expect } from 'vitest'
import { useHolidays } from '~/composables/useHolidays'
import { withComposable } from '../utils/test-composable'
import { setupFetchMock } from '../utils/mock-fetch'
import { buildHoliday } from '../utils/factories'
import type { HolidayResponse } from '~/types/holiday'

const mockHolidays: HolidayResponse[] = [
  buildHoliday({ date: '2026-04-06', name: 'Tweede Paasdag' }),
  buildHoliday({ date: '2026-04-27', name: 'Koningsdag' })
]

const mockFetch = setupFetchMock([])

describe('useHolidays', () => {
  describe('initial state', () => {
    it('starts with empty holidays, suggestions, and no error', async () => {
      const { holidays, suggestions, pending, error } = await withComposable(() => useHolidays(1))

      expect(holidays.value).toEqual([])
      expect(suggestions.value).toEqual([])
      expect(pending.value).toBe(false)
      expect(error.value).toBeNull()
    })
  })

  describe('fetchHolidays()', () => {
    it('populates holidays on success', async () => {
      const { fetchHolidays, holidays } = await withComposable(() => useHolidays(1))
      mockFetch.mockResolvedValueOnce(mockHolidays)

      await fetchHolidays()

      expect(holidays.value).toEqual(mockHolidays)
    })

    it('calls the correct endpoint for the given periodId', async () => {
      const { fetchHolidays } = await withComposable(() => useHolidays(99))
      mockFetch.mockResolvedValueOnce([])

      await fetchHolidays()

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/v1/oncall-periods/99/holidays',
        expect.objectContaining({ method: 'GET' })
      )
    })

    it('sets pending to true during fetch and false after', async () => {
      const composable = await withComposable(() => useHolidays(1))
      mockFetch.mockResolvedValueOnce(mockHolidays)

      const fetchPromise = composable.fetchHolidays()
      expect(composable.pending.value).toBe(true)

      await fetchPromise
      expect(composable.pending.value).toBe(false)
    })

    it('sets error on failure', async () => {
      const { fetchHolidays, error } = await withComposable(() => useHolidays(1))
      mockFetch.mockRejectedValueOnce(new Error('API error'))

      await fetchHolidays()

      expect(error.value).toBeInstanceOf(Error)
    })

    it('wraps non-Error rejections in an Error', async () => {
      const { fetchHolidays, error } = await withComposable(() => useHolidays(1))
      mockFetch.mockRejectedValueOnce('string error')

      await fetchHolidays()

      expect(error.value?.message).toBe('Failed to load holidays')
    })

    it('sets pending to false even on failure', async () => {
      const { fetchHolidays, pending } = await withComposable(() => useHolidays(1))
      mockFetch.mockRejectedValueOnce(new Error('fail'))

      await fetchHolidays()

      expect(pending.value).toBe(false)
    })

    it('clears error before re-fetching', async () => {
      const composable = await withComposable(() => useHolidays(1))
      mockFetch.mockRejectedValueOnce(new Error('first fail'))
      await composable.fetchHolidays()
      expect(composable.error.value).not.toBeNull()

      mockFetch.mockResolvedValueOnce(mockHolidays)
      await composable.fetchHolidays()
      expect(composable.error.value).toBeNull()
    })
  })

  describe('fetchSuggestions()', () => {
    it('populates suggestions on success', async () => {
      const { fetchSuggestions, suggestions } = await withComposable(() => useHolidays(1))
      mockFetch.mockResolvedValueOnce(mockHolidays)

      await fetchSuggestions('2026-04-01', '2026-04-30')

      expect(suggestions.value).toEqual(mockHolidays)
    })

    it('calls the suggestions endpoint with start and end query params', async () => {
      const { fetchSuggestions } = await withComposable(() => useHolidays(1))
      mockFetch.mockResolvedValueOnce([])

      await fetchSuggestions('2026-04-01', '2026-04-30')

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/v1/holidays/suggestions',
        expect.objectContaining({ query: { start: '2026-04-01', end: '2026-04-30' } })
      )
    })

    it('clears suggestions and sets error on failure', async () => {
      const composable = await withComposable(() => useHolidays(1))
      composable.suggestions.value = mockHolidays
      mockFetch.mockRejectedValueOnce(new Error('fail'))

      await composable.fetchSuggestions('2026-04-01', '2026-04-30')

      expect(composable.suggestions.value).toEqual([])
      expect(composable.error.value).toBeInstanceOf(Error)
    })

    it('sets pending to false after failure', async () => {
      const { fetchSuggestions, pending } = await withComposable(() => useHolidays(1))
      mockFetch.mockRejectedValueOnce(new Error('fail'))

      await fetchSuggestions('2026-04-01', '2026-04-30')

      expect(pending.value).toBe(false)
    })
  })

  describe('saveHolidays()', () => {
    it('updates holidays ref with the server response on success', async () => {
      const { saveHolidays, holidays } = await withComposable(() => useHolidays(1))
      const updated: HolidayResponse[] = [{ date: '2026-05-05', name: 'Bevrijdingsdag' }]
      mockFetch.mockResolvedValueOnce(updated)

      await saveHolidays(updated)

      expect(holidays.value).toEqual(updated)
    })

    it('calls PUT to the correct endpoint with the updated holidays body', async () => {
      const { saveHolidays } = await withComposable(() => useHolidays(7))
      const updated: HolidayResponse[] = [{ date: '2026-05-05', name: 'Bevrijdingsdag' }]
      mockFetch.mockResolvedValueOnce(updated)

      await saveHolidays(updated)

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/v1/oncall-periods/7/holidays',
        expect.objectContaining({
          method: 'PUT',
          body: updated
        })
      )
    })

    it('sets savePending to false after success', async () => {
      const { saveHolidays, savePending } = await withComposable(() => useHolidays(1))
      mockFetch.mockResolvedValueOnce([])

      await saveHolidays([])

      expect(savePending.value).toBe(false)
    })

    it('re-throws and sets savePending to false on failure', async () => {
      const { saveHolidays, savePending } = await withComposable(() => useHolidays(1))
      mockFetch.mockRejectedValueOnce(new Error('Save failed'))

      await expect(saveHolidays([])).rejects.toThrow('Save failed')
      expect(savePending.value).toBe(false)
    })
  })
})
