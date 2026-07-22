import { afterEach, describe, expect, it, vi } from 'vitest'
import { flushPromises } from '@vue/test-utils'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import ProfileCompensationFields from '~/components/settings/profile/ProfileCompensationFields.vue'
import ProfileForm from '~/components/settings/profile/ProfileForm.vue'
import ProfileFormActions from '~/components/settings/profile/ProfileFormActions.vue'
import ProfileRateWarning from '~/components/settings/profile/ProfileRateWarning.vue'
import ProfileScheduleFields from '~/components/settings/profile/ProfileScheduleFields.vue'
import { buildProfile } from '../utils/factories'

afterEach(() => {
  document.body.innerHTML = ''
})

describe('ProfileScheduleFields', () => {
  it('renders schedule values and emits typed edits', async () => {
    const wrapper = await mountSuspended(ProfileScheduleFields, {
      props: {
        workingDays: ['MONDAY'],
        workStartTime: '08:00',
        workEndTime: '16:30'
      }
    })

    const monday = wrapper.findAll('button').find(button => button.text() === 'Mon')
    const saturday = wrapper.findAll('button').find(button => button.text() === 'Sat')
    if (!monday || !saturday) throw new Error('Expected working-day buttons')

    expect(monday.classes()).toContain('text-(--ui-color-primary-500)')
    expect(saturday.classes()).not.toContain('text-(--ui-color-primary-500)')
    expect(monday.attributes('aria-label')).toBe('Monday')
    expect(monday.attributes('aria-pressed')).toBe('true')
    expect(monday.attributes('aria-describedby')).toBe('working-days-description')
    expect(saturday.attributes('aria-label')).toBe('Saturday')
    expect(saturday.attributes('aria-pressed')).toBe('false')

    await saturday.trigger('click')
    await wrapper.get('input#work-start-time').setValue('09:15')

    expect(wrapper.emitted('toggleDay')).toEqual([['SATURDAY']])
    expect(wrapper.emitted('update:workStartTime')).toEqual([['09:15']])
  })
})

describe('ProfileCompensationFields', () => {
  function props() {
    return {
      hourlyRate: 1,
      standbyWeekdaySaturdayPercentage: 0.0005,
      standbyWeekdaySundayHolidayPercentage: 0.0005
    }
  }

  it('renders Nuxt UI fields and emits compensation edits', async () => {
    const wrapper = await mountSuspended(ProfileCompensationFields, { props: props() })

    expect(wrapper.findAllComponents({ name: 'UFormField' })).toHaveLength(3)
    expect(wrapper.get('label[for="hourly-rate"]').text()).toBe('Hourly rate')
    expect(wrapper.get('label[for="standby-weekday-saturday"]').text()).toBe('Weekday / Saturday')
    expect(wrapper.get('label[for="standby-sunday-holiday"]').text()).toBe('Sunday / Holiday')

    await wrapper.get('input#hourly-rate').setValue(75)

    expect(wrapper.emitted('update:hourlyRate')).toEqual([[75]])
  })
})

describe('ProfileFormActions', () => {
  it('exposes the saving state on the submit action', async () => {
    const wrapper = await mountSuspended(ProfileFormActions, {
      props: { saving: true }
    })

    expect(wrapper.get('button').attributes('type')).toBe('submit')
    expect(wrapper.get('button').attributes()).toHaveProperty('disabled')
  })
})

describe('ProfileRateWarning', () => {
  it('emits confirmation and cancellation decisions', async () => {
    const wrapper = await mountSuspended(ProfileRateWarning, {
      props: { open: true, hourlyRate: 250, saving: false }
    })
    await flushPromises()

    expect(document.body.textContent).toContain('€250.00')
    const buttons = Array.from(document.body.querySelectorAll('button'))
    const confirm = buttons.find(button => button.textContent?.trim() === 'Confirm')
    const cancel = buttons.find(button => button.textContent?.trim() === 'Cancel')
    if (!confirm || !cancel) throw new Error('Expected warning actions')

    confirm.click()
    cancel.click()
    await flushPromises()

    expect(wrapper.emitted('confirm')).toHaveLength(1)
    expect(wrapper.emitted('cancel')).toHaveLength(1)
  })
})

describe('ProfileForm', () => {
  it('associates every input with its Nuxt UI field label and description', async () => {
    const wrapper = await mountSuspended(ProfileForm, {
      props: { profile: buildProfile(), save: vi.fn() }
    })

    const fields = [
      ['work-start-time', 'workStartTime', 'Start time', 'normal workday start time'],
      ['work-end-time', 'workEndTime', 'End time', 'normal workday end time'],
      ['hourly-rate', 'hourlyRate', 'Hourly rate', 'incident hour'],
      [
        'standby-weekday-saturday',
        'standbyWeekdaySaturdayPercentage',
        'Weekday / Saturday',
        'weekday or Saturday'
      ],
      [
        'standby-sunday-holiday',
        'standbyWeekdaySundayHolidayPercentage',
        'Sunday / Holiday',
        'Sunday or holiday'
      ]
    ] as const

    for (const [id, name, label, description] of fields) {
      const input = wrapper.get(`input#${id}`)
      const describedBy = input.attributes('aria-describedby')?.split(' ') ?? []
      const describedText = describedBy
        .map(descriptionId => wrapper.get(`[id="${descriptionId}"]`).text())
        .join(' ')

      expect(input.attributes('name')).toBe(name)
      expect(wrapper.get(`label[for="${id}"]`).text()).toBe(label)
      expect(describedText).toContain(description)
      expect(input.attributes('aria-invalid')).toBe('false')
    }
  })

  it('programmatically associates schema errors with invalid fields', async () => {
    const save = vi.fn()
    const wrapper = await mountSuspended(ProfileForm, {
      props: { profile: buildProfile(), save }
    })

    const invalidFields = [
      ['hourly-rate', 1, 'Hourly rate must be greater than 1.00'],
      [
        'standby-weekday-saturday',
        0.0005,
        'Weekday / Saturday percentage must be at least 0.001'
      ],
      [
        'standby-sunday-holiday',
        0.0005,
        'Sunday / Holiday percentage must be at least 0.001'
      ]
    ] as const

    for (const [id, value] of invalidFields) {
      await wrapper.get(`input#${id}`).setValue(value)
    }
    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(save).not.toHaveBeenCalled()

    for (const [id, _value, message] of invalidFields) {
      const input = wrapper.get(`input#${id}`)
      const describedBy = input.attributes('aria-describedby')?.split(' ') ?? []
      const error = describedBy
        .map(errorId => wrapper.find(`[id="${errorId}"]`))
        .find(element => element.exists() && element.text() === message)

      expect(input.attributes('aria-invalid')).toBe('true')
      expect(error?.attributes('data-slot')).toBe('error')
    }
  })

  it('composes focused sections and submits through the supplied save function', async () => {
    const save = vi.fn().mockResolvedValue(undefined)
    const wrapper = await mountSuspended(ProfileForm, {
      props: { profile: buildProfile(), save }
    })

    expect(wrapper.findComponent(ProfileScheduleFields).exists()).toBe(true)
    expect(wrapper.findComponent(ProfileCompensationFields).exists()).toBe(true)
    expect(wrapper.findComponent(ProfileFormActions).exists()).toBe(true)
    expect(wrapper.findComponent(ProfileRateWarning).exists()).toBe(true)

    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(save).toHaveBeenCalledOnce()
  })

  it('keeps high hourly rates behind confirmation before saving', async () => {
    const save = vi.fn().mockResolvedValue(undefined)
    const wrapper = await mountSuspended(ProfileForm, {
      props: { profile: buildProfile({ hourlyRate: 250 }), save }
    })

    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(save).not.toHaveBeenCalled()
    expect(document.body.textContent).toContain('€250.00')

    const confirm = Array.from(document.body.querySelectorAll('button'))
      .find(button => button.textContent?.trim() === 'Confirm')
    if (!confirm) throw new Error('Expected high-rate confirmation action')
    confirm.click()
    await flushPromises()

    expect(save).toHaveBeenCalledOnce()
  })
})
