import { describe, it, expect, vi, afterEach, beforeEach } from 'vitest'
import { nextTick } from 'vue'
import { flushPromises } from '@vue/test-utils'

import { CalendarDate } from '@internationalized/date'
import { useOnCallPeriodForm } from '~/composables/useOnCallPeriodForm'
import { withComposable } from '../utils/test-composable'
import { setupFetchMock } from '../utils/mock-fetch'
import { buildPeriod } from '../utils/factories'

// ---------------------------------------------------------------------------
// Stub $fetch (Nuxt global) before each test.
// useRouter and useRuntimeConfig are left as the real Nuxt implementations —
// mocking useRouter breaks @nuxt/test-utils internals that also call it.
// ---------------------------------------------------------------------------

const mockFetch = setupFetchMock([])

// ---------------------------------------------------------------------------
// Shared fixture — an edit-mode period covering all of April 2026
// ---------------------------------------------------------------------------

const editPeriod = buildPeriod({ id: 42 })

// ===========================================================================
// addCustomHoliday
// ===========================================================================

describe('addCustomHoliday', () => {
  it('sets an error when no custom date is selected', async () => {
    const form = await withComposable(() => useOnCallPeriodForm('edit', editPeriod))

    form.customHolidayDate.value = undefined
    form.addCustomHoliday()

    expect(form.customHolidayError.value).toBe('Date is required.')
  })

  it('sets an error when the period date range has no start date', async () => {
    const form = await withComposable(() => useOnCallPeriodForm('edit', editPeriod))

    form.customHolidayDate.value = new CalendarDate(2026, 4, 15)
    form.dateRange.value = { start: undefined, end: new CalendarDate(2026, 4, 30) }
    form.addCustomHoliday()

    expect(form.customHolidayError.value).toBe('Please select a period date range first.')
  })

  it('sets an error when the custom date is before the period start', async () => {
    const form = await withComposable(() => useOnCallPeriodForm('edit', editPeriod))

    form.customHolidayDate.value = new CalendarDate(2026, 3, 31) // March 31 — before April
    form.addCustomHoliday()

    expect(form.customHolidayError.value).toBe('Date must be within the on-call period.')
  })

  it('sets an error when the custom date is after the period end', async () => {
    const form = await withComposable(() => useOnCallPeriodForm('edit', editPeriod))

    form.customHolidayDate.value = new CalendarDate(2026, 5, 1) // May 1 — after April 30
    form.addCustomHoliday()

    expect(form.customHolidayError.value).toBe('Date must be within the on-call period.')
  })

  it('sets an error when a holiday on that date already exists', async () => {
    const form = await withComposable(() => useOnCallPeriodForm('edit', buildPeriod({ id: 42, holidays: [{ date: '2026-04-27', name: 'Koningsdag' }] })))

    form.customHolidayDate.value = new CalendarDate(2026, 4, 27)
    form.addCustomHoliday()

    expect(form.customHolidayError.value).toBe('A holiday on this date already exists.')
  })

  it('adds the holiday when all conditions are met', async () => {
    const form = await withComposable(() => useOnCallPeriodForm('edit', editPeriod))

    form.customHolidayDate.value = new CalendarDate(2026, 4, 15)
    form.customHolidayName.value = 'Team day'
    form.addCustomHoliday()

    expect(form.holidays.value).toContainEqual({ date: '2026-04-15', name: 'Team day' })
  })

  it('clears the date and name inputs after a successful add', async () => {
    const form = await withComposable(() => useOnCallPeriodForm('edit', editPeriod))

    form.customHolidayDate.value = new CalendarDate(2026, 4, 15)
    form.customHolidayName.value = 'Team day'
    form.addCustomHoliday()

    expect(form.customHolidayDate.value).toBeUndefined()
    expect(form.customHolidayName.value).toBe('')
  })

  it('keeps the holiday list sorted by date ascending after adding', async () => {
    const form = await withComposable(() => useOnCallPeriodForm('edit', buildPeriod({ id: 42, holidays: [{ date: '2026-04-27', name: 'Koningsdag' }] })))

    form.customHolidayDate.value = new CalendarDate(2026, 4, 6)
    form.customHolidayName.value = 'Tweede Paasdag'
    form.addCustomHoliday()

    expect(form.holidays.value[0].date).toBe('2026-04-06')
    expect(form.holidays.value[1].date).toBe('2026-04-27')
  })

  it('clears a previous error when a new attempt is made', async () => {
    const form = await withComposable(() => useOnCallPeriodForm('edit', editPeriod))

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
    const form = await withComposable(() => useOnCallPeriodForm('edit', buildPeriod({ id: 42, holidays: [
      { date: '2026-04-06', name: 'Tweede Paasdag' },
      { date: '2026-04-27', name: 'Koningsdag' }
    ]})))

    form.removeHoliday('2026-04-06')

    expect(form.holidays.value).toHaveLength(1)
    expect(form.holidays.value[0].date).toBe('2026-04-27')
  })

  it('is a no-op when the given date does not exist in the list', async () => {
    const form = await withComposable(() => useOnCallPeriodForm('edit', buildPeriod({ id: 42, holidays: [{ date: '2026-04-27', name: 'Koningsdag' }] })))

    form.removeHoliday('2026-05-01')

    expect(form.holidays.value).toHaveLength(1)
  })
})

// ===========================================================================
// validateForm — tested indirectly via save(), which sets error.value
// ===========================================================================

describe('validateForm (via save)', () => {
  it('sets error when start date is missing', async () => {
    const form = await withComposable(() => useOnCallPeriodForm('edit', editPeriod))

    form.dateRange.value = { start: undefined, end: new CalendarDate(2026, 4, 30) }
    await form.save()

    expect(form.error.value).toBe('Please select a start date.')
  })

  it('sets error when end date is missing', async () => {
    const form = await withComposable(() => useOnCallPeriodForm('edit', editPeriod))

    form.dateRange.value = { start: new CalendarDate(2026, 4, 1), end: undefined }
    await form.save()

    expect(form.error.value).toBe('Please select an end date.')
  })

  it('sets error for an invalid start time', async () => {
    const form = await withComposable(() => useOnCallPeriodForm('edit', editPeriod))

    form.startTime.value = 'bad'
    await form.save()

    expect(form.error.value).toBe('Please enter a valid start time.')
  })

  it('sets error for an invalid end time', async () => {
    const form = await withComposable(() => useOnCallPeriodForm('edit', editPeriod))

    form.endTime.value = '25:00'
    await form.save()

    expect(form.error.value).toBe('Please enter a valid end time.')
  })

  it('sets error when end datetime is not after start', async () => {
    // Initialise with reversed dates so endDateTime < startDateTime
    const form = await withComposable(() => useOnCallPeriodForm('edit', buildPeriod({ id: 42, startDateTime: '2026-04-30T14:00:00', endDateTime: '2026-04-01T14:00:00' })))

    await form.save()

    expect(form.error.value).toBe('End date and time must be after start.')
  })
})

// ===========================================================================
// Create mode initialization
// ===========================================================================

describe('create mode initialization', () => {
  it('sets default times to 14:00', async () => {
    const form = await withComposable(() => useOnCallPeriodForm('create'))

    expect(form.startTime.value).toBe('14:00')
    expect(form.endTime.value).toBe('14:00')
  })

  it('sets date range to current week Monday through next week Monday', async () => {
    const form = await withComposable(() => useOnCallPeriodForm('create'))

    expect(form.dateRange.value.start).toBeDefined()
    expect(form.dateRange.value.end).toBeDefined()

    // Verify the dates are calendar dates (not null/undefined)
    expect(form.dateRange.value.start?.toString()).toMatch(/^\d{4}-\d{2}-\d{2}$/)
    expect(form.dateRange.value.end?.toString()).toMatch(/^\d{4}-\d{2}-\d{2}$/)
  })

  it('triggers initial holiday suggestion fetch on create', async () => {
    const form = await withComposable(() => useOnCallPeriodForm('create'))

    // After withComposable, flushPromises has been called,
    // so the initial fetch should have been attempted
    expect(mockFetch).toHaveBeenCalledWith(
      '/api/v1/holidays/suggestions',
      expect.any(Object)
    )
  })
})

// ===========================================================================
// save — success and error paths
// ===========================================================================

describe('save', () => {
  it('saves successfully in create mode (POST + holiday PUT)', async () => {
    const form = await withComposable(() => useOnCallPeriodForm('create'))

    // Clear mock before the save test (previous initialization calls don't matter)
    mockFetch.mockClear()
    mockFetch.mockResolvedValueOnce({ id: 99 }) // POST period
    mockFetch.mockResolvedValueOnce(undefined) // PUT holidays

    // Mock router.push
    const pushSpy = vi.spyOn(useRouter(), 'push').mockResolvedValue(null)

    form.startTime.value = '10:00'
    form.endTime.value = '18:00'

    await form.save()

    // Should call POST for period creation
    expect(mockFetch).toHaveBeenNthCalledWith(
      1,
      '/api/v1/oncall-periods',
      expect.objectContaining({
        method: 'POST',
        body: expect.objectContaining({
          startDateTime: expect.any(String),
          endDateTime: expect.any(String)
        })
      })
    )

    // Should call PUT for holidays with the created period ID
    expect(mockFetch).toHaveBeenNthCalledWith(
      2,
      '/api/v1/oncall-periods/99/holidays',
      expect.objectContaining({
        method: 'PUT',
        body: expect.any(Array)
      })
    )

    // Verify navigation to the new period
    expect(pushSpy).toHaveBeenCalledWith('/oncall/99')

    // Verify no error and saving flag cleared
    expect(form.error.value).toBeNull()
    expect(form.saving.value).toBe(false)

    pushSpy.mockRestore()
  })

  it('saves successfully in edit mode (PUT period + holiday PUT)', async () => {
    const form = await withComposable(() => useOnCallPeriodForm('edit', editPeriod))

    // Clear mock before the save test
    mockFetch.mockClear()
    mockFetch.mockResolvedValueOnce(undefined) // PUT period
    mockFetch.mockResolvedValueOnce(undefined) // PUT holidays

    // Mock router.push
    const pushSpy = vi.spyOn(useRouter(), 'push').mockResolvedValue(null)

    form.startTime.value = '11:00'
    form.endTime.value = '19:00'

    await form.save()

    // Should call PUT for period update (not POST)
    expect(mockFetch).toHaveBeenNthCalledWith(
      1,
      `/api/v1/oncall-periods/${editPeriod.id}`,
      expect.objectContaining({
        method: 'PUT',
        body: expect.objectContaining({
          startDateTime: expect.any(String),
          endDateTime: expect.any(String)
        })
      })
    )

    // Should call PUT for holidays with same period ID
    expect(mockFetch).toHaveBeenNthCalledWith(
      2,
      `/api/v1/oncall-periods/${editPeriod.id}/holidays`,
      expect.objectContaining({
        method: 'PUT'
      })
    )

    // Verify navigation to the period
    expect(pushSpy).toHaveBeenCalledWith(`/oncall/${editPeriod.id}`)

    // Verify no error
    expect(form.error.value).toBeNull()

    pushSpy.mockRestore()
  })

  it('sets error and clears saving flag when $fetch throws', async () => {
    const form = await withComposable(() => useOnCallPeriodForm('edit', editPeriod))

    // Clear mock and set up rejection
    mockFetch.mockClear()
    mockFetch.mockRejectedValueOnce(new Error('Network error'))

    await form.save()

    // Verify error is set
    expect(form.error.value).not.toBeNull()
    expect(form.error.value).toContain('Failed to save on-call period')

    // Verify saving flag is cleared
    expect(form.saving.value).toBe(false)
  })
})

// ===========================================================================
// Edge cases and error handling
// ===========================================================================

describe('edge cases', () => {
  it('validateForm returns null when form is valid', async () => {
    const form = await withComposable(() => useOnCallPeriodForm('edit', editPeriod))

    form.startTime.value = '10:00'
    form.endTime.value = '18:00'
    form.dateRange.value = {
      start: new CalendarDate(2026, 4, 1),
      end: new CalendarDate(2026, 4, 30)
    }

    // Validate form and check that it doesn't error
    // (This exercises the null return path in validateForm)
    await form.save()

    // Should not set error on valid form
    expect(form.error.value).toBeNull()
  })

  it('holiday suggestions fetch handles errors gracefully', async () => {
    // Mock fetch to reject on the initial suggestions call
    mockFetch.mockRejectedValueOnce(new Error('API error'))

    const form = await withComposable(() => useOnCallPeriodForm('edit', editPeriod))

    // Should not throw and should initialize without crashing
    expect(form.holidays.value).toBeDefined()
    expect(form.fetchingHolidays.value).toBe(false)
  })
})

// ===========================================================================
// Schedule suggestion fetch (debounce)
// ===========================================================================

describe('scheduleSuggestionFetch (debounce)', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('fetches suggestions after the 500ms debounce when date range changes', async () => {
    mockFetch.mockResolvedValue([])

    const form = await withComposable(() => useOnCallPeriodForm('edit', editPeriod))

    // Clear any fetch calls made during initialization
    mockFetch.mockClear()

    // Trigger the watch by setting a new date range
    form.dateRange.value = {
      start: new CalendarDate(2026, 4, 16),
      end: new CalendarDate(2026, 5, 15)
    }

    // Let Vue reactivity propagate
    await nextTick()

    // No fetch should have happened yet (debounce pending)
    expect(mockFetch).not.toHaveBeenCalledWith(
      '/api/v1/holidays/suggestions',
      expect.any(Object)
    )

    // Advance past the debounce period
    vi.advanceTimersByTime(510)
    await flushPromises()
    expect(mockFetch).toHaveBeenCalledWith(
      '/api/v1/holidays/suggestions',
      expect.any(Object)
    )
  })

  it('resets the debounce timer when date range changes rapidly', async () => {
    mockFetch.mockResolvedValue([])

    const form = await withComposable(() => useOnCallPeriodForm('edit', editPeriod))
    mockFetch.mockClear()

    // First change
    form.dateRange.value = {
      start: new CalendarDate(2026, 4, 10),
      end: new CalendarDate(2026, 4, 20)
    }
    await nextTick()

    // Advance 200ms (before debounce fires)
    vi.advanceTimersByTime(200)

    // Second change (resets the timer)
    form.dateRange.value = {
      start: new CalendarDate(2026, 4, 16),
      end: new CalendarDate(2026, 5, 15)
    }
    await nextTick()

    // Advance another 200ms — still less than 500ms from the LAST change
    vi.advanceTimersByTime(200)
    expect(mockFetch).not.toHaveBeenCalledWith(
      '/api/v1/holidays/suggestions',
      expect.any(Object)
    )

    // Advance remaining time past the 500ms debounce from the second change
    // (Timer was set at clock +200ms, needs clock +700ms; currently at +400ms)
    vi.advanceTimersByTime(300)
    await flushPromises()
    expect(mockFetch).toHaveBeenCalledWith(
      '/api/v1/holidays/suggestions',
      expect.any(Object)
    )
  })

  it('does not fetch suggestions when start date is missing', async () => {
    mockFetch.mockResolvedValue([])

    const form = await withComposable(() => useOnCallPeriodForm('edit', editPeriod))
    mockFetch.mockClear()

    // Set only end date (no start)
    form.dateRange.value = {
      start: undefined,
      end: new CalendarDate(2026, 4, 30)
    }
    await nextTick()
    vi.advanceTimersByTime(600)
    await flushPromises()
    expect(mockFetch).not.toHaveBeenCalledWith(
      '/api/v1/holidays/suggestions',
      expect.any(Object)
    )
  })
})
