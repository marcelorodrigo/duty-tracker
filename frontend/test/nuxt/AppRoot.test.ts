import { describe, it, expect } from 'vitest'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import AppRoot from '~/app.vue'

describe('app.vue', () => {
  it('renders the header with a logo link to /', async () => {
    const wrapper = await mountSuspended(AppRoot)
    // UHeader is rendered; the logo NuxtLink goes to '/'
    const logoLink = wrapper.find('a[href="/"]')
    expect(logoLink.exists()).toBe(true)
  })

  it('renders a settings button linking to /settings', async () => {
    const wrapper = await mountSuspended(AppRoot)
    const settingsLink = wrapper.find('a[href="/settings"]')
    expect(settingsLink.exists()).toBe(true)
  })

  it('renders a color mode button', async () => {
    const wrapper = await mountSuspended(AppRoot)
    // UColorModeButton renders a button with an aria-label related to color mode
    const buttons = wrapper.findAll('button')
    expect(buttons.length).toBeGreaterThan(0)
  })

  it('renders the main content area', async () => {
    const wrapper = await mountSuspended(AppRoot)
    expect(wrapper.html()).toContain('main')
  })
})
