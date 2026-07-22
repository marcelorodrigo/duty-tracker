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
    it('starts with null data, empty suggestions, and no error', async () => {
      const { data, suggestions, pending, error } = await withComposable(() => useHolidays(1))

      expect(data.value).toBeNull()
      expect(suggestions.value).toEqual([])
      expect(pending.value).toBe(false)
      expect(error.value).toBeNull()
    })
  })

  describe('refresh()', () => {
    it('populates holidays on success', async () => {
      const { refresh, data } = await withComposable(() => useHolidays(1))
      mockFetch.mockResolvedValueOnce(mockHolidays)

      await refresh()

      expect(data.value).toEqual(mockHolidays)
    })

    it('calls the correct endpoint for the given periodId', async () => {
      const { refresh } = await withComposable(() => useHolidays(99))
      mockFetch.mockResolvedValueOnce([])

      await refresh()

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/v1/oncall-periods/99/holidays',
        expect.objectContaining({ method: 'GET' })
      )
    })

    it('sets pending to true during fetch and false after', async () => {
      const composable = await withComposable(() => useHolidays(1))
      mockFetch.mockResolvedValueOnce(mockHolidays)

      const fetchPromise = composable.refresh()
      expect(composable.pending.value).toBe(true)

      await fetchPromise
      expect(composable.pending.value).toBe(false)
    })

    it('sets error on failure', async () => {
      const { refresh, error } = await withComposable(() => useHolidays(1))
      mockFetch.mockRejectedValueOnce(new Error('API error'))

      await refresh()

      expect(error.value).toBeInstanceOf(Error)
    })

    it('wraps non-Error rejections in an Error', async () => {
      const { refresh, error } = await withComposable(() => useHolidays(1))
      mockFetch.mockRejectedValueOnce('string error')

      await refresh()

      expect(error.value?.message).toBe('Failed to load holidays')
    })

    it('sets pending to false even on failure', async () => {
      const { refresh, pending } = await withComposable(() => useHolidays(1))
      mockFetch.mockRejectedValueOnce(new Error('fail'))

      await refresh()

      expect(pending.value).toBe(false)
    })

    it('clears error before re-fetching', async () => {
      const composable = await withComposable(() => useHolidays(1))
      mockFetch.mockRejectedValueOnce(new Error('first fail'))
      await composable.refresh()
      expect(composable.error.value).not.toBeNull()

      mockFetch.mockResolvedValueOnce(mockHolidays)
      await composable.refresh()
      expect(composable.error.value).toBeNull()
    })
  })

  describe('loadSuggestions()', () => {
    it('populates suggestions on success', async () => {
      const { loadSuggestions, suggestions } = await withComposable(() => useHolidays(1))
      mockFetch.mockResolvedValueOnce(mockHolidays)

      await loadSuggestions('2026-04-01', '2026-04-30')

      expect(suggestions.value).toEqual(mockHolidays)
    })

    it('calls the suggestions endpoint with start and end query params', async () => {
      const { loadSuggestions } = await withComposable(() => useHolidays(1))
      mockFetch.mockResolvedValueOnce([])

      await loadSuggestions('2026-04-01', '2026-04-30')

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/v1/holidays/suggestions',
        expect.objectContaining({ query: { start: '2026-04-01', end: '2026-04-30' } })
      )
    })

    it('clears suggestions on failure without replacing the resource error', async () => {
      const composable = await withComposable(() => useHolidays(1))
      mockFetch.mockResolvedValueOnce(mockHolidays)
      await composable.loadSuggestions('2026-04-01', '2026-04-30')
      mockFetch.mockRejectedValueOnce(new Error('fail'))

      await composable.loadSuggestions('2026-04-01', '2026-04-30')

      expect(composable.suggestions.value).toEqual([])
      expect(composable.error.value).toBeNull()
    })

    it('does not use resource pending for suggestion lookups', async () => {
      const { loadSuggestions, pending } = await withComposable(() => useHolidays(1))
      mockFetch.mockRejectedValueOnce(new Error('fail'))

      await loadSuggestions('2026-04-01', '2026-04-30')

      expect(pending.value).toBe(false)
    })
  })

  describe('save()', () => {
    it('updates holidays ref with the server response on success', async () => {
      const { save, data } = await withComposable(() => useHolidays(1))
      const updated: HolidayResponse[] = [{ date: '2026-05-05', name: 'Bevrijdingsdag' }]
      mockFetch.mockResolvedValueOnce(updated)

      await save(updated)

      expect(data.value).toEqual(updated)
    })

    it('calls PUT to the correct endpoint with the updated holidays body', async () => {
      const { save } = await withComposable(() => useHolidays(7))
      const updated: HolidayResponse[] = [{ date: '2026-05-05', name: 'Bevrijdingsdag' }]
      mockFetch.mockResolvedValueOnce(updated)

      await save(updated)

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/v1/oncall-periods/7/holidays',
        expect.objectContaining({
          method: 'PUT',
          body: updated
        })
      )
    })

    it('re-throws failures for the mutation caller', async () => {
      const { save } = await withComposable(() => useHolidays(1))
      mockFetch.mockRejectedValueOnce(new Error('Save failed'))

      await expect(save([])).rejects.toThrow('Save failed')
    })
  })
})
