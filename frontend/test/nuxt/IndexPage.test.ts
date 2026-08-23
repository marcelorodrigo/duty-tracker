import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { ref } from 'vue'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import { flushPromises } from '@vue/test-utils'
import type { VueWrapper } from '@vue/test-utils'
import IndexPage from '~/pages/index.vue'
import type { OnCallPeriodResponse } from '~/types/onCallPeriod'
import type { CalendarFeedPreview } from '~/types/calendarFeed'

const now = new Date()

const activePeriod: OnCallPeriodResponse = {
  id: 1,
  startDateTime: new Date(now.getTime() - 60 * 60 * 1000).toISOString(),
  endDateTime: new Date(now.getTime() + 60 * 60 * 1000).toISOString(),
  holidays: [],
  createdAt: '2026-01-01T00:00:00Z'
}

const pastPeriod: OnCallPeriodResponse = {
  id: 2,
  startDateTime: '2020-01-01T14:00:00',
  endDateTime: '2020-01-08T14:00:00',
  holidays: [],
  createdAt: '2020-01-01T00:00:00Z'
}

const activePeriodsRef = ref<OnCallPeriodResponse[]>([])
const pastPeriodsRef = ref<OnCallPeriodResponse[]>([])
const pendingRef = ref(false)
const errorRef = ref<Error | null>(null)
const deleteModalOpenRef = ref(false)
const deletingPeriodRef = ref<OnCallPeriodResponse | null>(null)
const mockFetchPeriods = vi.fn()
const mockOpenDeleteModal = vi.fn()
const mockCloseDeleteModal = vi.fn()
const mockRemove = vi.fn()

const profileRef = ref<{ id: number; calendarFeedUrl?: string }>({ id: 1, calendarFeedUrl: undefined })
const calendarFeedPreviewRef = ref<CalendarFeedPreview | null>(null)
const calendarFeedPendingRef = ref(false)
const calendarFeedErrorRef = ref<Error | null>(null)
const mockFetchCalendarFeedPreview = vi.fn()
const mockImportEvent = vi.fn()

vi.mock('~/composables/useOnCallPeriods', () => ({
  useOnCallPeriods: () => ({
    periods: activePeriodsRef,
    activePeriods: activePeriodsRef,
    pastPeriods: pastPeriodsRef,
    pending: pendingRef,
    error: errorRef,
    deleteModalOpen: deleteModalOpenRef,
    deletingPeriod: deletingPeriodRef,
    fetchPeriods: mockFetchPeriods,
    openDeleteModal: mockOpenDeleteModal,
    closeDeleteModal: mockCloseDeleteModal,
    remove: mockRemove
  })
}))

vi.mock('~/composables/useProfile', () => ({
  useProfile: () => ({
    profile: profileRef,
    pending: ref(false),
    error: ref(null),
    save: vi.fn()
  })
}))

vi.mock('~/composables/useCalendarFeed', () => ({
  useCalendarFeed: () => ({
    preview: calendarFeedPreviewRef,
    pending: calendarFeedPendingRef,
    error: calendarFeedErrorRef,
    importing: ref(false),
    fetchPreview: mockFetchCalendarFeedPreview,
    importEvent: mockImportEvent
  })
}))

const mockNavigateTo = vi.fn()

let currentWrapper: VueWrapper | undefined
vi.mock('#app/composables/router', async (importOriginal) => {
  const actual = await importOriginal<typeof import('#app/composables/router')>()
  return {
    ...actual,
    navigateTo: (...args: Parameters<typeof actual.navigateTo>) => mockNavigateTo(...args)
  }
})

beforeEach(() => {
  activePeriodsRef.value = []
  pastPeriodsRef.value = []
  pendingRef.value = false
  errorRef.value = null
  deleteModalOpenRef.value = false
  deletingPeriodRef.value = null
  profileRef.value = { id: 1, calendarFeedUrl: undefined }
  calendarFeedPreviewRef.value = null
  calendarFeedPendingRef.value = false
  calendarFeedErrorRef.value = null
  mockFetchPeriods.mockReset()
  mockOpenDeleteModal.mockReset()
  mockCloseDeleteModal.mockReset()
  mockRemove.mockReset()
  mockFetchCalendarFeedPreview.mockReset()
  mockImportEvent.mockReset()
  mockNavigateTo.mockReset()
})

afterEach(() => {
  currentWrapper?.unmount()
  currentWrapper = undefined
  vi.unstubAllGlobals()
})

describe('IndexPage (pages/index.vue)', () => {
  describe('loading state', () => {
    it('shows a loading spinner when pending=true', async () => {
      pendingRef.value = true
      currentWrapper = await mountSuspended(IndexPage)
      // Spinner is rendered via UIcon with animate-spin class
      expect(currentWrapper.html()).toContain('animate-spin')
    })
  })

  describe('error state', () => {
    it('shows an error alert when error is set', async () => {
      errorRef.value = new Error('Network error')
      currentWrapper = await mountSuspended(IndexPage)
      expect(currentWrapper.text()).toContain('Failed to load periods')
    })
  })

  describe('empty state', () => {
    it('shows empty state UI when activePeriods is empty', async () => {
      activePeriodsRef.value = []
      currentWrapper = await mountSuspended(IndexPage)
      expect(currentWrapper.text()).toContain('No active on-call periods')
    })
  })

  describe('active periods list', () => {
    it('renders an OnCallPeriodCard for each active period', async () => {
      activePeriodsRef.value = [activePeriod]
      currentWrapper = await mountSuspended(IndexPage)
      // The card renders the period's formatted date range
      expect(currentWrapper.text()).toContain('Active')
    })
  })

  describe('past periods section', () => {
    it('shows past periods section when pastPeriods has items', async () => {
      pastPeriodsRef.value = [pastPeriod]
      currentWrapper = await mountSuspended(IndexPage)
      expect(currentWrapper.text()).toContain('Past periods')
    })

    it('does not show past periods section when pastPeriods is empty', async () => {
      pastPeriodsRef.value = []
      currentWrapper = await mountSuspended(IndexPage)
      expect(currentWrapper.text()).not.toContain('Past periods')
    })
  })

  describe('interactions', () => {
    it('calls fetchPeriods on mount', async () => {
      currentWrapper = await mountSuspended(IndexPage)
      await flushPromises()
      expect(mockFetchPeriods).toHaveBeenCalledOnce()
    })

    it('fetches calendar feed preview on mount when a URL is configured', async () => {
      profileRef.value = { id: 1, calendarFeedUrl: 'https://app.incident.io/feed.ics' }
      currentWrapper = await mountSuspended(IndexPage)
      await flushPromises()
      expect(mockFetchCalendarFeedPreview).toHaveBeenCalledOnce()
    })

    it('does not fetch calendar feed preview when no URL is configured', async () => {
      profileRef.value = { id: 1, calendarFeedUrl: undefined }
      currentWrapper = await mountSuspended(IndexPage)
      await flushPromises()
      expect(mockFetchCalendarFeedPreview).not.toHaveBeenCalled()
    })

    it('calls navigateTo with the correct edit URL when onEdit fires', async () => {
      activePeriodsRef.value = [activePeriod]
      currentWrapper = await mountSuspended(IndexPage)
      await currentWrapper.find('[aria-label="Edit period"]').trigger('click')
      expect(mockNavigateTo).toHaveBeenCalledWith(`/oncall/${activePeriod.id}/edit`)
    })
  })

  describe('calendar feed section', () => {
    it('shows the calendar feed section while preview is loading', async () => {
      profileRef.value = { id: 1, calendarFeedUrl: 'https://app.incident.io/feed.ics' }
      calendarFeedPendingRef.value = true
      currentWrapper = await mountSuspended(IndexPage)

      expect(currentWrapper.text()).toContain('Calendar feed')
    })

    it('shows the calendar feed section on preview fetch error', async () => {
      profileRef.value = { id: 1, calendarFeedUrl: 'https://app.incident.io/feed.ics' }
      calendarFeedErrorRef.value = new Error('Failed to load feed')
      currentWrapper = await mountSuspended(IndexPage)
      await flushPromises()

      expect(currentWrapper.text()).toContain('Calendar feed')
      expect(currentWrapper.text()).toContain('Failed to load')
    })

    it('shows the calendar feed section when the preview is empty', async () => {
      profileRef.value = { id: 1, calendarFeedUrl: 'https://app.incident.io/feed.ics' }
      calendarFeedPreviewRef.value = { upcoming: [], past: [] }
      currentWrapper = await mountSuspended(IndexPage)
      await flushPromises()

      expect(currentWrapper.text()).toContain('Calendar feed')
      expect(currentWrapper.text()).toContain('No on-call events found in the feed')
    })
  })
})
