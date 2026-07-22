import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ref } from 'vue'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import ProfileForm from '~/components/settings/profile/ProfileForm.vue'
import ProfilePage from '~/pages/settings/profile.vue'
import type { EngineerProfileResponse } from '~/types/profile'
import { buildProfile } from '../utils/factories'

const mockSave = vi.fn()
const profileRef = ref<EngineerProfileResponse | null>(buildProfile())
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
    profileRef.value = buildProfile()
    pendingRef.value = false
    errorRef.value = null
    mockSave.mockReset()
    mockSave.mockResolvedValue(undefined)
  })

  it('delegates the loaded profile form to the focused feature component', async () => {
    const wrapper = await mountSuspended(ProfilePage)

    expect(wrapper.findComponent(ProfileForm).exists()).toBe(true)
    expect(wrapper.text()).toContain('Work schedule')
    expect(wrapper.text()).toContain('Compensation')
    expect(wrapper.text()).toContain('Save profile')
  })

  it('renders a loading state while the profile request is pending', async () => {
    pendingRef.value = true

    const wrapper = await mountSuspended(ProfilePage)

    expect(wrapper.findComponent(ProfileForm).exists()).toBe(false)
    expect(wrapper.find('.animate-spin').exists()).toBe(true)
  })

  it('renders the load error instead of the form', async () => {
    errorRef.value = new Error('network unavailable')

    const wrapper = await mountSuspended(ProfilePage)

    expect(wrapper.findComponent(ProfileForm).exists()).toBe(false)
    expect(wrapper.text()).toContain('Failed to load profile')
    expect(wrapper.text()).toContain('Please reload the page to try again.')
  })

  it('renders the empty state when no profile exists', async () => {
    profileRef.value = null

    const wrapper = await mountSuspended(ProfilePage)

    expect(wrapper.findComponent(ProfileForm).exists()).toBe(false)
    expect(wrapper.text()).toContain('No profile found.')
  })
})
