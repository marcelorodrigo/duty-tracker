import { vi, beforeEach, afterEach } from 'vitest'

export function setupFetchMock(defaultResponse?: unknown) {
  const mockFetch = vi.fn()

  beforeEach(() => {
    vi.stubGlobal('$fetch', mockFetch)
    if (defaultResponse !== undefined) {
      mockFetch.mockResolvedValue(defaultResponse)
    }
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  return mockFetch
}
