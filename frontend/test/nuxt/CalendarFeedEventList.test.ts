import { describe, it, expect, vi } from 'vitest'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import { flushPromises } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { PiniaColada } from '@pinia/colada'
import CalendarFeedEventList from '~/components/CalendarFeedEventList.vue'
import type { CalendarFeedEvent } from '~/types/calendarFeed'

function deferredPromise<T>() {
  let resolve!: (value: T) => void
  let reject!: (error: unknown) => void
  const promise = new Promise<T>((res, rej) => {
    resolve = res
    reject = rej
  })
  return { promise, resolve, reject }
}

const event: CalendarFeedEvent = {
  startDateTime: '2026-02-01T09:00:00',
  endDateTime: '2026-02-08T09:00:00',
  summary: 'On-call'
}

describe('CalendarFeedEventList', () => {
  it('keeps the import button disabled while importing', async () => {
    const { promise, resolve } = deferredPromise<boolean>()
    const importEvent = vi.fn(() => promise)

    const wrapper = await mountSuspended(CalendarFeedEventList, {
      global: {
        plugins: [createPinia(), PiniaColada],
      },
      props: {
        title: 'Upcoming',
        events: [event],
        importEvent,
        importing: false
      }
    })

    const button = wrapper.find('button')

    expect(button.attributes('disabled')).toBeUndefined()

    await button.trigger('click')
    await flushPromises()

    expect(importEvent).toHaveBeenCalledOnce()
    await wrapper.setProps({ importing: true })
    expect(button.attributes('disabled')).toBeDefined()

    resolve(true)
    await flushPromises()
    await wrapper.setProps({ importing: false })
    expect(button.attributes('disabled')).toBeUndefined()
  })

  it('clears the disabled state when importEvent rejects', async () => {
    const { promise, reject } = deferredPromise<boolean>()
    const importEvent = vi.fn(() => promise)

    const wrapper = await mountSuspended(CalendarFeedEventList, {
      global: {
        plugins: [createPinia(), PiniaColada],
      },
      props: {
        title: 'Upcoming',
        events: [event],
        importEvent,
        importing: false
      }
    })

    const button = wrapper.find('button')
    await button.trigger('click')
    await flushPromises()

    await wrapper.setProps({ importing: true })
    expect(button.attributes('disabled')).toBeDefined()

    reject(new Error('Import failed'))
    await flushPromises()
    await wrapper.setProps({ importing: false })
    expect(button.attributes('disabled')).toBeUndefined()
  })
})
