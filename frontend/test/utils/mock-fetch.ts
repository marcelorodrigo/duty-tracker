import { beforeEach, afterEach } from 'vitest'
import { mockFetch } from './mock-ofetch'

export function setupFetchMock(defaultResponse?: unknown) {
  beforeEach(() => {
    mockFetch.mockReset()
    if (defaultResponse !== undefined) {
      mockFetch.mockResolvedValue(defaultResponse)
    }
  })

  afterEach(() => {
    mockFetch.mockReset()
  })

  return mockFetch
}
