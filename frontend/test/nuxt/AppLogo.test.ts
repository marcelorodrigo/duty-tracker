import { describe, expect, it, vi, beforeEach } from 'vitest'
import { mountSuspended } from '@nuxt/test-utils/runtime'

// Import after mock is defined
import AppLogo from '~/components/AppLogo.vue'

let colorModeValue: 'light' | 'dark' = 'light'

vi.mock('#app', async () => {
  const actual = await vi.importActual('#app')
  return {
    ...actual,
    useColorMode: () => ({
      value: colorModeValue
    })
  }
})

describe('AppLogo', () => {
  beforeEach(() => {
    colorModeValue = 'light'
  })

  it('renders the logo and text', async () => {
    const component = await mountSuspended(AppLogo)

    expect(component.text()).toContain('Duty Tracker')
    const img = component.find('img')
    expect(img.exists()).toBe(true)
  })

  it('displays an SVG logo alongside text', async () => {
    const component = await mountSuspended(AppLogo)

    const img = component.find('img')
    expect(img.attributes('src')).toMatch(/engineer-(light|dark)\.svg/)
  })

  it('hides the decorative logo from assistive technology', async () => {
    const component = await mountSuspended(AppLogo)

    const img = component.find('img')
    expect(img.attributes('alt')).toBe('')
    expect(img.attributes('aria-hidden')).toBe('true')
  })

  it('applies correct sizing class to logo', async () => {
    const component = await mountSuspended(AppLogo)

    const img = component.find('img')
    expect(img.classes()).toContain('size-14')
  })

  it('displays light mode logo by default', async () => {
    colorModeValue = 'light'
    const component = await mountSuspended(AppLogo)

    const img = component.find('img')
    expect(img.attributes('src')).toContain('engineer-light')
  })
})
