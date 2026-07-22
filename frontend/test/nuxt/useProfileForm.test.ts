import { describe, expect, it, vi } from 'vitest'
import { nextTick, ref } from 'vue'
import { useProfileForm } from '~/composables/useProfileForm'
import { buildProfile } from '../utils/factories'

describe('useProfileForm', () => {
  it('initializes and resynchronizes editable state from the profile', async () => {
    const profile = ref(buildProfile())
    const form = useProfileForm({ profile, save: vi.fn() })

    expect(form.workingDays.value).toEqual([
      'MONDAY',
      'TUESDAY',
      'WEDNESDAY',
      'THURSDAY',
      'FRIDAY'
    ])
    expect(form.workStartTime.value).toBe('08:00')
    expect(form.workEndTime.value).toBe('16:30')

    profile.value = buildProfile({
      workingDays: ['SATURDAY'],
      workStartTime: '09:15:00',
      workEndTime: '17:45:00'
    })
    await nextTick()

    expect(form.workingDays.value).toEqual(['SATURDAY'])
    expect(form.workStartTime.value).toBe('09:15')
    expect(form.workEndTime.value).toBe('17:45')
  })

  it('toggles days and saves them in calendar order with API time values', async () => {
    const save = vi.fn().mockResolvedValue(undefined)
    const form = useProfileForm({ profile: buildProfile(), save })

    form.toggleDay('WEDNESDAY')
    form.toggleDay('SATURDAY')
    await form.submit()

    expect(save).toHaveBeenCalledOnce()
    expect(save).toHaveBeenCalledWith({
      workingDays: ['MONDAY', 'TUESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'],
      workStartTime: '08:00:00',
      workEndTime: '16:30:00',
      hourlyRate: 50,
      standbyWeekdaySaturdayPercentage: 0.067,
      standbyWeekdaySundayHolidayPercentage: 0.084
    })
  })

  it.each([
    ['hourly rate', 'hourlyRate', 1, 'Hourly rate must be greater than 1.00'],
    [
      'weekday percentage',
      'standbyWeekdaySaturdayPercentage',
      0.0005,
      'Weekday / Saturday percentage must be at least 0.001'
    ],
    [
      'Sunday percentage',
      'standbyWeekdaySundayHolidayPercentage',
      0.0005,
      'Sunday / Holiday percentage must be at least 0.001'
    ]
  ] as const)('blocks an invalid %s', async (_label, field, value, message) => {
    const save = vi.fn()
    const form = useProfileForm({ profile: buildProfile(), save })

    form[field].value = value
    await form.submit()

    expect(form.submitAttempted.value).toBe(true)
    expect(save).not.toHaveBeenCalled()
    expect([
      form.rateError.value,
      form.standbyWeekdaySaturdayError.value,
      form.standbyWeekdaySundayHolidayError.value
    ]).toContain(message)
  })

  it('requires confirmation before saving a high hourly rate', async () => {
    const save = vi.fn().mockResolvedValue(undefined)
    const form = useProfileForm({ profile: buildProfile({ hourlyRate: 250 }), save })

    await form.submit()

    expect(form.showRateWarning.value).toBe(true)
    expect(save).not.toHaveBeenCalled()

    await form.confirmRateWarning()

    expect(save).toHaveBeenCalledOnce()
    expect(form.showRateWarning.value).toBe(false)
  })

  it('always restores the action state when saving rejects', async () => {
    const form = useProfileForm({
      profile: buildProfile(),
      save: vi.fn().mockRejectedValue(new Error('save failed'))
    })

    await expect(form.submit()).rejects.toThrow('save failed')

    expect(form.saving.value).toBe(false)
    expect(form.showRateWarning.value).toBe(false)
  })
})
