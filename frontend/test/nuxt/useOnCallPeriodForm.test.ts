import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import { defineComponent } from 'vue'
import { flushPromises } from '@vue/test-utils'
import { CalendarDate } from '@internationalized/date'
import { useOnCallPeriodForm } from '~/composables/useOnCallPeriodForm'
import type { OnCallPeriodResponse } from '~/types/onCallPeriod'

// ---------------------------------------------------------------------------
// Stub $fetch (Nuxt global) before each test.
// useRouter and useRuntimeConfig are left as the real Nuxt implementations —
// mocking useRouter breaks @nuxt/test-utils internals that also call it.
// ---------------------------------------------------------------------------

const mockFetch = vi.fn()

beforeEach(() => {
  vi.stubGlobal('$fetch', mockFetch)
  mockFetch.mockResolvedValue([])
})

afterEach(() => {
  vi.unstubAllGlobals()
})

// ---------------------------------------------------------------------------
// Helper: run the composable inside a mounted Vue component so that
// Vue reactivity and Nuxt auto-imports are fully initialised.
// ---------------------------------------------------------------------------

async function withComposable(
  mode: 'create' | 'edit',
  existingPeriod?: OnCallPeriodResponse
): Promise<ReturnType<typeof useOnCallPeriodForm>> {
  let form!: ReturnType<typeof useOnCallPeriodForm>

  await mountSuspended(defineComponent({
    setup() {
      form = useOnCallPeriodForm(mode, existingPeriod)
      return () => null
    }
  }))

  // Flush any pending $fetch promises so holiday state is settled
  await flushPromises()

  return form
}

// ---------------------------------------------------------------------------
// Shared fixture — an edit-mode period covering all of April 2026
// ---------------------------------------------------------------------------

const editPeriod: OnCallPeriodResponse = {
  id: 42,
  startDateTime: '2026-04-01T14:00:00',
  endDateTime: '2026-04-30T14:00:00',
  holidays: [],
  createdAt: '2026-01-01T00:00:00Z'
}

// ===========================================================================
// addCustomHoliday
// ===========================================================================

describe('addCustomHoliday', () => {
  it('sets an error when no custom date is selected', async () => {
    const form = await withComposable('edit', editPeriod)

    form.customHolidayDate.value = undefined
    form.addCustomHoliday()

    expect(form.customHolidayError.value).toBe('Date is required.')
  })

  it('sets an error when the period date range has no start date', async () => {
    const form = await withComposable('edit', editPeriod)

    form.customHolidayDate.value = new CalendarDate(2026, 4, 15)
    form.dateRange.value = { start: undefined, end: new CalendarDate(2026, 4, 30) }
    form.addCustomHoliday()

    expect(form.customHolidayError.value).toBe('Please select a period date range first.')
  })

  it('sets an error when the custom date is before the period start', async () => {
    const form = await withComposable('edit', editPeriod)

    form.customHolidayDate.value = new CalendarDate(2026, 3, 31) // March 31 — before April
    form.addCustomHoliday()

    expect(form.customHolidayError.value).toBe('Date must be within the on-call period.')
  })

  it('sets an error when the custom date is after the period end', async () => {
    const form = await withComposable('edit', editPeriod)

    form.customHolidayDate.value = new CalendarDate(2026, 5, 1) // May 1 — after April 30
    form.addCustomHoliday()

    expect(form.customHolidayError.value).toBe('Date must be within the on-call period.')
  })

  it('sets an error when a holiday on that date already exists', async () => {
    const form = await withComposable('edit', {
      ...editPeriod,
      holidays: [{ date: '2026-04-27', name: 'Koningsdag' }]
    })

    form.customHolidayDate.value = new CalendarDate(2026, 4, 27)
    form.addCustomHoliday()

    expect(form.customHolidayError.value).toBe('A holiday on this date already exists.')
  })

  it('adds the holiday when all conditions are met', async () => {
    const form = await withComposable('edit', editPeriod)

    form.customHolidayDate.value = new CalendarDate(2026, 4, 15)
    form.customHolidayName.value = 'Team day'
    form.addCustomHoliday()

    expect(form.holidays.value).toContainEqual({ date: '2026-04-15', name: 'Team day' })
  })

  it('clears the date and name inputs after a successful add', async () => {
    const form = await withComposable('edit', editPeriod)

    form.customHolidayDate.value = new CalendarDate(2026, 4, 15)
    form.customHolidayName.value = 'Team day'
    form.addCustomHoliday()

    expect(form.customHolidayDate.value).toBeUndefined()
    expect(form.customHolidayName.value).toBe('')
  })

  it('keeps the holiday list sorted by date ascending after adding', async () => {
    const form = await withComposable('edit', {
      ...editPeriod,
      holidays: [{ date: '2026-04-27', name: 'Koningsdag' }]
    })

    form.customHolidayDate.value = new CalendarDate(2026, 4, 6)
    form.customHolidayName.value = 'Tweede Paasdag'
    form.addCustomHoliday()

    expect(form.holidays.value[0].date).toBe('2026-04-06')
    expect(form.holidays.value[1].date).toBe('2026-04-27')
  })

  it('clears a previous error when a new attempt is made', async () => {
    const form = await withComposable('edit', editPeriod)

    // First call — triggers an error
    form.customHolidayDate.value = undefined
    form.addCustomHoliday()
    expect(form.customHolidayError.value).not.toBeNull()

    // Second call — error is cleared at the start of the function
    form.customHolidayDate.value = new CalendarDate(2026, 4, 15)
    form.addCustomHoliday()
    expect(form.customHolidayError.value).toBeNull()
  })
})

// ===========================================================================
// removeHoliday
// ===========================================================================

describe('removeHoliday', () => {
  it('removes the holiday with the given date', async () => {
    const form = await withComposable('edit', {
      ...editPeriod,
      holidays: [
        { date: '2026-04-06', name: 'Tweede Paasdag' },
        { date: '2026-04-27', name: 'Koningsdag' }
      ]
    })

    form.removeHoliday('2026-04-06')

    expect(form.holidays.value).toHaveLength(1)
    expect(form.holidays.value[0].date).toBe('2026-04-27')
  })

  it('is a no-op when the given date does not exist in the list', async () => {
    const form = await withComposable('edit', {
      ...editPeriod,
      holidays: [{ date: '2026-04-27', name: 'Koningsdag' }]
    })

    form.removeHoliday('2026-05-01')

    expect(form.holidays.value).toHaveLength(1)
  })
})

// ===========================================================================
// validateForm — tested indirectly via save(), which sets error.value
// ===========================================================================

describe('validateForm (via save)', () => {
  it('sets error when start date is missing', async () => {
    const form = await withComposable('edit', editPeriod)

    form.dateRange.value = { start: undefined, end: new CalendarDate(2026, 4, 30) }
    await form.save()

    expect(form.error.value).toBe('Please select a start date.')
  })

  it('sets error when end date is missing', async () => {
    const form = await withComposable('edit', editPeriod)

    form.dateRange.value = { start: new CalendarDate(2026, 4, 1), end: undefined }
    await form.save()

    expect(form.error.value).toBe('Please select an end date.')
  })

  it('sets error for an invalid start time', async () => {
    const form = await withComposable('edit', editPeriod)

    form.startTime.value = 'bad'
    await form.save()

    expect(form.error.value).toBe('Please enter a valid start time.')
  })

  it('sets error for an invalid end time', async () => {
    const form = await withComposable('edit', editPeriod)

    form.endTime.value = '25:00'
    await form.save()

    expect(form.error.value).toBe('Please enter a valid end time.')
  })

  it('sets error when end datetime is not after start', async () => {
    // Initialise with reversed dates so endDateTime < startDateTime
    const form = await withComposable('edit', {
      ...editPeriod,
      startDateTime: '2026-04-30T14:00:00',
      endDateTime: '2026-04-01T14:00:00'
    })

    await form.save()

    expect(form.error.value).toBe('End date and time must be after start.')
  })
})
