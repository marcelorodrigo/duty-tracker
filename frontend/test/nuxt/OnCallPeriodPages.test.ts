import { afterEach, describe, expect, it, vi } from 'vitest'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import NewOnCallPeriodPage from '~/pages/oncall/new.vue'
import EditOnCallPeriodPage from '~/pages/oncall/[id]/edit.vue'
import type { OnCallPeriodResponse } from '~/types/onCallPeriod'

const period: OnCallPeriodResponse = {
  id: 42,
  startDateTime: '2026-04-01T14:00:00',
  endDateTime: '2026-04-30T14:00:00',
  holidays: [],
  createdAt: '2026-01-01T00:00:00Z'
}

const mountOptions = {
  global: {
    stubs: {
      OnCallPeriodForm: {
        template: '<div data-testid="period-form" />'
      }
    }
  }
}

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('on-call period route shells', () => {
  it('composes the create form inside a narrow page shell', async () => {
    const wrapper = await mountSuspended(NewOnCallPeriodPage, mountOptions)

    expect(wrapper.get('h1').text()).toBe('New on-call period')
    expect(wrapper.get('nav a[href="/"]').exists()).toBe(true)
    expect(wrapper.get('nav [aria-label="Back to periods"]').exists()).toBe(true)
    expect(wrapper.find('.max-w-3xl').exists()).toBe(true)
    expect(wrapper.get('[data-testid="period-form"]').exists()).toBe(true)
  })

  it('composes a loaded edit form inside the same page shell', async () => {
    vi.stubGlobal('$fetch', vi.fn().mockResolvedValue(period))

    const wrapper = await mountSuspended(EditOnCallPeriodPage, {
      ...mountOptions,
      route: '/oncall/42/edit'
    })

    expect(wrapper.get('h1').text()).toBe('Edit on-call period')
    expect(wrapper.get('nav a[href="/"]').exists()).toBe(true)
    expect(wrapper.get('nav [aria-label="Back to periods"]').exists()).toBe(true)
    expect(wrapper.find('.max-w-3xl').exists()).toBe(true)
    expect(wrapper.get('[data-testid="period-form"]').exists()).toBe(true)
  })
})
