import { describe, expect, it } from 'vitest'
import { useOnCallPeriod } from '~/composables/useOnCallPeriod'
import { withComposable } from '../utils/test-composable'
import { setupFetchMock } from '../utils/mock-fetch'
import { buildPeriod } from '../utils/factories'

const mockFetch = setupFetchMock()

describe('useOnCallPeriod', () => {
  it('loads the requested period through the shared resource contract', async () => {
    const period = buildPeriod({ id: 42 })
    mockFetch.mockResolvedValueOnce(period)
    const resource = await withComposable(() => useOnCallPeriod(42))

    await resource.refresh()

    expect(resource.data.value).toEqual(period)
    expect(mockFetch).toHaveBeenCalledWith(
      '/api/v1/oncall-periods/42',
      expect.objectContaining({ method: 'GET' })
    )
  })

  it('exposes a normalized error when loading fails', async () => {
    mockFetch.mockRejectedValueOnce(new Error('Network unavailable'))
    const resource = await withComposable(() => useOnCallPeriod(42))

    await resource.refresh()

    expect(resource.error.value?.detail).toBe('Network unavailable')
    expect(resource.data.value).toBeNull()
  })
})
