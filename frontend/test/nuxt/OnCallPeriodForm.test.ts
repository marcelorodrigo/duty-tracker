import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref } from 'vue'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import { CalendarDate } from '@internationalized/date'
import OnCallPeriodForm from '~/components/OnCallPeriodForm.vue'
import type { HolidayResponse } from '~/types/holiday'

// ---------------------------------------------------------------------------
// Mock useOnCallPeriodForm so the component can render without a real Nuxt
// router, useFetch, or $fetch in this test.
// ---------------------------------------------------------------------------

const mockSave = vi.fn()
const mockAddCustomHoliday = vi.fn()
const mockRemoveHoliday = vi.fn()

const dateRangeRef = ref<{ start: CalendarDate | undefined, end: CalendarDate | undefined }>({
  start: undefined,
  end: undefined
})
const startTimeRef = ref('14:00')
const endTimeRef = ref('14:00')
const holidaysRef = ref<HolidayResponse[]>([])
const customHolidayDateRef = ref<CalendarDate | undefined>(undefined)
const customHolidayNameRef = ref('')
const customHolidayErrorRef = ref<string | null>(null)
const fetchingHolidaysRef = ref(false)
const savingRef = ref(false)
const errorRef = ref<string | null>(null)

vi.mock('~/composables/useOnCallPeriodForm', () => ({
  useOnCallPeriodForm: () => ({
    dateRange: dateRangeRef,
    startTime: startTimeRef,
    endTime: endTimeRef,
    holidays: holidaysRef,
    customHolidayDate: customHolidayDateRef,
    customHolidayName: customHolidayNameRef,
    customHolidayError: customHolidayErrorRef,
    fetchingHolidays: fetchingHolidaysRef,
    saving: savingRef,
    error: errorRef,
    addCustomHoliday: mockAddCustomHoliday,
    removeHoliday: mockRemoveHoliday,
    save: mockSave
  })
}))

describe('OnCallPeriodForm', () => {
  beforeEach(() => {
    dateRangeRef.value = { start: undefined, end: undefined }
    startTimeRef.value = '14:00'
    endTimeRef.value = '14:00'
    holidaysRef.value = []
    customHolidayDateRef.value = undefined
    customHolidayNameRef.value = ''
    customHolidayErrorRef.value = null
    fetchingHolidaysRef.value = false
    savingRef.value = false
    errorRef.value = null
    mockSave.mockReset()
    mockSave.mockResolvedValue(undefined)
    mockAddCustomHoliday.mockReset()
    mockRemoveHoliday.mockReset()
  })

  describe('date range label', () => {
    it('shows "Select date range" placeholder when no range is selected', async () => {
      const wrapper = await mountSuspended(OnCallPeriodForm, {
        props: { mode: 'create' }
      })

      // Label text is not rendered when rangeReady is false; the calendar shows without label
      expect(wrapper.text()).not.toContain('→')
    })

    it('shows formatted date range label when both dates are set', async () => {
      dateRangeRef.value = {
        start: new CalendarDate(2026, 4, 1),
        end: new CalendarDate(2026, 4, 30)
      }

      const wrapper = await mountSuspended(OnCallPeriodForm, {
        props: { mode: 'create' }
      })

      expect(wrapper.text()).toContain('2026-04-01')
      expect(wrapper.text()).toContain('2026-04-30')
      expect(wrapper.text()).toContain('→')
    })
  })

  describe('time inputs', () => {
    it('renders start time and end time fields', async () => {
      const wrapper = await mountSuspended(OnCallPeriodForm, {
        props: { mode: 'create' }
      })

      expect(wrapper.text()).toContain('Start time')
      expect(wrapper.text()).toContain('End time')
    })
  })

  describe('holidays section', () => {
    it('is hidden when no date range is selected', async () => {
      const wrapper = await mountSuspended(OnCallPeriodForm, {
        props: { mode: 'create' }
      })

      expect(wrapper.text()).not.toContain('Holidays')
    })

    it('appears when both start and end dates are set', async () => {
      dateRangeRef.value = {
        start: new CalendarDate(2026, 4, 1),
        end: new CalendarDate(2026, 4, 30)
      }

      const wrapper = await mountSuspended(OnCallPeriodForm, {
        props: { mode: 'create' }
      })

      expect(wrapper.text()).toContain('Holidays')
    })

    it('shows loading spinner while fetching holiday suggestions', async () => {
      dateRangeRef.value = {
        start: new CalendarDate(2026, 4, 1),
        end: new CalendarDate(2026, 4, 30)
      }
      fetchingHolidaysRef.value = true

      const wrapper = await mountSuspended(OnCallPeriodForm, {
        props: { mode: 'create' }
      })

      expect(wrapper.text()).toContain('Loading holiday suggestions')
    })

    it('shows empty state when holidays list is empty and not loading', async () => {
      dateRangeRef.value = {
        start: new CalendarDate(2026, 4, 1),
        end: new CalendarDate(2026, 4, 30)
      }
      holidaysRef.value = []

      const wrapper = await mountSuspended(OnCallPeriodForm, {
        props: { mode: 'create' }
      })

      expect(wrapper.text()).toContain('No holidays found')
    })

    it('renders holiday rows when holidays are present', async () => {
      dateRangeRef.value = {
        start: new CalendarDate(2026, 4, 1),
        end: new CalendarDate(2026, 4, 30)
      }
      holidaysRef.value = [
        { date: '2026-04-27', name: 'Koningsdag' }
      ]

      const wrapper = await mountSuspended(OnCallPeriodForm, {
        props: { mode: 'create' }
      })

      expect(wrapper.text()).toContain('27/04/2026')
      // Holiday name is bound via v-model to a UInput — check the input value attribute
      const inputs = wrapper.findAll('input')
      const holidayInput = inputs.find(i => i.element.value === 'Koningsdag')
      expect(holidayInput).toBeDefined()
    })

    it('shows "Add custom holiday" section when range is set', async () => {
      dateRangeRef.value = {
        start: new CalendarDate(2026, 4, 1),
        end: new CalendarDate(2026, 4, 30)
      }

      const wrapper = await mountSuspended(OnCallPeriodForm, {
        props: { mode: 'create' }
      })

      expect(wrapper.text()).toContain('Add custom holiday')
    })
  })

  describe('error alert', () => {
    it('is not shown when error is null', async () => {
      const wrapper = await mountSuspended(OnCallPeriodForm, {
        props: { mode: 'create' }
      })

      expect(wrapper.text()).not.toContain('Network error')
    })

    it('shows error message when error is set', async () => {
      errorRef.value = 'Network error'

      const wrapper = await mountSuspended(OnCallPeriodForm, {
        props: { mode: 'create' }
      })

      expect(wrapper.text()).toContain('Network error')
    })
  })

  describe('action buttons', () => {
    it('renders Save and Cancel buttons', async () => {
      const wrapper = await mountSuspended(OnCallPeriodForm, {
        props: { mode: 'create' }
      })

      const buttons = wrapper.findAll('button')
      const buttonTexts = buttons.map(b => b.text())
      expect(buttonTexts).toContain('Save')
      expect(buttonTexts).toContain('Cancel')
    })

    it('calls save() when Save button is clicked', async () => {
      const wrapper = await mountSuspended(OnCallPeriodForm, {
        props: { mode: 'create' }
      })

      const saveBtn = wrapper.findAll('button').find(b => b.text() === 'Save')
      if (!saveBtn) throw new Error('Save button not found')

      await saveBtn.trigger('click')

      expect(mockSave).toHaveBeenCalledOnce()
    })
  })
})
