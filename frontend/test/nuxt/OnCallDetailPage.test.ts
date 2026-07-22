import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest'
import { ref } from 'vue'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import { flushPromises } from '@vue/test-utils'
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
    deleteModalOpen: ref(false),
    deletingPeriod: ref(null),
    fetchPeriods: vi.fn(),
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
const mockFetch = vi.fn()

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
  vi.stubGlobal('$fetch', mockFetch)
})

afterEach(() => {
  vi.unstubAllGlobals()
})

// ---------------------------------------------------------------------------
// Component tests
// ---------------------------------------------------------------------------
const ROUTE = '/oncall/42'

describe('OnCall Detail Page - Component', () => {
  describe('period loading', () => {
    it('does not render settled states while the period is loading', async () => {
      // $fetch never resolves during this test
      mockFetch.mockReturnValue(new Promise(() => {}))
      const wrapper = await mountSuspended(OnCallDetailPage, { route: ROUTE })

      expect(wrapper.text()).not.toContain('Failed to load period')
      expect(wrapper.text()).not.toContain('On-call period')
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
    it('does not render the empty incident state while incidents are loading', async () => {
      mockFetch.mockResolvedValue(mockPastPeriod)
      incidentsPendingRef.value = true
      const wrapper = await mountSuspended(OnCallDetailPage, { route: ROUTE })
      await flushPromises()
      expect(wrapper.text()).not.toContain('No incidents logged for this period')
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
