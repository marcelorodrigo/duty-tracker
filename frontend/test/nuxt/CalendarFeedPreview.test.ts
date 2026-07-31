import { describe, it, expect, vi } from 'vitest'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import CalendarFeedPreview from '~/components/CalendarFeedPreview.vue'
import type { CalendarFeedPreview as CalendarFeedPreviewType } from '~/types/calendarFeed'

const samplePreview: CalendarFeedPreviewType = {
  upcoming: [
    { startDateTime: '2026-02-01T09:00:00', endDateTime: '2026-02-08T09:00:00', summary: 'On-call' }
  ],
  past: [
    { startDateTime: '2026-01-01T09:00:00', endDateTime: '2026-01-08T09:00:00', summary: 'Previous' }
  ]
}

describe('CalendarFeedPreview', () => {
  it('renders empty state when no feed URL is configured', async () => {
    const wrapper = await mountSuspended(CalendarFeedPreview, {
      props: {
        preview: null,
        pending: false,
        error: null,
        hasFeedUrl: false
      }
    })

    expect(wrapper.text()).toContain('No calendar feed configured')
  })

  it('renders upcoming and past events when preview is provided', async () => {
    const wrapper = await mountSuspended(CalendarFeedPreview, {
      props: {
        preview: samplePreview,
        pending: false,
        error: null,
        hasFeedUrl: true
      }
    })

    expect(wrapper.text()).toContain('Upcoming')
    expect(wrapper.text()).toContain('Past')
    expect(wrapper.text()).toContain('On-call')
    expect(wrapper.text()).toContain('Previous')
  })

  it('emits refresh when the refresh button is clicked', async () => {
    const wrapper = await mountSuspended(CalendarFeedPreview, {
      props: {
        preview: samplePreview,
        pending: false,
        error: null,
        hasFeedUrl: true
      }
    })

    const refreshBtn = wrapper.findAll('button').find(b => b.text().includes('Refresh'))
    await refreshBtn?.trigger('click')

    expect(wrapper.emitted('refresh')).toHaveLength(1)
  })

  it('emits import when import button is clicked', async () => {
    const wrapper = await mountSuspended(CalendarFeedPreview, {
      props: {
        preview: samplePreview,
        pending: false,
        error: null,
        hasFeedUrl: true
      }
    })

    const importBtn = wrapper.findAll('button').find(b => b.text().includes('Import'))
    await importBtn?.trigger('click')

    expect(wrapper.emitted('import')).toHaveLength(1)
    expect(wrapper.emitted('import')![0]).toEqual([samplePreview.upcoming[0]])
  })

  it('renders error state', async () => {
    const wrapper = await mountSuspended(CalendarFeedPreview, {
      props: {
        preview: samplePreview,
        pending: false,
        error: new Error('Failed to load'),
        hasFeedUrl: true
      }
    })

    expect(wrapper.text()).toContain('Failed to load')
  })
})
