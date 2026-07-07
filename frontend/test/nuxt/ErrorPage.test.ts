import { describe, it, expect } from 'vitest'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import ErrorPage from '~/error.vue'
import type { NuxtError } from '#app'

function createError(statusCode: number, statusMessage?: string): NuxtError {
  return {
    statusCode,
    statusMessage,
    status: statusCode,
    statusText: statusMessage,
    message: statusMessage ?? 'Something went wrong',
    name: 'NuxtError',
    fatal: false,
    unhandled: false
  } as NuxtError
}

describe('error.vue', () => {
  it('renders the status code for a 404 error', async () => {
    const error = createError(404, 'Not Found')
    const wrapper = await mountSuspended(ErrorPage, { props: { error } })

    expect(wrapper.text()).toContain('404')
  })

  it('renders the custom 404 status message', async () => {
    const error = createError(404, 'Not Found')
    const wrapper = await mountSuspended(ErrorPage, { props: { error } })

    expect(wrapper.text()).toContain('Page not found')
  })

  it('renders the custom 404 description', async () => {
    const error = createError(404, 'Not Found')
    const wrapper = await mountSuspended(ErrorPage, { props: { error } })

    expect(wrapper.text()).toContain("doesn't exist")
  })

  it('renders generic status message for a 500 error', async () => {
    const error = createError(500, 'Internal Server Error')
    const wrapper = await mountSuspended(ErrorPage, { props: { error } })

    expect(wrapper.text()).toContain('Something went wrong')
  })

  it('renders generic description for a 500 error', async () => {
    const error = createError(500, 'Internal Server Error')
    const wrapper = await mountSuspended(ErrorPage, { props: { error } })

    expect(wrapper.text()).toContain('unexpected error')
  })

  it('renders a back-to-home button', async () => {
    const error = createError(404, 'Not Found')
    const wrapper = await mountSuspended(ErrorPage, { props: { error } })

    const buttons = wrapper.findAll('button')
    expect(buttons.length).toBeGreaterThan(0)
  })
})
