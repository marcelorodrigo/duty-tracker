import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import { defineComponent } from 'vue'
import { flushPromises } from '@vue/test-utils'
import { useCompensationRates } from '~/composables/useCompensationRates'
import type { CompensationRateResponse } from '~/types/compensation'

const mockFetch = vi.fn()

const mockRates: CompensationRateResponse[] = [
  {
    id: 1,
    rateCategory: 'OVERTIME_ALLOWANCE',
    overtimeDayType: 'WEEKDAY',
    label: 'Mon-Fri 00:00',
    timeFrom: '00:00:00',
    timeTo: '01:00:00',
    percentage: 50
  },
  {
    id: 2,
    rateCategory: 'OVERTIME_ALLOWANCE',
    overtimeDayType: 'SATURDAY',
    label: 'Sat 00:00',
    timeFrom: '00:00:00',
    timeTo: '01:00:00',
    percentage: 50
  },
  {
    id: 3,
    rateCategory: 'OVERTIME_ALLOWANCE',
    overtimeDayType: 'SUNDAY_HOLIDAY',
    label: 'Sun/PH 00:00',
    timeFrom: '00:00:00',
    timeTo: '01:00:00',
    percentage: 100
  }
]

beforeEach(() => {
  vi.stubGlobal('$fetch', mockFetch)
  // Return deep copies so mutation in one test doesn't affect others
  mockFetch.mockResolvedValue({
    rates: mockRates.map(r => ({ ...r }))
  })
})

afterEach(() => {
  vi.unstubAllGlobals()
})

async function withComposable(): Promise<ReturnType<typeof useCompensationRates>> {
  let composable!: ReturnType<typeof useCompensationRates>

  await mountSuspended(defineComponent({
    setup() {
      composable = useCompensationRates()
      return () => null
    }
  }))

  await flushPromises()
  return composable
}

describe('useCompensationRates', () => {
  describe('initial state', () => {
    it('loads rates via useFetch and exposes them', async () => {
      const { data } = await withComposable()

      expect(data.value?.rates).toEqual(mockRates)
    })

    it('builds pivotRows from the loaded rates', async () => {
      const { pivotRows } = await withComposable()

      expect(pivotRows.value).toHaveLength(1)
      expect(pivotRows.value[0]!.slot).toBe('00:00–01:00')
      expect(pivotRows.value[0]!.weekday.id).toBe(1)
    })

    it('pivotRows is empty when data has no rates', async () => {
      const composable = await withComposable()
      composable.data.value = null

      expect(composable.pivotRows.value).toEqual([])
    })
  })

  describe('updateRate()', () => {
    it('applies the optimistic percentage update immediately', async () => {
      const composable = await withComposable()
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
      const composable = await withComposable()
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
      const composable = await withComposable()
      composable.data.value = { rates: mockRates.map(r => ({ ...r })) }
      mockFetch.mockRejectedValueOnce(new Error('Server error'))

      await composable.updateRate(1, 99)

      const rate = composable.data.value?.rates.find(r => r.id === 1)
      expect(rate?.percentage).toBe(50) // original value
    })

    it('is a no-op when data is null', async () => {
      const composable = await withComposable()
      composable.data.value = null
      mockFetch.mockClear()

      // Should not throw
      await composable.updateRate(1, 75)
      expect(mockFetch).not.toHaveBeenCalled()
    })

    it('is a no-op when the rate id does not exist', async () => {
      const composable = await withComposable()
      mockFetch.mockClear()

      // Should not throw and should not call $fetch
      await composable.updateRate(999, 75)

      expect(mockFetch).not.toHaveBeenCalled()
    })
  })
})
