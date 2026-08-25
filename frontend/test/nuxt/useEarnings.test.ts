import { describe, it, expect, beforeEach } from 'vitest'
import { flushPromises } from '@vue/test-utils'
import { useEarnings } from '~/composables/useEarnings'
import { withComposable } from '../utils/test-composable'
import { setupFetchMock } from '../utils/mock-fetch'
import { buildEarnings } from '../utils/factories'

const mockEarnings = buildEarnings()

const mockFetch = setupFetchMock(mockEarnings)

describe('useEarnings', () => {
  beforeEach(() => {
    mockFetch.mockReset()
    mockFetch.mockResolvedValue(mockEarnings)
  })

  it('loads earnings via GET /api/v1/oncall-periods/{periodId}/earnings', async () => {
    const { earnings } = await withComposable(() => useEarnings(1))
    await flushPromises()

    expect(mockFetch).toHaveBeenCalledWith(
      '/api/v1/oncall-periods/1/earnings',
      expect.objectContaining({ baseURL: expect.any(String) })
    )
    expect(earnings.value).toEqual(mockEarnings)
  })

  it('calls the correct endpoint for the given periodId', async () => {
    await withComposable(() => useEarnings(42))
    await flushPromises()

    expect(mockFetch).toHaveBeenCalledWith(
      '/api/v1/oncall-periods/42/earnings',
      expect.objectContaining({ baseURL: expect.any(String) })
    )
  })

  it('sets pending to true while loading and false after', async () => {
    let resolveFetch!: (value: unknown) => void
    const deferred = new Promise<unknown>((resolve) => {
      resolveFetch = resolve
    })
    mockFetch.mockReturnValue(deferred)
    const { pending } = await withComposable(() => useEarnings(1))

    expect(pending.value).toBe(true)
    resolveFetch(mockEarnings)
    await flushPromises()
    expect(pending.value).toBe(false)
  })

  it('surfaces the error when the request fails', async () => {
    const err = new Error('Network error')
    mockFetch.mockRejectedValue(err)
    const { error, earnings } = await withComposable(() => useEarnings(1))
    await flushPromises()

    expect(error.value).toBe(err)
    expect(earnings.value).toBeUndefined()
  })
})
