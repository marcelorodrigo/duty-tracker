import { describe, it, expect } from 'vitest'
import { useOnCallPeriodReport } from '~/composables/useOnCallPeriodReport'
import { withComposable } from '../utils/test-composable'
import { setupFetchMock } from '../utils/mock-fetch'
import { buildReport } from '../utils/factories'

const mockReport = buildReport()

const mockFetch = setupFetchMock()

describe('useOnCallPeriodReport', () => {
  describe('initial state', () => {
    it('starts with report null, loading false, error null', async () => {
      const { report, loading, error } = await withComposable(() => useOnCallPeriodReport(1))

      expect(report.value).toBeNull()
      expect(loading.value).toBe(false)
      expect(error.value).toBeNull()
    })
  })

  describe('fetch()', () => {
    it('populates report on success', async () => {
      mockFetch.mockResolvedValueOnce(mockReport)
      const composable = await withComposable(() => useOnCallPeriodReport(1))

      await composable.fetch()

      expect(composable.report.value).toEqual(mockReport)
    })

    it('calls the correct endpoint for the given periodId', async () => {
      mockFetch.mockResolvedValueOnce(mockReport)
      const composable = await withComposable(() => useOnCallPeriodReport(42))

      await composable.fetch()

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/v1/oncall-periods/42/report',
        expect.objectContaining({ method: 'GET' })
      )
    })

    it('sets loading to true during fetch and false after', async () => {
      mockFetch.mockResolvedValueOnce(mockReport)
      const composable = await withComposable(() => useOnCallPeriodReport(1))

      const fetchPromise = composable.fetch()
      expect(composable.loading.value).toBe(true)
      await fetchPromise
      expect(composable.loading.value).toBe(false)
    })

    it('clears error before fetching', async () => {
      const composable = await withComposable(() => useOnCallPeriodReport(1))
      mockFetch.mockRejectedValueOnce(new Error('first error'))
      await composable.fetch()
      expect(composable.error.value).not.toBeNull()

      mockFetch.mockResolvedValueOnce(mockReport)
      await composable.fetch()
      expect(composable.error.value).toBeNull()
    })

    it('sets error with the original Error instance on failure', async () => {
      const err = new Error('Report generation failed')
      mockFetch.mockRejectedValueOnce(err)
      const composable = await withComposable(() => useOnCallPeriodReport(1))

      await composable.fetch()

      expect(composable.error.value).toBe(err)
      expect(composable.report.value).toBeNull()
    })

    it('wraps non-Error rejections in an Error', async () => {
      mockFetch.mockRejectedValueOnce('plain string')
      const composable = await withComposable(() => useOnCallPeriodReport(1))

      await composable.fetch()

      expect(composable.error.value).toBeInstanceOf(Error)
      expect(composable.error.value?.message).toBe('Failed to generate report')
    })

    it('sets loading to false even on failure', async () => {
      mockFetch.mockRejectedValueOnce(new Error('fail'))
      const composable = await withComposable(() => useOnCallPeriodReport(1))

      await composable.fetch()

      expect(composable.loading.value).toBe(false)
    })
  })
})
