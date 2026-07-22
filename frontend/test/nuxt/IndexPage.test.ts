import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { ref } from 'vue'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import { flushPromises } from '@vue/test-utils'
import IndexPage from '~/pages/index.vue'
import type { OnCallPeriodResponse } from '~/types/onCallPeriod'

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

const mockNavigateTo = vi.fn()
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
  mockFetchPeriods.mockReset()
  mockOpenDeleteModal.mockReset()
  mockCloseDeleteModal.mockReset()
  mockRemove.mockReset()
  mockNavigateTo.mockReset()
})

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('IndexPage (pages/index.vue)', () => {
  describe('loading state', () => {
    it('does not render settled states while loading', async () => {
      pendingRef.value = true
      const wrapper = await mountSuspended(IndexPage)

      expect(wrapper.text()).not.toContain('No active on-call periods')
      expect(wrapper.text()).not.toContain('Failed to load periods')
    })
  })

  describe('error state', () => {
    it('shows an error alert when error is set', async () => {
      errorRef.value = new Error('Network error')
      const wrapper = await mountSuspended(IndexPage)
      expect(wrapper.text()).toContain('Failed to load periods')
    })
  })

  describe('empty state', () => {
    it('shows empty state UI when activePeriods is empty', async () => {
      activePeriodsRef.value = []
      const wrapper = await mountSuspended(IndexPage)
      expect(wrapper.text()).toContain('No active on-call periods')
    })
  })

  describe('active periods list', () => {
    it('renders an OnCallPeriodCard for each active period', async () => {
      activePeriodsRef.value = [activePeriod]
      const wrapper = await mountSuspended(IndexPage)
      // The card renders the period's formatted date range
      expect(wrapper.text()).toContain('Active')
    })
  })

  describe('past periods section', () => {
    it('shows past periods section when pastPeriods has items', async () => {
      pastPeriodsRef.value = [pastPeriod]
      const wrapper = await mountSuspended(IndexPage)
      expect(wrapper.text()).toContain('Past periods')
    })

    it('does not show past periods section when pastPeriods is empty', async () => {
      pastPeriodsRef.value = []
      const wrapper = await mountSuspended(IndexPage)
      expect(wrapper.text()).not.toContain('Past periods')
    })
  })

  describe('interactions', () => {
    it('calls fetchPeriods on mount', async () => {
      await mountSuspended(IndexPage)
      await flushPromises()
      expect(mockFetchPeriods).toHaveBeenCalledOnce()
    })

    it('calls navigateTo with the correct edit URL when onEdit fires', async () => {
      activePeriodsRef.value = [activePeriod]
      const wrapper = await mountSuspended(IndexPage)
      await wrapper.find('[aria-label="Edit period"]').trigger('click')
      expect(mockNavigateTo).toHaveBeenCalledWith(`/oncall/${activePeriod.id}/edit`)
    })
  })
})
