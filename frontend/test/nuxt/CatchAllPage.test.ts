import { describe, it, expect } from 'vitest'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import CatchAllPage from '~/pages/[...slug].vue'

describe('[...slug].vue (catch-all 404 page)', () => {
  it('renders the page not found heading', async () => {
    const wrapper = await mountSuspended(CatchAllPage)

    expect(wrapper.text()).toContain('Page not found')
  })

  it('renders a button linking to /', async () => {
    const wrapper = await mountSuspended(CatchAllPage)

    const link = wrapper.find('a[href="/"]')
    expect(link.exists()).toBe(true)
    expect(link.text()).toContain('Go to Dashboard')
  })
})
