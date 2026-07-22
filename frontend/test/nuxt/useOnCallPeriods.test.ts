import { describe, it, expect } from 'vitest'
import { withComposable } from '../utils/test-composable'
import { setupFetchMock } from '../utils/mock-fetch'
import { buildPeriod } from '../utils/factories'
import { useOnCallPeriods } from '~/composables/useOnCallPeriods'

const now = new Date()
const oneHourAgo = new Date(now.getTime() - 60 * 60 * 1000).toISOString()
const oneHourFromNow = new Date(now.getTime() + 60 * 60 * 1000).toISOString()
const twoHoursFromNow = new Date(now.getTime() + 2 * 60 * 60 * 1000).toISOString()

const activePeriod = buildPeriod({ id: 1, startDateTime: oneHourAgo, endDateTime: oneHourFromNow })
const scheduledPeriod = buildPeriod({ id: 2, startDateTime: oneHourFromNow, endDateTime: twoHoursFromNow })
const pastPeriod = buildPeriod({ id: 3, startDateTime: '2020-01-01T14:00:00', endDateTime: '2020-01-08T14:00:00', createdAt: '2020-01-01T00:00:00Z' })

const mockFetch = setupFetchMock({ periods: [] })

describe('useOnCallPeriods', () => {
  describe('initial state', () => {
    it('starts with null data, pending false, no error, modal closed', async () => {
      const composable = await withComposable(() => useOnCallPeriods())

      expect(composable.data.value).toBeNull()
      expect(composable.pending.value).toBe(false)
      expect(composable.error.value).toBeNull()
      expect(composable.deleteModalOpen.value).toBe(false)
      expect(composable.deletingPeriod.value).toBeNull()
    })
  })

  describe('refresh()', () => {
    it('populates periods on success', async () => {
      const composable = await withComposable(() => useOnCallPeriods())
      mockFetch.mockResolvedValueOnce({ periods: [activePeriod, pastPeriod] })

      await composable.refresh()

      expect(composable.data.value).toEqual([activePeriod, pastPeriod])
    })

    it('calls the correct endpoint', async () => {
      const composable = await withComposable(() => useOnCallPeriods())
      mockFetch.mockResolvedValueOnce({ periods: [] })

      await composable.refresh()

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/v1/oncall-periods',
        expect.objectContaining({ method: 'GET' })
      )
    })

    it('sets pending true during fetch and false after', async () => {
      const composable = await withComposable(() => useOnCallPeriods())
      mockFetch.mockResolvedValueOnce({ periods: [] })

      const fetchPromise = composable.refresh()
      expect(composable.pending.value).toBe(true)
      await fetchPromise
      expect(composable.pending.value).toBe(false)
    })

    it('sets error on failure', async () => {
      const composable = await withComposable(() => useOnCallPeriods())
      mockFetch.mockRejectedValueOnce(new Error('Network error'))

      await composable.refresh()

      expect(composable.error.value).toBeInstanceOf(Error)
    })

    it('wraps non-Error rejections', async () => {
      const composable = await withComposable(() => useOnCallPeriods())
      mockFetch.mockRejectedValueOnce('string error')

      await composable.refresh()

      expect(composable.error.value?.message).toBe('Failed to fetch periods')
    })

    it('clears error before re-fetching', async () => {
      const composable = await withComposable(() => useOnCallPeriods())
      mockFetch.mockRejectedValueOnce(new Error('first fail'))
      await composable.refresh()
      expect(composable.error.value).not.toBeNull()

      mockFetch.mockResolvedValueOnce({ periods: [] })
      await composable.refresh()
      expect(composable.error.value).toBeNull()
    })
  })

  describe('activePeriods computed', () => {
    it('includes active periods', async () => {
      const composable = await withComposable(() => useOnCallPeriods())
      composable.data.value = [activePeriod, pastPeriod]

      expect(composable.activePeriods.value).toContainEqual(activePeriod)
    })

    it('includes scheduled periods', async () => {
      const composable = await withComposable(() => useOnCallPeriods())
      composable.data.value = [scheduledPeriod, pastPeriod]

      expect(composable.activePeriods.value).toContainEqual(scheduledPeriod)
    })

    it('excludes past periods', async () => {
      const composable = await withComposable(() => useOnCallPeriods())
      composable.data.value = [activePeriod, pastPeriod]

      expect(composable.activePeriods.value).not.toContainEqual(pastPeriod)
    })

    it('is empty when all periods are past', async () => {
      const composable = await withComposable(() => useOnCallPeriods())
      composable.data.value = [pastPeriod]

      expect(composable.activePeriods.value).toHaveLength(0)
    })
  })

  describe('pastPeriods computed', () => {
    it('includes past periods', async () => {
      const composable = await withComposable(() => useOnCallPeriods())
      composable.data.value = [activePeriod, pastPeriod]

      expect(composable.pastPeriods.value).toContainEqual(pastPeriod)
    })

    it('excludes active and scheduled periods', async () => {
      const composable = await withComposable(() => useOnCallPeriods())
      composable.data.value = [activePeriod, scheduledPeriod, pastPeriod]

      expect(composable.pastPeriods.value).not.toContainEqual(activePeriod)
      expect(composable.pastPeriods.value).not.toContainEqual(scheduledPeriod)
    })

    it('is empty when there are no past periods', async () => {
      const composable = await withComposable(() => useOnCallPeriods())
      composable.data.value = [activePeriod, scheduledPeriod]

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
      mockFetch.mockResolvedValueOnce({ periods: [] }) // refresh

      await composable.remove(3)

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/v1/oncall-periods/3',
        expect.objectContaining({ method: 'DELETE' })
      )
    })

    it('refreshes periods and closes modal on success', async () => {
      const composable = await withComposable(() => useOnCallPeriods())
      composable.openDeleteModal(pastPeriod)
      mockFetch.mockResolvedValueOnce(undefined) // DELETE
      mockFetch.mockResolvedValueOnce({ periods: [] }) // refresh

      await composable.remove(3)

      expect(composable.deleteModalOpen.value).toBe(false)
    })

    it('does not close modal on failure', async () => {
      const composable = await withComposable(() => useOnCallPeriods())
      composable.openDeleteModal(pastPeriod)
      mockFetch.mockRejectedValueOnce(new Error('Server error'))

      await composable.remove(3)

      expect(composable.deleteModalOpen.value).toBe(true)
    })
  })
})
