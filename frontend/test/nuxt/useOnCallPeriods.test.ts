import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import { defineComponent, nextTick } from 'vue'
import { flushPromises } from '@vue/test-utils'
import { useOnCallPeriods } from '~/composables/useOnCallPeriods'
import type { OnCallPeriodResponse } from '~/types/onCallPeriod'

const mockFetch = vi.fn()

beforeEach(() => {
  vi.stubGlobal('$fetch', mockFetch)
  mockFetch.mockResolvedValue({ periods: [] })
})

afterEach(() => {
  vi.unstubAllGlobals()
})

async function withComposable(): Promise<ReturnType<typeof useOnCallPeriods>> {
  let composable!: ReturnType<typeof useOnCallPeriods>

  await mountSuspended(defineComponent({
    setup() {
      composable = useOnCallPeriods()
      return () => null
    }
  }))

  await flushPromises()
  return composable
}

const now = new Date()
const oneHourAgo = new Date(now.getTime() - 60 * 60 * 1000).toISOString()
const oneHourFromNow = new Date(now.getTime() + 60 * 60 * 1000).toISOString()
const twoHoursFromNow = new Date(now.getTime() + 2 * 60 * 60 * 1000).toISOString()

const activePeriod: OnCallPeriodResponse = {
  id: 1,
  startDateTime: oneHourAgo,
  endDateTime: oneHourFromNow,
  holidays: [],
  createdAt: '2026-01-01T00:00:00Z'
}

const scheduledPeriod: OnCallPeriodResponse = {
  id: 2,
  startDateTime: oneHourFromNow,
  endDateTime: twoHoursFromNow,
  holidays: [],
  createdAt: '2026-01-01T00:00:00Z'
}

const pastPeriod: OnCallPeriodResponse = {
  id: 3,
  startDateTime: '2020-01-01T14:00:00',
  endDateTime: '2020-01-08T14:00:00',
  holidays: [],
  createdAt: '2020-01-01T00:00:00Z'
}

describe('useOnCallPeriods', () => {
  describe('initial state', () => {
    it('starts with empty periods, pending false, no error, modal closed', async () => {
      const composable = await withComposable()

      expect(composable.periods.value).toEqual([])
      expect(composable.pending.value).toBe(false)
      expect(composable.error.value).toBeNull()
      expect(composable.deleteModalOpen.value).toBe(false)
      expect(composable.deletingPeriod.value).toBeNull()
    })
  })

  describe('fetchPeriods()', () => {
    it('populates periods on success', async () => {
      const composable = await withComposable()
      mockFetch.mockResolvedValueOnce({ periods: [activePeriod, pastPeriod] })

      await composable.fetchPeriods()

      expect(composable.periods.value).toEqual([activePeriod, pastPeriod])
    })

    it('calls the correct endpoint', async () => {
      const composable = await withComposable()
      mockFetch.mockResolvedValueOnce({ periods: [] })

      await composable.fetchPeriods()

      expect(mockFetch).toHaveBeenCalledWith('/api/v1/oncall-periods', expect.any(Object))
    })

    it('sets pending true during fetch and false after', async () => {
      const composable = await withComposable()
      mockFetch.mockResolvedValueOnce({ periods: [] })

      const fetchPromise = composable.fetchPeriods()
      expect(composable.pending.value).toBe(true)
      await fetchPromise
      expect(composable.pending.value).toBe(false)
    })

    it('sets error on failure', async () => {
      const composable = await withComposable()
      mockFetch.mockRejectedValueOnce(new Error('Network error'))

      await composable.fetchPeriods()

      expect(composable.error.value).toBeInstanceOf(Error)
    })

    it('wraps non-Error rejections', async () => {
      const composable = await withComposable()
      mockFetch.mockRejectedValueOnce('string error')

      await composable.fetchPeriods()

      expect(composable.error.value?.message).toBe('Failed to fetch periods')
    })

    it('clears error before re-fetching', async () => {
      const composable = await withComposable()
      mockFetch.mockRejectedValueOnce(new Error('first fail'))
      await composable.fetchPeriods()
      expect(composable.error.value).not.toBeNull()

      mockFetch.mockResolvedValueOnce({ periods: [] })
      await composable.fetchPeriods()
      expect(composable.error.value).toBeNull()
    })
  })

  describe('activePeriods computed', () => {
    it('includes active periods', async () => {
      const composable = await withComposable()
      composable.periods.value = [activePeriod, pastPeriod]

      expect(composable.activePeriods.value).toContainEqual(activePeriod)
    })

    it('includes scheduled periods', async () => {
      const composable = await withComposable()
      composable.periods.value = [scheduledPeriod, pastPeriod]

      expect(composable.activePeriods.value).toContainEqual(scheduledPeriod)
    })

    it('excludes past periods', async () => {
      const composable = await withComposable()
      composable.periods.value = [activePeriod, pastPeriod]

      expect(composable.activePeriods.value).not.toContainEqual(pastPeriod)
    })

    it('is empty when all periods are past', async () => {
      const composable = await withComposable()
      composable.periods.value = [pastPeriod]

      expect(composable.activePeriods.value).toHaveLength(0)
    })
  })

  describe('pastPeriods computed', () => {
    it('includes past periods', async () => {
      const composable = await withComposable()
      composable.periods.value = [activePeriod, pastPeriod]

      expect(composable.pastPeriods.value).toContainEqual(pastPeriod)
    })

    it('excludes active and scheduled periods', async () => {
      const composable = await withComposable()
      composable.periods.value = [activePeriod, scheduledPeriod, pastPeriod]

      expect(composable.pastPeriods.value).not.toContainEqual(activePeriod)
      expect(composable.pastPeriods.value).not.toContainEqual(scheduledPeriod)
    })

    it('is empty when there are no past periods', async () => {
      const composable = await withComposable()
      composable.periods.value = [activePeriod, scheduledPeriod]

      expect(composable.pastPeriods.value).toHaveLength(0)
    })
  })

  describe('delete modal state', () => {
    it('openDeleteModal sets deletingPeriod and opens modal', async () => {
      const { openDeleteModal, deleteModalOpen, deletingPeriod } = await withComposable()

      openDeleteModal(pastPeriod)

      expect(deleteModalOpen.value).toBe(true)
      expect(deletingPeriod.value).toEqual(pastPeriod)
    })

    it('closeDeleteModal closes modal and clears deletingPeriod', async () => {
      const composable = await withComposable()
      composable.openDeleteModal(pastPeriod)

      composable.closeDeleteModal()

      expect(composable.deleteModalOpen.value).toBe(false)
      expect(composable.deletingPeriod.value).toBeNull()
    })
  })

  describe('remove()', () => {
    it('calls DELETE to the correct endpoint', async () => {
      const composable = await withComposable()
      mockFetch.mockResolvedValueOnce(undefined) // DELETE
      mockFetch.mockResolvedValueOnce({ periods: [] }) // fetchPeriods

      await composable.remove(3)

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/v1/oncall-periods/3',
        expect.objectContaining({ method: 'DELETE' })
      )
    })

    it('refreshes periods and closes modal on success', async () => {
      const composable = await withComposable()
      composable.openDeleteModal(pastPeriod)
      mockFetch.mockResolvedValueOnce(undefined) // DELETE
      mockFetch.mockResolvedValueOnce({ periods: [] }) // fetchPeriods

      await composable.remove(3)

      expect(composable.deleteModalOpen.value).toBe(false)
    })

    it('does not close modal on failure', async () => {
      const composable = await withComposable()
      composable.openDeleteModal(pastPeriod)
      mockFetch.mockRejectedValueOnce(new Error('Server error'))

      await composable.remove(3)

      expect(composable.deleteModalOpen.value).toBe(true)
    })
  })
})
