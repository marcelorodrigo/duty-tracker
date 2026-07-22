import type { CalendarDate, DateValue } from '@internationalized/date'
import type { OnCallPeriodResponse } from '~/types/onCallPeriod'
import type { HolidayInput } from '~/types/holiday'
import {
  calendarDateFromISO,
  calendarDateToISO,
  buildCalendarDateTime,
  fromCalendarDateTime,
  currentWeekMondayAt14,
  nextWeekMondayAt14,
  extractTimeFromISO
} from '~/utils/dates'
import {
  validateOnCallPeriodForm,
  validateCustomHoliday
} from '~/utils/validation'

export function useOnCallPeriodForm(mode: 'create' | 'edit', existingPeriod?: OnCallPeriodResponse) {
  const router = useRouter()
  const { $api } = useNuxtApp()

  // ---- Date / time state ------------------------------------------------
  const dateRange = shallowRef<{ start: DateValue | undefined, end: DateValue | undefined }>({
    start: undefined,
    end: undefined
  })
  const startTime = ref('14:00')
  const endTime = ref('14:00')

  // ---- Holiday state ----------------------------------------------------
  const holidays = ref<HolidayInput[]>([])
  const customHolidayDate = shallowRef<DateValue | undefined>(undefined)
  const customHolidayName = ref('')
  const customHolidayError = ref<string | null>(null)
  const fetchingHolidays = ref(false)

  // ---- UI state ---------------------------------------------------------
  const saving = ref(false)
  const error = ref<string | null>(null)

  // ---- Debounce handle --------------------------------------------------
  let debounceTimer: ReturnType<typeof setTimeout> | null = null

  // ---- Helpers -----------------------------------------------------------

  function mergeHolidays(
    current: HolidayInput[],
    newSuggestions: { date: string, name: string | null }[],
    newStart: string,
    newEnd: string
  ): HolidayInput[] {
    // Keep holidays that fall within the new date range
    const start = calendarDateFromISO(newStart)
    const end = calendarDateFromISO(newEnd)
    const filtered = current.filter((h) => {
      const d = calendarDateFromISO(h.date)
      return d.compare(start) >= 0 && d.compare(end) <= 0
    })
    const existingDates = new Set(filtered.map(h => h.date))

    // Add suggestions not already present
    for (const suggestion of newSuggestions) {
      if (!existingDates.has(suggestion.date)) {
        filtered.push({
          date: suggestion.date,
          name: suggestion.name ?? ''
        })
      }
    }

    // Sort by date ascending
    filtered.sort((a, b) => calendarDateFromISO(a.date).compare(calendarDateFromISO(b.date)))
    return filtered
  }

  async function fetchAndMergeSuggestions(): Promise<void> {
    if (!dateRange.value.start || !dateRange.value.end) return

    const start = calendarDateToISO(dateRange.value.start as CalendarDate)
    const end = calendarDateToISO(dateRange.value.end as CalendarDate)

    fetchingHolidays.value = true
    try {
      const suggestions = await $api.get<{ date: string, name: string | null }[]>(
        '/holidays/suggestions',
        { query: { start, end } }
      )
      holidays.value = mergeHolidays(holidays.value, suggestions, start, end)
    } catch {
      // Swallow suggestion fetch errors — holidays section still usable
    } finally {
      fetchingHolidays.value = false
    }
  }

  function scheduleSuggestionFetch(): void {
    if (debounceTimer !== null) {
      clearTimeout(debounceTimer)
    }
    debounceTimer = setTimeout(() => {
      fetchAndMergeSuggestions()
    }, 500)
  }

  // ---- Watch date range for auto-fetch ----------------------------------
  watch(
    () => [dateRange.value.start, dateRange.value.end],
    ([start, end]) => {
      if (start && end) {
        scheduleSuggestionFetch()
      }
    }
  )

  // ---- Initialisation ---------------------------------------------------

  function initCreate(): void {
    const now = new Date()
    const startDate = currentWeekMondayAt14(now)
    const endDate = nextWeekMondayAt14(startDate)

    dateRange.value = {
      start: calendarDateFromISO(
        `${startDate.getFullYear()}-${String(startDate.getMonth() + 1).padStart(2, '0')}-${String(startDate.getDate()).padStart(2, '0')}`
      ),
      end: calendarDateFromISO(
        `${endDate.getFullYear()}-${String(endDate.getMonth() + 1).padStart(2, '0')}-${String(endDate.getDate()).padStart(2, '0')}`
      )
    }
    startTime.value = '14:00'
    endTime.value = '14:00'
    // Trigger initial suggestion fetch
    fetchAndMergeSuggestions()
  }

  async function initEdit(period: OnCallPeriodResponse): Promise<void> {
    dateRange.value = {
      start: calendarDateFromISO(period.startDateTime),
      end: calendarDateFromISO(period.endDateTime)
    }

    // Extract HH:MM from ISO datetime strings
    const startTimePart = extractTimeFromISO(period.startDateTime)
    const endTimePart = extractTimeFromISO(period.endDateTime)

    startTime.value = startTimePart
    endTime.value = endTimePart

    // Seed the list with already-saved holidays
    holidays.value = period.holidays.map(h => ({
      date: h.date,
      name: h.name ?? ''
    }))

    // Merge with suggestions
    await fetchAndMergeSuggestions()
  }

  // Run init based on mode
  if (mode === 'create') {
    initCreate()
  } else if (mode === 'edit' && existingPeriod) {
    initEdit(existingPeriod)
  }

  // ---- Holiday actions --------------------------------------------------

  function addCustomHoliday(): void {
    customHolidayError.value = null

    const error = validateCustomHoliday(
      customHolidayDate.value,
      dateRange.value.start,
      dateRange.value.end,
      holidays.value.map(h => h.date)
    )
    if (error) {
      customHolidayError.value = error
      return
    }

    const dateISO = calendarDateToISO(customHolidayDate.value as CalendarDate)

    holidays.value = [
      ...holidays.value,
      { date: dateISO, name: customHolidayName.value.trim() }
    ].sort((a, b) => calendarDateFromISO(a.date).compare(calendarDateFromISO(b.date)))

    customHolidayDate.value = undefined
    customHolidayName.value = ''
  }

  function removeHoliday(date: string): void {
    holidays.value = holidays.value.filter(h => h.date !== date)
  }

  // ---- Validation -------------------------------------------------------

  function validateForm(): string | null {
    return validateOnCallPeriodForm(
      dateRange.value.start,
      dateRange.value.end,
      startTime.value,
      endTime.value
    )
  }

  // ---- Save -------------------------------------------------------------

  async function save(): Promise<void> {
    error.value = null
    const validationError = validateForm()
    if (validationError) {
      error.value = validationError
      return
    }

    const startDT = buildCalendarDateTime(dateRange.value.start! as CalendarDate, startTime.value)
    const endDT = buildCalendarDateTime(dateRange.value.end! as CalendarDate, endTime.value)
    const startISO = fromCalendarDateTime(startDT)
    const endISO = fromCalendarDateTime(endDT)

    const selectedHolidays = holidays.value
      .map(h => ({ date: h.date, name: h.name || null }))

    saving.value = true
    try {
      let periodId: number

      if (mode === 'create') {
        const created = await $api.post<{ id: number }>('/oncall-periods', {
          startDateTime: startISO,
          endDateTime: endISO
        })
        periodId = created.id
      } else {
        periodId = existingPeriod!.id
        await $api.put(`/oncall-periods/${periodId}`, {
          startDateTime: startISO,
          endDateTime: endISO
        })
      }

      await $api.put(`/oncall-periods/${periodId}/holidays`, selectedHolidays)

      await router.push(`/oncall/${periodId}`)
    } catch (err) {
      error.value = getApiErrorMessage(err)
    } finally {
      saving.value = false
    }
  }

  // ---- Exported API -----------------------------------------------------

  return {
    dateRange,
    startTime,
    endTime,
    holidays,
    customHolidayDate,
    customHolidayName,
    customHolidayError,
    fetchingHolidays,
    saving,
    error,
    addCustomHoliday,
    removeHoliday,
    save
  }
}
