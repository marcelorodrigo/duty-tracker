import { describe, expect, it, vi } from 'vitest'
import {
  ApiProblem,
  createApiClient
} from '~/utils/api'
import type { ApiTransport } from '~/utils/api'

function createTransportMock() {
  const spy = vi.fn()
  const transport: ApiTransport = async <TResponse>(request, options) => {
    return await spy(request, options) as TResponse
  }

  return { spy, transport }
}

describe('API client', () => {
  it('applies the shared request policy and returns typed data', async () => {
    const { spy, transport } = createTransportMock()
    spy.mockResolvedValue({ id: 42 })
    const client = createApiClient(transport)

    const response = await client.post<{ id: number }>('/oncall-periods', {
      startDateTime: '2026-07-20T14:00:00',
      endDateTime: '2026-07-27T14:00:00'
    })

    expect(response).toEqual({ id: 42 })
    expect(spy).toHaveBeenCalledWith('/api/v1/oncall-periods', {
      method: 'POST',
      headers: { accept: 'application/json, application/problem+json' },
      timeout: 10_000,
      retry: 0,
      body: {
        startDateTime: '2026-07-20T14:00:00',
        endDateTime: '2026-07-27T14:00:00'
      }
    })
  })

  it('normalizes RFC Problem Details while preserving extensions', async () => {
    const { spy, transport } = createTransportMock()
    spy.mockRejectedValue({
      response: { status: 409 },
      data: {
        type: 'https://duty-tracker.example/errors/oncall-period-overlap',
        title: 'On-call period overlap',
        detail: 'The selected period overlaps an existing period.',
        instance: '/api/v1/oncall-periods',
        correlationId: 'request-123'
      }
    })
    const client = createApiClient(transport)

    try {
      await client.post('/oncall-periods', {})
      expect.unreachable('The API request should reject')
    } catch (error) {
      expect(error).toBeInstanceOf(ApiProblem)
      expect(error).toMatchObject({
        name: 'ApiProblem',
        type: 'https://duty-tracker.example/errors/oncall-period-overlap',
        title: 'On-call period overlap',
        status: 409,
        detail: 'The selected period overlaps an existing period.',
        instance: '/api/v1/oncall-periods'
      })
      expect((error as ApiProblem).problem.correlationId).toBe('request-123')
    }
  })

  it('does not disguise non-Problem transport errors', async () => {
    const { spy, transport } = createTransportMock()
    const networkError = new Error('Network unavailable')
    spy.mockRejectedValue(networkError)
    const client = createApiClient(transport)

    await expect(client.get('/profile')).rejects.toBe(networkError)
  })
})
