import { describe, it, expect } from 'vitest'
import { useOnCallPeriodReport } from '~/composables/useOnCallPeriodReport'
import { withComposable } from '../utils/test-composable'
import { setupFetchMock } from '../utils/mock-fetch'
import { buildReport } from '../utils/factories'

const mockReport = buildReport()

const mockFetch = setupFetchMock()

describe('useOnCallPeriodReport', () => {
  describe('initial state', () => {
    it('starts with data null, pending false, error null', async () => {
      const { data, pending, error } = await withComposable(() => useOnCallPeriodReport(1))

      expect(data.value).toBeNull()
      expect(pending.value).toBe(false)
      expect(error.value).toBeNull()
    })
  })

  describe('refresh()', () => {
    it('populates report on success', async () => {
      mockFetch.mockResolvedValueOnce(mockReport)
      const composable = await withComposable(() => useOnCallPeriodReport(1))

      await composable.refresh()

      expect(composable.data.value).toEqual(mockReport)
    })

    it('calls the correct endpoint for the given periodId', async () => {
      mockFetch.mockResolvedValueOnce(mockReport)
      const composable = await withComposable(() => useOnCallPeriodReport(42))

      await composable.refresh()

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/v1/oncall-periods/42/report',
        expect.objectContaining({ method: 'GET' })
      )
    })

    it('sets pending to true during refresh and false after', async () => {
      mockFetch.mockResolvedValueOnce(mockReport)
      const composable = await withComposable(() => useOnCallPeriodReport(1))

      const refreshPromise = composable.refresh()
      expect(composable.pending.value).toBe(true)
      await refreshPromise
      expect(composable.pending.value).toBe(false)
    })

    it('clears error before fetching', async () => {
      const composable = await withComposable(() => useOnCallPeriodReport(1))
      mockFetch.mockRejectedValueOnce(new Error('first error'))
      await composable.refresh()
      expect(composable.error.value).not.toBeNull()

      mockFetch.mockResolvedValueOnce(mockReport)
      await composable.refresh()
      expect(composable.error.value).toBeNull()
    })

    it('normalizes errors as ApiProblem', async () => {
      const err = new Error('Report generation failed')
      mockFetch.mockRejectedValueOnce(err)
      const composable = await withComposable(() => useOnCallPeriodReport(1))

      await composable.refresh()

      expect(composable.error.value?.message).toBe(err.message)
      expect(composable.data.value).toBeNull()
    })

    it('wraps non-Error rejections in an Error', async () => {
      mockFetch.mockRejectedValueOnce('plain string')
      const composable = await withComposable(() => useOnCallPeriodReport(1))

      await composable.refresh()

      expect(composable.error.value).toBeInstanceOf(Error)
      expect(composable.error.value?.message).toBe('Failed to generate report')
    })

    it('sets pending to false even on failure', async () => {
      mockFetch.mockRejectedValueOnce(new Error('fail'))
      const composable = await withComposable(() => useOnCallPeriodReport(1))

      await composable.refresh()

      expect(composable.pending.value).toBe(false)
    })
  })
})
