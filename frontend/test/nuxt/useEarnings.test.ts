import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import { defineComponent } from 'vue'
import { flushPromises } from '@vue/test-utils'
import { useEarnings } from '~/composables/useEarnings'
import type { EarningsResponse } from '~/types/earnings'

const mockFetch = vi.fn()

beforeEach(() => {
  vi.stubGlobal('$fetch', mockFetch)
})

afterEach(() => {
  vi.unstubAllGlobals()
})

async function withComposable(periodId: number): Promise<ReturnType<typeof useEarnings>> {
  let composable!: ReturnType<typeof useEarnings>

  await mountSuspended(defineComponent({
    setup() {
      composable = useEarnings(periodId)
      return () => null
    }
  }))

  await flushPromises()
  return composable
}

const mockEarnings: EarningsResponse = {
  periodId: 1,
  periodStart: '2026-04-01T14:00:00',
  periodEnd: '2026-04-30T14:00:00',
  standbyLines: [],
  incidentLines: [],
  grandTotal: '123.45'
}

describe('useEarnings', () => {
  describe('initial state', () => {
    it('starts with earnings null, loading false, error null', async () => {
      mockFetch.mockResolvedValue(mockEarnings)
      const { earnings, loading, error } = await withComposable(1)

      expect(earnings.value).toBeNull()
      expect(loading.value).toBe(false)
      expect(error.value).toBeNull()
    })
  })

  describe('fetch()', () => {
    it('populates earnings on success', async () => {
      mockFetch.mockResolvedValue(mockEarnings)
      const { earnings, fetch } = await withComposable(1)

      await fetch()

      expect(earnings.value).toEqual(mockEarnings)
    })

    it('calls the correct endpoint for the given periodId', async () => {
      mockFetch.mockResolvedValue(mockEarnings)
      const { fetch } = await withComposable(42)

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
      const composable = await withComposable(1)

      const fetchPromise = composable.fetch()
      // loading should be true immediately after calling fetch (before await)
      expect(composable.loading.value).toBe(true)
      await fetchPromise
      expect(composable.loading.value).toBe(false)
      expect(loadingDuringFetch).toBe(true)
    })

    it('clears error before fetching', async () => {
      mockFetch.mockRejectedValueOnce(new Error('first error'))
      const composable = await withComposable(1)

      await composable.fetch()
      expect(composable.error.value).not.toBeNull()

      mockFetch.mockResolvedValueOnce(mockEarnings)
      await composable.fetch()
      expect(composable.error.value).toBeNull()
    })

    it('sets error on failure with the Error instance', async () => {
      const err = new Error('Network failure')
      mockFetch.mockRejectedValue(err)
      const { fetch, error, earnings } = await withComposable(1)

      await fetch()

      expect(error.value).toBe(err)
      expect(earnings.value).toBeNull()
    })

    it('wraps non-Error rejections in an Error', async () => {
      mockFetch.mockRejectedValue('plain string error')
      const { fetch, error } = await withComposable(1)

      await fetch()

      expect(error.value).toBeInstanceOf(Error)
      expect(error.value?.message).toBe('Failed to load earnings')
    })

    it('sets loading to false even when fetch fails', async () => {
      mockFetch.mockRejectedValue(new Error('fail'))
      const { fetch, loading } = await withComposable(1)

      await fetch()

      expect(loading.value).toBe(false)
    })
  })
})
