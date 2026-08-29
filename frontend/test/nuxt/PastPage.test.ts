import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { ref } from 'vue'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import { flushPromises } from '@vue/test-utils'
import type { VueWrapper } from '@vue/test-utils'
import PastPage from '~/pages/past.vue'
import type { OnCallPeriodResponse } from '~/types/onCallPeriod'

const pastPeriod: OnCallPeriodResponse = {
  id: 7,
  startDateTime: '2020-01-01T14:00:00',
  endDateTime: '2020-01-08T14:00:00',
  holidays: [],
  createdAt: '2020-01-01T00:00:00Z'
}

const pastPeriodsRef = ref<OnCallPeriodResponse[]>([])
const pendingRef = ref(false)
const errorRef = ref<Error | null>(null)
const hasMoreRef = ref(false)
const deleteModalOpenRef = ref(false)
const deletingPeriodRef = ref<OnCallPeriodResponse | null>(null)
const mockCloseDeleteModal = vi.fn()
const mockRemove = vi.fn()

const mockNavigateTo = vi.fn()

vi.mock('~/composables/useOnCallPeriods', () => ({
  useOnCallPeriods: () => ({
    periods: pastPeriodsRef,
    activePeriods: ref([]),
    pastPeriods: pastPeriodsRef,
    pending: pendingRef,
    error: errorRef,
    hasMore: hasMoreRef,
    sentinelRef: ref(null),
    reset: vi.fn(),
    loadNext: vi.fn(),
    deleteModalOpen: deleteModalOpenRef,
    deletingPeriod: deletingPeriodRef,
    openDeleteModal: (period: OnCallPeriodResponse) => {
      deletingPeriodRef.value = period
      deleteModalOpenRef.value = true
    },
    closeDeleteModal: mockCloseDeleteModal,
    remove: mockRemove
  })
}))

vi.mock('#app/composables/router', async (importOriginal) => {
  const actual = await importOriginal<typeof import('#app/composables/router')>()
  return {
    ...actual,
    navigateTo: (...args: Parameters<typeof actual.navigateTo>) => mockNavigateTo(...args)
  }
})

let currentWrapper: VueWrapper | undefined

beforeEach(() => {
  pastPeriodsRef.value = []
  pendingRef.value = false
  errorRef.value = null
  hasMoreRef.value = false
  deleteModalOpenRef.value = false
  deletingPeriodRef.value = null
  mockCloseDeleteModal.mockReset()
  mockRemove.mockReset()
  mockNavigateTo.mockReset()
})

afterEach(() => {
  currentWrapper?.unmount()
  currentWrapper = undefined
  document.body.innerHTML = ''
  vi.unstubAllGlobals()
})

describe('PastPage (pages/past.vue)', () => {
  describe('loading state', () => {
    it('shows a loading spinner while periods are pending', async () => {
      pendingRef.value = true
      currentWrapper = await mountSuspended(PastPage)
      expect(currentWrapper.html()).toContain('animate-spin')
    })
  })

  describe('error state', () => {
    it('shows an error alert when loading fails', async () => {
      errorRef.value = new Error('Network error')
      currentWrapper = await mountSuspended(PastPage)
      expect(currentWrapper.text()).toContain('Failed to load periods')
    })
  })

  describe('empty state', () => {
    it('shows an empty message when there are no past periods', async () => {
      currentWrapper = await mountSuspended(PastPage)
      expect(currentWrapper.text()).toContain('No past on-call periods.')
    })
  })

  describe('past periods list', () => {
    it('renders an OnCallPeriodCard for each past period', async () => {
      pastPeriodsRef.value = [pastPeriod]
      currentWrapper = await mountSuspended(PastPage)
      expect(currentWrapper.text()).toContain('Past')
    })
  })

  describe('pagination', () => {
    it('shows a pagination spinner when more periods are available', async () => {
      pastPeriodsRef.value = [pastPeriod]
      hasMoreRef.value = true
      currentWrapper = await mountSuspended(PastPage)
      expect(currentWrapper.html()).toContain('animate-spin')
    })

    it('does not show a pagination spinner when all periods are loaded', async () => {
      pastPeriodsRef.value = [pastPeriod]
      hasMoreRef.value = false
      currentWrapper = await mountSuspended(PastPage)
      expect(currentWrapper.html()).not.toContain('animate-spin')
    })
  })

  describe('delete flow', () => {
    it('opens the delete modal when a card delete button is clicked', async () => {
      pastPeriodsRef.value = [pastPeriod]
      currentWrapper = await mountSuspended(PastPage)
      await currentWrapper.find('[aria-label="Delete period"]').trigger('click')
      await flushPromises()
      expect(deleteModalOpenRef.value).toBe(true)
      expect(deletingPeriodRef.value).toEqual(pastPeriod)
      expect(document.body.textContent).toContain('Delete on-call period')
    })

    it('calls remove with the deleting period id when confirmed', async () => {
      pastPeriodsRef.value = [pastPeriod]
      currentWrapper = await mountSuspended(PastPage)
      await currentWrapper.find('[aria-label="Delete period"]').trigger('click')
      await flushPromises()

      const deleteButton = Array.from(document.body.querySelectorAll('button')).find(
        b => b.textContent?.trim() === 'Delete'
      )
      await deleteButton?.click()
      await flushPromises()

      expect(mockRemove).toHaveBeenCalledWith(pastPeriod.id)
    })
  })

  describe('edit flow', () => {
    it('navigates to the edit page when a card edit button is clicked', async () => {
      pastPeriodsRef.value = [pastPeriod]
      currentWrapper = await mountSuspended(PastPage)
      await currentWrapper.find('[aria-label="Edit period"]').trigger('click')
      expect(mockNavigateTo).toHaveBeenCalledWith(`/oncall/${pastPeriod.id}/edit`)
    })
  })
})
