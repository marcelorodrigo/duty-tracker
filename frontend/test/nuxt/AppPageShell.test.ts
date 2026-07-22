import { describe, expect, it } from 'vitest'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import AppPageShell from '~/components/AppPageShell.vue'

describe('AppPageShell', () => {
  it('renders navigation, heading, actions, and body with responsive header layout', async () => {
    const wrapper = await mountSuspended(AppPageShell, {
      props: { maxWidth: 'narrow' },
      slots: {
        navigation: '<a href="/">Back</a>',
        title: 'Page title',
        subtitle: 'Page subtitle',
        actions: '<button type="button">Save</button>',
        default: '<section>Page content</section>'
      }
    })

    expect(wrapper.get('nav').attributes('aria-label')).toBe('Page navigation')
    expect(wrapper.get('h1').text()).toBe('Page title')
    expect(wrapper.get('header').text()).toContain('Page subtitle')
    expect(wrapper.get('header').text()).toContain('Save')
    expect(wrapper.get('section').text()).toBe('Page content')
    expect(wrapper.get('header').classes()).toEqual(expect.arrayContaining([
      'flex-col',
      'sm:flex-row',
      'sm:items-center',
      'sm:justify-between'
    ]))
    expect(wrapper.find('.max-w-3xl').exists()).toBe(true)
  })

  it('omits wrappers for optional slots that are not provided', async () => {
    const wrapper = await mountSuspended(AppPageShell, {
      slots: {
        title: 'Page title',
        default: 'Page content'
      }
    })

    const header = wrapper.get('header')
    expect(header.find('nav').exists()).toBe(false)
    expect(header.find('p').exists()).toBe(false)
    expect(header.text()).toBe('Page title')
    expect(wrapper.find('.max-w-3xl').exists()).toBe(false)
  })
})
