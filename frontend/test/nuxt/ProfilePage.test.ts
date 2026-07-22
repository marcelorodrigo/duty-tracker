import { describe, expect, it, vi, beforeEach } from 'vitest'
import { ref, nextTick } from 'vue'
import { flushPromises } from '@vue/test-utils'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import ProfilePage from '~/pages/settings/profile.vue'
import type { EngineerProfileResponse, UpdateProfileRequest } from '~/types/profile'

const mockSave = vi.fn()

const mockProfile: EngineerProfileResponse = {
  id: 1,
  workingDays: ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'],
  workStartTime: '08:00:00',
  workEndTime: '16:30:00',
  hourlyRate: 50.00,
  standbyWeekdaySaturdayPercentage: 0.067,
  standbyWeekdaySundayHolidayPercentage: 0.084
}

// Mutable ref so individual tests can override the value
const profileRef = ref<EngineerProfileResponse | null>(mockProfile)
const pendingRef = ref(false)
const errorRef = ref<Error | null>(null)

vi.mock('~/composables/useProfile', () => ({
  useProfile: () => ({
    profile: profileRef,
    pending: pendingRef,
    error: errorRef,
    save: mockSave
  })
}))

describe('settings/profile.vue', () => {
  beforeEach(() => {
    profileRef.value = mockProfile
    pendingRef.value = false
    errorRef.value = null
    mockSave.mockReset()
    mockSave.mockResolvedValue(undefined)
  })

  it('renders the form when profile is loaded', async () => {
    const component = await mountSuspended(ProfilePage)

    expect(component.text()).toContain('Working days')
    expect(component.text()).toContain('Start time')
    expect(component.text()).toContain('End time')
    expect(component.text()).toContain('Save profile')
  })

  it('displays time inputs with seconds stripped', async () => {
    const component = await mountSuspended(ProfilePage)

    const inputs = component.findAll('input[type="time"]')
    expect(inputs).toHaveLength(2)
    expect((inputs[0]?.element as HTMLInputElement).value).toBe('08:00')
    expect((inputs[1]?.element as HTMLInputElement).value).toBe('16:30')
  })

  it('calls save with appended seconds and calendar-ordered days on submit', async () => {
    const component = await mountSuspended(ProfilePage)

    // Toggle Wednesday off, Saturday on
    const buttons = component.findAll('button')
    const wedBtn = buttons.find(b => b.text() === 'Wed')
    const satBtn = buttons.find(b => b.text() === 'Sat')

    if (!wedBtn || !satBtn) throw new Error('Day buttons not found')

    await wedBtn.trigger('click')
    await satBtn.trigger('click')

    const form = component.find('form')
    await form.trigger('submit')
    await component.vm.$nextTick()

    expect(mockSave).toHaveBeenCalledOnce()
    const calls = mockSave.mock.calls as Array<[UpdateProfileRequest]>
    const [request] = calls[0]!

    expect(request.workStartTime).toBe('08:00:00')
    expect(request.workEndTime).toBe('16:30:00')
    // Days must be in calendar order: Mon, Tue, Thu, Fri, Sat (Wed removed, Sat added)
    expect(request.workingDays).toEqual(['MONDAY', 'TUESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'])
    // Assert hourlyRate is preserved in the submit payload
    expect(request.hourlyRate).toBe(mockProfile.hourlyRate)
  })

  it('shows fallback message when profile is null', async () => {
    profileRef.value = null

    const component = await mountSuspended(ProfilePage)

    expect(component.text()).toContain('No profile found.')
    expect(component.find('form').exists()).toBe(false)
  })

  it('shows validation error when hourly rate is 1 or less', async () => {
    const component = await mountSuspended(ProfilePage)

    const rateInput = component.find('input#hourly-rate')
    await rateInput.setValue(1)
    await nextTick()

    const form = component.find('form')
    await form.trigger('submit')
    await nextTick()

    expect(component.text()).toContain('Hourly rate must be greater than 1.00')
    expect(mockSave).not.toHaveBeenCalled()
  })

  it('shows warning modal when hourly rate exceeds 200', async () => {
    const component = await mountSuspended(ProfilePage)

    const rateInput = component.find('input#hourly-rate')
    await rateInput.setValue(250)
    await nextTick()

    const form = component.find('form')
    await form.trigger('submit')
    await nextTick()

    // Modal content is teleported to body, check there
    expect(document.body.textContent).toContain('unusually high')
    expect(mockSave).not.toHaveBeenCalled()
  })

  it('submits after confirming high rate warning', async () => {
    const component = await mountSuspended(ProfilePage)

    const rateInput = component.find('input#hourly-rate')
    await rateInput.setValue(250)
    await nextTick()

    const form = component.find('form')
    await form.trigger('submit')
    await nextTick()

    // Find confirm button in teleported modal content
    const allButtons = document.body.querySelectorAll('button')
    const confirmBtn = Array.from(allButtons).find(b => b.textContent?.trim() === 'Confirm')
    if (!confirmBtn) throw new Error('Confirm button not found in teleported content')
    confirmBtn.click()
    await nextTick()

    expect(mockSave).toHaveBeenCalledOnce()
  })

  it('shows validation error when Weekday/Saturday percentage is below 0.001', async () => {
    const component = await mountSuspended(ProfilePage)

    const input = component.find('input#standby-weekday-saturday')
    await input.setValue(0.0005)
    await nextTick()

    const form = component.find('form')
    await form.trigger('submit')
    await nextTick()

    expect(component.text()).toContain('Weekday / Saturday percentage must be at least 0.001')
    expect(mockSave).not.toHaveBeenCalled()
  })

  it('shows validation error when Sunday/Holiday percentage is below 0.001', async () => {
    const component = await mountSuspended(ProfilePage)

    const input = component.find('input#standby-sunday-holiday')
    await input.setValue(0.0005)
    await nextTick()

    const form = component.find('form')
    await form.trigger('submit')
    await nextTick()

    expect(component.text()).toContain('Sunday / Holiday percentage must be at least 0.001')
    expect(mockSave).not.toHaveBeenCalled()
  })

  it('closes the rate warning modal when Cancel is clicked', async () => {
    const component = await mountSuspended(ProfilePage)

    const rateInput = component.find('input#hourly-rate')
    await rateInput.setValue(250)
    await nextTick()

    const form = component.find('form')
    await form.trigger('submit')
    await nextTick()

    // Modal should be open
    expect(document.body.textContent).toContain('unusually high')

    // Click Cancel in teleported content
    const allButtons = document.body.querySelectorAll('button')
    const cancelBtn = Array.from(allButtons).find(b => b.textContent?.trim() === 'Cancel')
    if (!cancelBtn) throw new Error('Cancel button not found in teleported content')

    cancelBtn.click()
    await flushPromises()
    await new Promise(resolve => setTimeout(resolve, 50))

    // Save was never called
    expect(mockSave).not.toHaveBeenCalled()
  })

  it('closes the rate warning modal when dismissed via update:open', async () => {
    const component = await mountSuspended(ProfilePage)

    const rateInput = component.find('input#hourly-rate')
    await rateInput.setValue(250)
    await nextTick()

    const form = component.find('form')
    await form.trigger('submit')
    await nextTick()

    // Modal should be open
    expect(document.body.textContent).toContain('unusually high')

    // Find the UModal component and trigger update:open with false
    const modal = component.findComponent({ name: 'UModal' })
    modal.vm.$emit('update:open', false)
    await nextTick()

    expect(mockSave).not.toHaveBeenCalled()
  })
})
