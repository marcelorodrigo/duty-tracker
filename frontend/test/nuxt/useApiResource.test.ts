import { isReadonly } from 'vue'
import { describe, expect, it } from 'vitest'
import { useApiResource } from '~/composables/useApiResource'
import { ApiProblem } from '~/utils/api'

describe('useApiResource', () => {
  it('exposes the documented initial contract', () => {
    const resource = useApiResource(async () => 'loaded', 'Failed to load resource')

    expect(resource.data.value).toBeNull()
    expect(resource.pending.value).toBe(false)
    expect(resource.error.value).toBeNull()
    expect(isReadonly(resource.pending)).toBe(true)
    expect(isReadonly(resource.error)).toBe(true)
  })

  it('tracks refresh state and stores the response', async () => {
    let resolveRequest!: (value: string) => void
    const request = new Promise<string>((resolve) => {
      resolveRequest = resolve
    })
    const resource = useApiResource(() => request, 'Failed to load resource')

    const refreshPromise = resource.refresh()

    expect(resource.pending.value).toBe(true)
    resolveRequest('loaded')
    await refreshPromise
    expect(resource.data.value).toBe('loaded')
    expect(resource.pending.value).toBe(false)
  })

  it('normalizes transport failures as ApiProblem and clears them before retrying', async () => {
    let attempt = 0
    const resource = useApiResource(async () => {
      attempt += 1
      if (attempt === 1) throw new Error('Network unavailable')
      return 'loaded'
    }, 'Failed to load resource')

    await resource.refresh()

    expect(resource.error.value).toBeInstanceOf(ApiProblem)
    expect(resource.error.value?.detail).toBe('Network unavailable')

    await resource.refresh()

    expect(resource.data.value).toBe('loaded')
    expect(resource.error.value).toBeNull()
  })

  it('preserves Problem Details produced by the API client', async () => {
    const problem = new ApiProblem({ status: 404, detail: 'Resource not found' })
    const resource = useApiResource(async () => {
      throw problem
    }, 'Failed to load resource')

    await resource.refresh()

    expect(resource.error.value).toBe(problem)
  })
})
