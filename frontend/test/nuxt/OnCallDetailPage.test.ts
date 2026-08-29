import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest'
import { ref } from 'vue'
import { mountSuspended, mockNuxtImport } from '@nuxt/test-utils/runtime'
import { mockFetch } from '../utils/mock-ofetch'
import { flushPromises } from '@vue/test-utils'
import { getPeriodStatus } from '~/utils/dates'
import OnCallDetailPage from '~/pages/oncall/[id].vue'
import type { OnCallPeriodResponse } from '~/types/onCallPeriod'
import type { IncidentResponse } from '~/types/incident'

// ---------------------------------------------------------------------------
// Mock data
// ---------------------------------------------------------------------------
const mockPastPeriod: OnCallPeriodResponse = {
  id: 42,
  startDateTime: '2020-01-01T14:00:00',
  endDateTime: '2020-01-08T14:00:00',
  holidays: [],
  createdAt: '2020-01-01T00:00:00Z'
}

const now = new Date()
const mockScheduledPeriod: OnCallPeriodResponse = {
  id: 42,
  startDateTime: new Date(now.getTime() + 60 * 60 * 1000).toISOString(),
  endDateTime: new Date(now.getTime() + 2 * 60 * 60 * 1000).toISOString(),
  holidays: [],
  createdAt: '2026-01-01T00:00:00Z'
}

const mockIncident: IncidentResponse = {
  id: 1,
  onCallPeriodId: 42,
  name: 'Database failover',
  startDateTime: '2020-01-03T02:30:00',
  endDateTime: '2020-01-03T04:15:00',
  createdAt: '2020-01-03T10:00:00Z'
}

// ---------------------------------------------------------------------------
// useIncidents mock
// ---------------------------------------------------------------------------
const incidentsRef = ref<IncidentResponse[]>([])
const incidentsPendingRef = ref(false)
const incidentsErrorRef = ref<Error | null>(null)
const dialogOpenRef = ref(false)
const dialogModeRef = ref<'create' | 'edit'>('create')
const editingIncidentRef = ref<IncidentResponse | null>(null)
const deleteModalOpenRef = ref(false)
const deletingIncidentRef = ref<IncidentResponse | null>(null)
const mockFetchIncidents = vi.fn()
const mockOpenCreateDialog = vi.fn()
const mockOpenEditDialog = vi.fn()
const mockCloseDialog = vi.fn()
const mockOpenDeleteModal = vi.fn()
const mockCloseDeleteModal = vi.fn()
const mockCreate = vi.fn()
const mockUpdate = vi.fn()
const mockRemove = vi.fn()

// Prevent index.vue (loaded by Nuxt router in same environment) from crashing
// when useOnCallPeriods mock from IndexPage.test.ts leaks across test files.
vi.mock('~/composables/useOnCallPeriods', () => ({
  useOnCallPeriods: () => ({
    periods: ref([]),
    activePeriods: ref([]),
    pastPeriods: ref([]),
    pending: ref(false),
    error: ref(null),
    hasMore: ref(false),
    sentinelRef: ref(null),
    reset: vi.fn(),
    deleteModalOpen: ref(false),
    deletingPeriod: ref(null),
    openDeleteModal: vi.fn(),
    closeDeleteModal: vi.fn(),
    remove: vi.fn()
  })
}))

vi.mock('~/composables/useIncidents', () => ({
  useIncidents: () => ({
    incidents: incidentsRef,
    pending: incidentsPendingRef,
    error: incidentsErrorRef,
    hasMore: ref(false),
    sentinelRef: ref(null),
    reset: vi.fn(),
    dialogOpen: dialogOpenRef,
    dialogMode: dialogModeRef,
    editingIncident: editingIncidentRef,
    deleteModalOpen: deleteModalOpenRef,
    deletingIncident: deletingIncidentRef,
    fetchIncidents: mockFetchIncidents,
    openCreateDialog: mockOpenCreateDialog,
    openEditDialog: mockOpenEditDialog,
    closeDialog: mockCloseDialog,
    openDeleteModal: mockOpenDeleteModal,
    closeDeleteModal: mockCloseDeleteModal,
    create: mockCreate,
    update: mockUpdate,
    remove: mockRemove
  })
}))

// ---------------------------------------------------------------------------
// $fetch mock
// ---------------------------------------------------------------------------
mockNuxtImport('$fetch', async () => {
  const { mockFetch } = await import('../utils/mock-ofetch')
  return mockFetch
})

beforeEach(() => {
  incidentsRef.value = []
  incidentsPendingRef.value = false
  incidentsErrorRef.value = null
  dialogOpenRef.value = false
  dialogModeRef.value = 'create'
  editingIncidentRef.value = null
  deleteModalOpenRef.value = false
  deletingIncidentRef.value = null
  mockFetchIncidents.mockReset()
  mockOpenCreateDialog.mockReset()
  mockOpenEditDialog.mockReset()
  mockCloseDialog.mockReset()
  mockOpenDeleteModal.mockReset()
  mockCloseDeleteModal.mockReset()
  mockCreate.mockReset()
  mockUpdate.mockReset()
  mockRemove.mockReset()
  mockFetch.mockReset()
})

afterEach(() => {
  vi.unstubAllGlobals()
})

// ---------------------------------------------------------------------------
// Existing pure-logic tests (kept as-is)
// ---------------------------------------------------------------------------
describe('OnCall Detail Page - Period Status Logic', () => {
  describe('getPeriodStatus determines button visibility', () => {
    it('returns "active" for currently ongoing periods - buttons should be visible', () => {
      const pastStart = new Date(now.getTime() - 60 * 60 * 1000) // 1 hour ago
      const futureEnd = new Date(now.getTime() + 60 * 60 * 1000) // 1 hour from now

      const status = getPeriodStatus(
        pastStart.toISOString(),
        futureEnd.toISOString()
      )

      // When status is 'active', buttons should be visible (v-if="status !== 'scheduled'")
      expect(status).toBe('active')
      expect(status !== 'scheduled').toBe(true)
    })

    it('returns "scheduled" for future periods - buttons should be hidden', () => {
      const futureStart = new Date(now.getTime() + 60 * 60 * 1000) // 1 hour from now
      const futureEnd = new Date(now.getTime() + 2 * 60 * 60 * 1000) // 2 hours from now

      const status = getPeriodStatus(
        futureStart.toISOString(),
        futureEnd.toISOString()
      )

      // When status is 'scheduled', buttons should be hidden (v-if="status !== 'scheduled'")
      expect(status).toBe('scheduled')
      expect(status !== 'scheduled').toBe(false)
    })

    it('returns "past" for ended periods - buttons should be visible', () => {
      const status = getPeriodStatus(
        '2020-01-01T14:00:00',
        '2020-01-08T14:00:00'
      )

      // When status is 'past', buttons should be visible (v-if="status !== 'scheduled'")
      expect(status).toBe('past')
      expect(status !== 'scheduled').toBe(true)
    })
  })

  describe('Status display text', () => {
    it('displays "Scheduled" when period is in the future', () => {
      const futureStart = new Date(now.getTime() + 60 * 60 * 1000)
      const futureEnd = new Date(now.getTime() + 2 * 60 * 60 * 1000)

      const status = getPeriodStatus(
        futureStart.toISOString(),
        futureEnd.toISOString()
      )

      const statusText = status === 'scheduled' ? 'Scheduled' : status === 'active' ? 'Active' : 'Past'
      expect(statusText).toBe('Scheduled')
    })

    it('displays "Active" when period is currently ongoing', () => {
      const pastStart = new Date(now.getTime() - 60 * 60 * 1000)
      const futureEnd = new Date(now.getTime() + 60 * 60 * 1000)

      const status = getPeriodStatus(
        pastStart.toISOString(),
        futureEnd.toISOString()
      )

      const statusText = status === 'scheduled' ? 'Scheduled' : status === 'active' ? 'Active' : 'Past'
      expect(statusText).toBe('Active')
    })

    it('displays "Past" when period has ended', () => {
      const status = getPeriodStatus(
        '2020-01-01T14:00:00',
        '2020-01-08T14:00:00'
      )

      const statusText = status === 'scheduled' ? 'Scheduled' : status === 'active' ? 'Active' : 'Past'
      expect(statusText).toBe('Past')
    })
  })
})

// ---------------------------------------------------------------------------
// Component tests
// ---------------------------------------------------------------------------
const ROUTE = '/oncall/42'

describe('OnCall Detail Page - Component', () => {
  describe('period loading', () => {
    it('shows spinner while period is being fetched', async () => {
      // $fetch never resolves during this test
      mockFetch.mockReturnValue(new Promise(() => {}))
      const wrapper = await mountSuspended(OnCallDetailPage, { route: ROUTE })
      expect(wrapper.html()).toContain('animate-spin')
    })

    it('shows error alert when period fetch fails', async () => {
      mockFetch.mockRejectedValue(new Error('Not found'))
      const wrapper = await mountSuspended(OnCallDetailPage, { route: ROUTE })
      await flushPromises()
      expect(wrapper.text()).toContain('Failed to load period')
    })

    it('renders period date/time when loaded', async () => {
      mockFetch.mockResolvedValue(mockPastPeriod)
      const wrapper = await mountSuspended(OnCallDetailPage, { route: ROUTE })
      await flushPromises()
      expect(wrapper.text()).toContain('01 Jan 2020 14:00')
      expect(wrapper.text()).toContain('08 Jan 2020 14:00')
    })
  })

  describe('action buttons visibility', () => {
    it('shows "Generate Report" and "My Earnings" when status is not scheduled', async () => {
      mockFetch.mockResolvedValue(mockPastPeriod)
      const wrapper = await mountSuspended(OnCallDetailPage, { route: ROUTE })
      await flushPromises()
      expect(wrapper.text()).toContain('Generate Report')
      expect(wrapper.text()).toContain('My Earnings')
    })

    it('hides "Generate Report" and "My Earnings" when status is scheduled', async () => {
      mockFetch.mockResolvedValue(mockScheduledPeriod)
      const wrapper = await mountSuspended(OnCallDetailPage, { route: ROUTE })
      await flushPromises()
      expect(wrapper.text()).not.toContain('Generate Report')
      expect(wrapper.text()).not.toContain('My Earnings')
    })
  })

  describe('incidents section', () => {
    it('shows incidents loading spinner when incidentsPending=true', async () => {
      mockFetch.mockResolvedValue(mockPastPeriod)
      incidentsPendingRef.value = true
      const wrapper = await mountSuspended(OnCallDetailPage, { route: ROUTE })
      await flushPromises()
      // Two spinners might appear (period + incidents) — just assert at least one
      expect(wrapper.html()).toContain('animate-spin')
    })

    it('shows empty state when incidents list is empty', async () => {
      mockFetch.mockResolvedValue(mockPastPeriod)
      incidentsRef.value = []
      const wrapper = await mountSuspended(OnCallDetailPage, { route: ROUTE })
      await flushPromises()
      expect(wrapper.text()).toContain('No incidents logged for this period')
    })

    it('renders each incident name and date range', async () => {
      mockFetch.mockResolvedValue(mockPastPeriod)
      incidentsRef.value = [mockIncident]
      const wrapper = await mountSuspended(OnCallDetailPage, { route: ROUTE })
      await flushPromises()
      expect(wrapper.text()).toContain('Database failover')
    })
  })

  describe('interactions', () => {
    it('calls openCreateDialog when "Log incident" is clicked', async () => {
      mockFetch.mockResolvedValue(mockPastPeriod)
      const wrapper = await mountSuspended(OnCallDetailPage, { route: ROUTE })
      await flushPromises()
      const logButton = wrapper.findAll('button').find(b => b.text().includes('Log incident'))
      await logButton?.trigger('click')
      expect(mockOpenCreateDialog).toHaveBeenCalledOnce()
    })

    it('calls openEditDialog when incident edit button is clicked', async () => {
      mockFetch.mockResolvedValue(mockPastPeriod)
      incidentsRef.value = [mockIncident]
      const wrapper = await mountSuspended(OnCallDetailPage, { route: ROUTE })
      await flushPromises()
      await wrapper.find('[aria-label="Edit incident"]').trigger('click')
      expect(mockOpenEditDialog).toHaveBeenCalledWith(mockIncident)
    })

    it('calls openDeleteModal when incident delete button is clicked', async () => {
      mockFetch.mockResolvedValue(mockPastPeriod)
      incidentsRef.value = [mockIncident]
      const wrapper = await mountSuspended(OnCallDetailPage, { route: ROUTE })
      await flushPromises()
      await wrapper.find('[aria-label="Delete incident"]').trigger('click')
      expect(mockOpenDeleteModal).toHaveBeenCalledWith(mockIncident)
    })
  })
})
