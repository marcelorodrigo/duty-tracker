import { describe, it, expect } from 'vitest'
import { useEarnings } from '~/composables/useEarnings'
import { withComposable } from '../utils/test-composable'
import { setupFetchMock } from '../utils/mock-fetch'
import { buildEarnings } from '../utils/factories'

const mockEarnings = buildEarnings()

const mockFetch = setupFetchMock()

describe('useEarnings', () => {
  describe('initial state', () => {
    it('starts with data null, pending false, error null', async () => {
      mockFetch.mockResolvedValue(mockEarnings)
      const { data, pending, error } = await withComposable(() => useEarnings(1))

      expect(data.value).toBeNull()
      expect(pending.value).toBe(false)
      expect(error.value).toBeNull()
    })
  })

  describe('refresh()', () => {
    it('populates earnings on success', async () => {
      mockFetch.mockResolvedValue(mockEarnings)
      const { data, refresh } = await withComposable(() => useEarnings(1))

      await refresh()

      expect(data.value).toEqual(mockEarnings)
    })

    it('calls the correct endpoint for the given periodId', async () => {
      mockFetch.mockResolvedValue(mockEarnings)
      const { refresh } = await withComposable(() => useEarnings(42))

      await refresh()

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/v1/oncall-periods/42/earnings',
        expect.objectContaining({ method: 'GET' })
      )
    })

    it('sets pending to true during the request and false after', async () => {
      mockFetch.mockImplementation(() => {
        return Promise.resolve(mockEarnings)
      })
      const composable = await withComposable(() => useEarnings(1))

      const refreshPromise = composable.refresh()
      expect(composable.pending.value).toBe(true)
      await refreshPromise
      expect(composable.pending.value).toBe(false)
    })

    it('clears error before fetching', async () => {
      mockFetch.mockRejectedValueOnce(new Error('first error'))
      const composable = await withComposable(() => useEarnings(1))

      await composable.refresh()
      expect(composable.error.value).not.toBeNull()

      mockFetch.mockResolvedValueOnce(mockEarnings)
      await composable.refresh()
      expect(composable.error.value).toBeNull()
    })

    it('normalizes errors as ApiProblem', async () => {
      const err = new Error('Network failure')
      mockFetch.mockRejectedValue(err)
      const { refresh, error, data } = await withComposable(() => useEarnings(1))

      await refresh()

      expect(error.value?.message).toBe(err.message)
      expect(data.value).toBeNull()
    })

    it('wraps non-Error rejections in an Error', async () => {
      mockFetch.mockRejectedValue('plain string error')
      const { refresh, error } = await withComposable(() => useEarnings(1))

      await refresh()

      expect(error.value).toBeInstanceOf(Error)
      expect(error.value?.message).toBe('Failed to load earnings')
    })

    it('sets pending to false even when refresh fails', async () => {
      mockFetch.mockRejectedValue(new Error('fail'))
      const { refresh, pending } = await withComposable(() => useEarnings(1))

      await refresh()

      expect(pending.value).toBe(false)
    })
  })
})
