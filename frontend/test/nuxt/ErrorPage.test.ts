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
  it.each([
    { status: 404, statusMessage: 'Not Found', expectedText: ['404', 'Page not found', 'doesn\'t exist'] },
    { status: 500, statusMessage: 'Internal Server Error', expectedText: ['500', 'Something went wrong', 'unexpected error'] }
  ])('renders correct content for $status error', async ({ status, statusMessage, expectedText }) => {
    const error = createError(status, statusMessage)
    const wrapper = await mountSuspended(ErrorPage, { props: { error } })

    for (const text of expectedText) {
      expect(wrapper.text()).toContain(text)
    }
  })

  it('renders a back-to-home button', async () => {
    const error = createError(404, 'Not Found')
    const wrapper = await mountSuspended(ErrorPage, { props: { error } })

    const buttons = wrapper.findAll('button')
    expect(buttons.length).toBeGreaterThan(0)
  })
})
