import { describe, it, expect, vi } from 'vitest'
import { useCompensationRates } from '~/composables/useCompensationRates'
import { withComposable } from '../utils/test-composable'
import { setupFetchMock } from '../utils/mock-fetch'
import { buildCompensationRate } from '../utils/factories'
import type { CompensationRateResponse } from '~/types/compensation'

const mockRates: CompensationRateResponse[] = [
  buildCompensationRate({ id: 1, overtimeDayType: 'WEEKDAY', label: 'Mon-Fri 00:00' }),
  buildCompensationRate({ id: 2, overtimeDayType: 'SATURDAY', label: 'Sat 00:00' }),
  buildCompensationRate({ id: 3, overtimeDayType: 'SUNDAY_HOLIDAY', label: 'Sun/PH 00:00', percentage: 100 })
]

const mockFetch = setupFetchMock({ rates: mockRates.map(r => ({ ...r })) })

describe('useCompensationRates', () => {
  describe('initial state', () => {
    it('loads rates via the API client and exposes them', async () => {
      const { data } = await withComposable(() => useCompensationRates())

      expect(data.value?.rates).toEqual(mockRates)
    })

    it('builds pivotRows from the loaded rates', async () => {
      const { pivotRows } = await withComposable(() => useCompensationRates())

      expect(pivotRows.value).toHaveLength(1)
      expect(pivotRows.value[0]!.slot).toBe('00:00–01:00')
      expect(pivotRows.value[0]!.weekday.id).toBe(1)
    })

    it('pivotRows is empty when data has no rates', async () => {
      const composable = await withComposable(() => useCompensationRates())
      composable.data.value = undefined

      expect(composable.pivotRows.value).toEqual([])
    })
  })

  describe('updateRate()', () => {
    it('applies the optimistic percentage update immediately', async () => {
      const composable = await withComposable(() => useCompensationRates())
      // Ensure data is populated with fresh copies (guard against useFetch cache)
      composable.data.value = { rates: mockRates.map(r => ({ ...r })) }
      mockFetch.mockResolvedValueOnce(undefined) // PUT succeeds

      const updatePromise = composable.updateRate(1, 75)
      // Optimistic update happens synchronously before await
      const optimisticRate = composable.data.value?.rates.find(r => r.id === 1)
      expect(optimisticRate?.percentage).toBe(75)

      await updatePromise
    })

    it('calls PUT to the correct endpoint with the updated payload', async () => {
      const composable = await withComposable(() => useCompensationRates())
      composable.data.value = { rates: mockRates.map(r => ({ ...r })) }
      mockFetch.mockResolvedValueOnce(undefined)
      mockFetch.mockClear()

      await composable.updateRate(1, 75)

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/v1/compensation-rates/1',
        expect.objectContaining({
          method: 'PUT',
          body: expect.objectContaining({ rateId: 1, percentage: 75 })
        })
      )
    })

    it('rolls back the percentage to the original value on failure', async () => {
      const composable = await withComposable(() => useCompensationRates())
      composable.data.value = { rates: mockRates.map(r => ({ ...r })) }
      mockFetch.mockRejectedValueOnce(new Error('Server error'))

      await composable.updateRate(1, 99)

      const rate = composable.data.value?.rates.find(r => r.id === 1)
      expect(rate?.percentage).toBe(50) // original value
    })

    it('is a no-op when data is undefined', async () => {
      const composable = await withComposable(() => useCompensationRates())
      composable.data.value = undefined
      mockFetch.mockClear()

      // Should not throw
      await composable.updateRate(1, 75)
      expect(mockFetch).not.toHaveBeenCalled()
    })

    it('is a no-op when the rate id does not exist', async () => {
      const composable = await withComposable(() => useCompensationRates())
      mockFetch.mockClear()

      // Should not throw and should not call $fetch
      await composable.updateRate(999, 75)

      expect(mockFetch).not.toHaveBeenCalled()
    })
  })
})
