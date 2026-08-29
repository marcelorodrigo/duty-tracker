import { describe, it, expect, beforeEach } from 'vitest'
import { flushPromises } from '@vue/test-utils'
import { withComposable } from '../utils/test-composable'
import { setupFetchMock } from '../utils/mock-fetch'
import { mockNuxtImport } from '@nuxt/test-utils/runtime'
import { mockFetch } from '../utils/mock-ofetch'
import { buildPeriod } from '../utils/factories'
import { useOnCallPeriods } from '~/composables/useOnCallPeriods'

mockNuxtImport('$fetch', async () => {
  const { mockFetch } = await import('../utils/mock-ofetch')
  return mockFetch
})

const now = new Date()
const oneHourAgo = new Date(now.getTime() - 60 * 60 * 1000).toISOString()
const oneHourFromNow = new Date(now.getTime() + 60 * 60 * 1000).toISOString()
const twoHoursFromNow = new Date(now.getTime() + 2 * 60 * 60 * 1000).toISOString()

const activePeriod = buildPeriod({ id: 1, startDateTime: oneHourAgo, endDateTime: oneHourFromNow })
const scheduledPeriod = buildPeriod({ id: 2, startDateTime: oneHourFromNow, endDateTime: twoHoursFromNow })
const pastPeriod = buildPeriod({ id: 3, startDateTime: '2020-01-01T14:00:00', endDateTime: '2020-01-08T14:00:00', createdAt: '2020-01-01T00:00:00Z' })

setupFetchMock({ content: [] })

describe('useOnCallPeriods', () => {
  beforeEach(() => {
    mockFetch.mockReset()
    mockFetch.mockResolvedValue({ content: [] })
  })

  describe('initial state', () => {
    it('starts with empty periods, pending false, no error, modal closed', async () => {
      const composable = await withComposable(() => useOnCallPeriods())
      await flushPromises()

      expect(composable.periods.value).toEqual([])
      expect(composable.pending.value).toBe(false)
      expect(composable.error.value).toBeNull()
      expect(composable.deleteModalOpen.value).toBe(false)
      expect(composable.deletingPeriod.value).toBeNull()
    })
  })

  describe('list query', () => {
    it('fetches via GET /api/v1/oncall-periods', async () => {
      await withComposable(() => useOnCallPeriods())
      await flushPromises()

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/v1/oncall-periods',
        expect.objectContaining({ baseURL: expect.any(String) })
      )
    })

    it('populates periods on success', async () => {
      mockFetch.mockResolvedValue({ content: [activePeriod, pastPeriod] })
      const composable = await withComposable(() => useOnCallPeriods())
      await flushPromises()

      expect(composable.periods.value).toEqual([activePeriod, pastPeriod])
    })

    it('sets pending true while loading and false after', async () => {
      let resolveFetch!: (value: unknown) => void
      const deferred = new Promise<unknown>((resolve) => {
        resolveFetch = resolve
      })
      mockFetch.mockReturnValue(deferred)
      const { pending } = await withComposable(() => useOnCallPeriods())

      expect(pending.value).toBe(true)
      resolveFetch({ content: [] })
      await flushPromises()
      expect(pending.value).toBe(false)
    })

    it('surfaces the error when the request fails', async () => {
      mockFetch.mockRejectedValue(new Error('Network error'))
      const { error, periods } = await withComposable(() => useOnCallPeriods())
      await flushPromises()

      expect(error.value).toBeInstanceOf(Error)
      expect(periods.value).toEqual([])
    })
  })

  describe('activePeriods computed', () => {
    it('includes active periods', async () => {
      mockFetch.mockResolvedValue({ content: [activePeriod, pastPeriod] })
      const composable = await withComposable(() => useOnCallPeriods())
      await flushPromises()

      expect(composable.activePeriods.value).toContainEqual(activePeriod)
    })

    it('includes scheduled periods', async () => {
      mockFetch.mockResolvedValue({ content: [scheduledPeriod, pastPeriod] })
      const composable = await withComposable(() => useOnCallPeriods())
      await flushPromises()

      expect(composable.activePeriods.value).toContainEqual(scheduledPeriod)
    })

    it('excludes past periods', async () => {
      mockFetch.mockResolvedValue({ content: [activePeriod, pastPeriod] })
      const composable = await withComposable(() => useOnCallPeriods())
      await flushPromises()

      expect(composable.activePeriods.value).not.toContainEqual(pastPeriod)
    })

    it('is empty when all periods are past', async () => {
      mockFetch.mockResolvedValue({ content: [pastPeriod] })
      const composable = await withComposable(() => useOnCallPeriods())
      await flushPromises()

      expect(composable.activePeriods.value).toHaveLength(0)
    })
  })

  describe('pastPeriods computed', () => {
    it('includes past periods', async () => {
      mockFetch.mockResolvedValue({ content: [activePeriod, pastPeriod] })
      const composable = await withComposable(() => useOnCallPeriods())
      await flushPromises()

      expect(composable.pastPeriods.value).toContainEqual(pastPeriod)
    })

    it('excludes active and scheduled periods', async () => {
      mockFetch.mockResolvedValue({ content: [activePeriod, scheduledPeriod, pastPeriod] })
      const composable = await withComposable(() => useOnCallPeriods())
      await flushPromises()

      expect(composable.pastPeriods.value).not.toContainEqual(activePeriod)
      expect(composable.pastPeriods.value).not.toContainEqual(scheduledPeriod)
    })

    it('is empty when there are no past periods', async () => {
      mockFetch.mockResolvedValue({ content: [activePeriod, scheduledPeriod] })
      const composable = await withComposable(() => useOnCallPeriods())
      await flushPromises()

      expect(composable.pastPeriods.value).toHaveLength(0)
    })
  })

  describe('delete modal state', () => {
    it('openDeleteModal sets deletingPeriod and opens modal', async () => {
      const { openDeleteModal, deleteModalOpen, deletingPeriod } = await withComposable(() => useOnCallPeriods())

      openDeleteModal(pastPeriod)

      expect(deleteModalOpen.value).toBe(true)
      expect(deletingPeriod.value).toEqual(pastPeriod)
    })

    it('closeDeleteModal closes modal and clears deletingPeriod', async () => {
      const composable = await withComposable(() => useOnCallPeriods())
      composable.openDeleteModal(pastPeriod)

      composable.closeDeleteModal()

      expect(composable.deleteModalOpen.value).toBe(false)
      expect(composable.deletingPeriod.value).toBeNull()
    })
  })

  describe('remove()', () => {
    it('calls DELETE to the correct endpoint', async () => {
      const composable = await withComposable(() => useOnCallPeriods())
      mockFetch.mockResolvedValueOnce(undefined) // DELETE
      mockFetch.mockResolvedValueOnce({ content: [] }) // refetch after invalidation

      await composable.remove(3)
      await flushPromises()

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/v1/oncall-periods/3',
        expect.objectContaining({ method: 'DELETE' })
      )
    })

    it('refreshes periods and closes modal on success', async () => {
      const composable = await withComposable(() => useOnCallPeriods())
      composable.openDeleteModal(pastPeriod)
      mockFetch.mockResolvedValueOnce(undefined) // DELETE
      mockFetch.mockResolvedValueOnce({ content: [] }) // refetch after invalidation

      await composable.remove(3)
      await flushPromises()

      expect(composable.deleteModalOpen.value).toBe(false)
    })

    it('does not close modal on failure', async () => {
      const composable = await withComposable(() => useOnCallPeriods())
      composable.openDeleteModal(pastPeriod)
      mockFetch.mockRejectedValueOnce(new Error('Server error'))

      await composable.remove(3)
      await flushPromises()

      expect(composable.deleteModalOpen.value).toBe(true)
    })
  })

  describe('auto-loading past periods', () => {
    it('keeps loading pages until a past period is found', async () => {
      mockFetch.mockImplementation((_url: string, opts: { params?: { page?: number } }) => {
        const pageNum = opts?.params?.page ?? 0
        if (pageNum === 0) {
          return Promise.resolve({
            content: [activePeriod, scheduledPeriod],
            page: 0,
            size: 20,
            totalElements: 3,
            totalPages: 2
          })
        }
        return Promise.resolve({
          content: [pastPeriod],
          page: 1,
          size: 20,
          totalElements: 3,
          totalPages: 2
        })
      })

      const composable = await withComposable(() => useOnCallPeriods())
      for (let i = 0; i < 5; i++) {
        await flushPromises()
      }

      expect(composable.pastPeriods.value).toContainEqual(pastPeriod)
      expect(composable.periods.value).toHaveLength(3)
    })

    it('stops loading when no past period exists across all pages', async () => {
      mockFetch.mockImplementation((_url: string, opts: { params?: { page?: number } }) => {
        const pageNum = opts?.params?.page ?? 0
        if (pageNum === 0) {
          return Promise.resolve({ content: [activePeriod], page: 0, size: 20, totalElements: 2, totalPages: 2 })
        }
        return Promise.resolve({ content: [scheduledPeriod], page: 1, size: 20, totalElements: 2, totalPages: 2 })
      })

      const composable = await withComposable(() => useOnCallPeriods())
      for (let i = 0; i < 5; i++) {
        await flushPromises()
      }

      expect(composable.pastPeriods.value).toHaveLength(0)
      expect(composable.periods.value).toHaveLength(2)
    })
  })
})
