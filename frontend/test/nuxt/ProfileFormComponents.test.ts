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
      standbyWeekdaySundayHolidayPercentage: 0.0005,
      submitAttempted: true,
      rateError: 'Hourly rate must be greater than 1.00',
      standbyWeekdaySaturdayError: 'Weekday / Saturday percentage must be at least 0.001',
      standbyWeekdaySundayHolidayError: 'Sunday / Holiday percentage must be at least 0.001'
    }
  }

  it('renders validation feedback and emits compensation edits', async () => {
    const wrapper = await mountSuspended(ProfileCompensationFields, { props: props() })

    expect(wrapper.text()).toContain('Hourly rate must be greater than 1.00')
    expect(wrapper.text()).toContain('Weekday / Saturday percentage must be at least 0.001')
    expect(wrapper.text()).toContain('Sunday / Holiday percentage must be at least 0.001')

    await wrapper.get('input#hourly-rate').setValue(75)

    expect(wrapper.emitted('update:hourlyRate')).toEqual([[75]])
  })

  it('hides validation feedback before submission', async () => {
    const wrapper = await mountSuspended(ProfileCompensationFields, {
      props: { ...props(), submitAttempted: false }
    })

    expect(wrapper.text()).not.toContain('must be greater')
    expect(wrapper.text()).not.toContain('must be at least')
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
})
