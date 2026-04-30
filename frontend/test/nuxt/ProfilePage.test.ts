import { describe, expect, it, vi, beforeEach } from 'vitest'
import { ref } from 'vue'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import ProfilePage from '~/pages/settings/profile.vue'
import type { EngineerProfileResponse } from '~/types/profile'

const mockSave = vi.fn()

const mockProfile: EngineerProfileResponse = {
  id: 1,
  workingDays: ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'],
  workStartTime: '08:00:00',
  workEndTime: '16:30:00'
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

  it('shows active day buttons for working days in profile', async () => {
    const component = await mountSuspended(ProfilePage)

    // Mon-Fri should be active; Sat/Sun should be inactive
    const buttons = component.findAll('button')
    const monBtn = buttons.find(b => b.text() === 'Mon')
    const satBtn = buttons.find(b => b.text() === 'Sat')

    expect(monBtn?.classes()).toContain('text-(--ui-color-primary-500)')
    expect(satBtn?.classes()).not.toContain('text-(--ui-color-primary-500)')
  })

  it('toggles a day on click', async () => {
    const component = await mountSuspended(ProfilePage)

    const buttons = component.findAll('button')
    const satBtn = buttons.find(b => b.text() === 'Sat')

    if (!satBtn) throw new Error('Saturday button not found')

    // Saturday is off — click to enable
    await satBtn.trigger('click')
    expect(satBtn.classes()).toContain('text-(--ui-color-primary-500)')

    // Click again to disable
    await satBtn.trigger('click')
    expect(satBtn.classes()).not.toContain('text-(--ui-color-primary-500)')
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
    const calls = mockSave.mock.calls as unknown[][]
    const [request] = calls[0]!

    expect(request.workStartTime).toBe('08:00:00')
    expect(request.workEndTime).toBe('16:30:00')
    // Days must be in calendar order: Mon, Tue, Thu, Fri, Sat (Wed removed, Sat added)
    expect(request.workingDays).toEqual(['MONDAY', 'TUESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'])
  })

  it('shows fallback message when profile is null', async () => {
    profileRef.value = null

    const component = await mountSuspended(ProfilePage)

    expect(component.text()).toContain('No profile found.')
    expect(component.find('form').exists()).toBe(false)
  })
})
