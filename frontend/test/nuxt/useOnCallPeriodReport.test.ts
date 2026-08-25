import { describe, it, expect, beforeEach } from 'vitest'
import { flushPromises } from '@vue/test-utils'
import { useOnCallPeriodReport } from '~/composables/useOnCallPeriodReport'
import { withComposable } from '../utils/test-composable'
import { setupFetchMock } from '../utils/mock-fetch'
import { buildReport } from '../utils/factories'

const mockReport = buildReport()

const mockFetch = setupFetchMock(mockReport)

describe('useOnCallPeriodReport', () => {
  beforeEach(() => {
    mockFetch.mockReset()
    mockFetch.mockResolvedValue(mockReport)
  })

  it('loads report via GET /api/v1/oncall-periods/{periodId}/report', async () => {
    const { report } = await withComposable(() => useOnCallPeriodReport(1))
    await flushPromises()

    expect(mockFetch).toHaveBeenCalledWith(
      '/api/v1/oncall-periods/1/report',
      expect.objectContaining({ baseURL: expect.any(String) })
    )
    expect(report.value).toEqual(mockReport)
  })

  it('calls the correct endpoint for the given periodId', async () => {
    await withComposable(() => useOnCallPeriodReport(42))
    await flushPromises()

    expect(mockFetch).toHaveBeenCalledWith(
      '/api/v1/oncall-periods/42/report',
      expect.objectContaining({ baseURL: expect.any(String) })
    )
  })

  it('sets pending to true while loading and false after', async () => {
    let resolveFetch!: (value: unknown) => void
    const deferred = new Promise<unknown>((resolve) => {
      resolveFetch = resolve
    })
    mockFetch.mockReturnValue(deferred)
    const { pending } = await withComposable(() => useOnCallPeriodReport(1))

    expect(pending.value).toBe(true)
    resolveFetch(mockReport)
    await flushPromises()
    expect(pending.value).toBe(false)
  })

  it('surfaces the error when the request fails', async () => {
    const err = new Error('Report generation failed')
    mockFetch.mockRejectedValue(err)
    const { error, report } = await withComposable(() => useOnCallPeriodReport(1))
    await flushPromises()

    expect(error.value).toBe(err)
    expect(report.value).toBeUndefined()
  })
})
