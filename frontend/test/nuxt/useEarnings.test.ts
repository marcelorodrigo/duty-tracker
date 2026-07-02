import { describe, it, expect } from 'vitest'
import { useEarnings } from '~/composables/useEarnings'
import { withComposable } from '../utils/test-composable'
import { setupFetchMock } from '../utils/mock-fetch'
import { buildEarnings } from '../utils/factories'

const mockEarnings = buildEarnings()

const mockFetch = setupFetchMock()

describe('useEarnings', () => {
  describe('initial state', () => {
    it('starts with earnings null, loading false, error null', async () => {
      mockFetch.mockResolvedValue(mockEarnings)
      const { earnings, loading, error } = await withComposable(() => useEarnings(1))

      expect(earnings.value).toBeNull()
      expect(loading.value).toBe(false)
      expect(error.value).toBeNull()
    })
  })

  describe('fetch()', () => {
    it('populates earnings on success', async () => {
      mockFetch.mockResolvedValue(mockEarnings)
      const { earnings, fetch } = await withComposable(() => useEarnings(1))

      await fetch()

      expect(earnings.value).toEqual(mockEarnings)
    })

    it('calls the correct endpoint for the given periodId', async () => {
      mockFetch.mockResolvedValue(mockEarnings)
      const { fetch } = await withComposable(() => useEarnings(42))

      await fetch()

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/v1/oncall-periods/42/earnings',
        expect.any(Object)
      )
    })

    it('sets loading to true during the request and false after', async () => {
      let loadingDuringFetch = false
      mockFetch.mockImplementation(() => {
        loadingDuringFetch = true // we assert it was true at some point
        return Promise.resolve(mockEarnings)
      })
      const composable = await withComposable(() => useEarnings(1))

      const fetchPromise = composable.fetch()
      // loading should be true immediately after calling fetch (before await)
      expect(composable.loading.value).toBe(true)
      await fetchPromise
      expect(composable.loading.value).toBe(false)
      expect(loadingDuringFetch).toBe(true)
    })

    it('clears error before fetching', async () => {
      mockFetch.mockRejectedValueOnce(new Error('first error'))
      const composable = await withComposable(() => useEarnings(1))

      await composable.fetch()
      expect(composable.error.value).not.toBeNull()

      mockFetch.mockResolvedValueOnce(mockEarnings)
      await composable.fetch()
      expect(composable.error.value).toBeNull()
    })

    it('sets error on failure with the Error instance', async () => {
      const err = new Error('Network failure')
      mockFetch.mockRejectedValue(err)
      const { fetch, error, earnings } = await withComposable(() => useEarnings(1))

      await fetch()

      expect(error.value).toBe(err)
      expect(earnings.value).toBeNull()
    })

    it('wraps non-Error rejections in an Error', async () => {
      mockFetch.mockRejectedValue('plain string error')
      const { fetch, error } = await withComposable(() => useEarnings(1))

      await fetch()

      expect(error.value).toBeInstanceOf(Error)
      expect(error.value?.message).toBe('Failed to load earnings')
    })

    it('sets loading to false even when fetch fails', async () => {
      mockFetch.mockRejectedValue(new Error('fail'))
      const { fetch, loading } = await withComposable(() => useEarnings(1))

      await fetch()

      expect(loading.value).toBe(false)
    })
  })
})
