import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import { defineComponent } from 'vue'
import { flushPromises } from '@vue/test-utils'
import { useIncidents } from '~/composables/useIncidents'
import type { IncidentResponse, CreateIncidentRequest, UpdateIncidentRequest } from '~/types/incident'

const mockFetch = vi.fn()

beforeEach(() => {
  vi.stubGlobal('$fetch', mockFetch)
  mockFetch.mockResolvedValue({ incidents: [] })
})

afterEach(() => {
  vi.unstubAllGlobals()
})

async function withComposable(periodId: number): Promise<ReturnType<typeof useIncidents>> {
  let composable!: ReturnType<typeof useIncidents>

  await mountSuspended(defineComponent({
    setup() {
      composable = useIncidents(periodId)
      return () => null
    }
  }))

  await flushPromises()
  return composable
}

const mockIncident: IncidentResponse = {
  id: 1,
  onCallPeriodId: 10,
  name: 'Database failover',
  startDateTime: '2026-04-03T02:30:00',
  endDateTime: '2026-04-03T04:15:00',
  createdAt: '2026-04-03T10:00:00Z'
}

describe('useIncidents', () => {
  describe('initial state', () => {
    it('starts with empty incidents, pending false, no error, dialogs closed', async () => {
      const composable = await withComposable(10)

      expect(composable.incidents.value).toEqual([])
      expect(composable.pending.value).toBe(false)
      expect(composable.error.value).toBeNull()
      expect(composable.dialogOpen.value).toBe(false)
      expect(composable.deleteModalOpen.value).toBe(false)
    })
  })

  describe('dialog state', () => {
    it('openCreateDialog sets mode to create and opens dialog', async () => {
      const { openCreateDialog, dialogOpen, dialogMode, editingIncident } = await withComposable(10)

      openCreateDialog()

      expect(dialogOpen.value).toBe(true)
      expect(dialogMode.value).toBe('create')
      expect(editingIncident.value).toBeNull()
    })

    it('openEditDialog sets mode to edit, stores incident, and opens dialog', async () => {
      const { openEditDialog, dialogOpen, dialogMode, editingIncident } = await withComposable(10)

      openEditDialog(mockIncident)

      expect(dialogOpen.value).toBe(true)
      expect(dialogMode.value).toBe('edit')
      expect(editingIncident.value).toEqual(mockIncident)
    })

    it('closeDialog closes dialog and clears editingIncident', async () => {
      const composable = await withComposable(10)
      composable.openEditDialog(mockIncident)

      composable.closeDialog()

      expect(composable.dialogOpen.value).toBe(false)
      expect(composable.editingIncident.value).toBeNull()
    })
  })

  describe('delete modal state', () => {
    it('openDeleteModal sets deletingIncident and opens modal', async () => {
      const { openDeleteModal, deleteModalOpen, deletingIncident } = await withComposable(10)

      openDeleteModal(mockIncident)

      expect(deleteModalOpen.value).toBe(true)
      expect(deletingIncident.value).toEqual(mockIncident)
    })

    it('closeDeleteModal closes modal and clears deletingIncident', async () => {
      const composable = await withComposable(10)
      composable.openDeleteModal(mockIncident)

      composable.closeDeleteModal()

      expect(composable.deleteModalOpen.value).toBe(false)
      expect(composable.deletingIncident.value).toBeNull()
    })
  })

  describe('fetchIncidents()', () => {
    it('populates incidents on success', async () => {
      const composable = await withComposable(10)
      mockFetch.mockResolvedValueOnce({ incidents: [mockIncident] })

      await composable.fetchIncidents()

      expect(composable.incidents.value).toEqual([mockIncident])
    })

    it('calls the correct endpoint with onCallPeriodId param', async () => {
      const composable = await withComposable(42)
      mockFetch.mockResolvedValueOnce({ incidents: [] })

      await composable.fetchIncidents()

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/v1/incidents',
        expect.objectContaining({ params: { onCallPeriodId: 42 } })
      )
    })

    it('sets pending true during fetch and false after', async () => {
      const composable = await withComposable(10)
      mockFetch.mockResolvedValueOnce({ incidents: [] })

      const fetchPromise = composable.fetchIncidents()
      expect(composable.pending.value).toBe(true)
      await fetchPromise
      expect(composable.pending.value).toBe(false)
    })

    it('sets error on failure', async () => {
      const composable = await withComposable(10)
      mockFetch.mockRejectedValueOnce(new Error('Network error'))

      await composable.fetchIncidents()

      expect(composable.error.value).toBeInstanceOf(Error)
    })

    it('wraps non-Error rejections in an Error', async () => {
      const composable = await withComposable(10)
      mockFetch.mockRejectedValueOnce('plain string')

      await composable.fetchIncidents()

      expect(composable.error.value?.message).toBe('Failed to fetch incidents')
    })
  })

  describe('create()', () => {
    const createRequest: CreateIncidentRequest = {
      onCallPeriodId: 10,
      name: 'New incident',
      startDateTime: '2026-04-03T02:30:00',
      endDateTime: '2026-04-03T04:15:00'
    }

    it('calls POST to /api/v1/incidents with the request body', async () => {
      const composable = await withComposable(10)
      mockFetch.mockResolvedValueOnce(undefined) // POST
      mockFetch.mockResolvedValueOnce({ incidents: [] }) // fetchIncidents

      await composable.create(createRequest)

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/v1/incidents',
        expect.objectContaining({ method: 'POST', body: createRequest })
      )
    })

    it('refreshes incidents and closes dialog on success', async () => {
      const composable = await withComposable(10)
      composable.openCreateDialog()
      mockFetch.mockResolvedValueOnce(undefined) // POST
      mockFetch.mockResolvedValueOnce({ incidents: [mockIncident] }) // fetchIncidents

      await composable.create(createRequest)

      expect(composable.incidents.value).toEqual([mockIncident])
      expect(composable.dialogOpen.value).toBe(false)
    })

    it('does not close dialog on failure', async () => {
      const composable = await withComposable(10)
      composable.openCreateDialog()
      mockFetch.mockRejectedValueOnce(new Error('Server error'))

      await composable.create(createRequest)

      expect(composable.dialogOpen.value).toBe(true)
    })
  })

  describe('update()', () => {
    const updateRequest: UpdateIncidentRequest = {
      name: 'Updated incident',
      startDateTime: '2026-04-03T02:30:00',
      endDateTime: '2026-04-03T05:00:00'
    }

    it('calls PUT to the correct endpoint', async () => {
      const composable = await withComposable(10)
      mockFetch.mockResolvedValueOnce(undefined) // PUT
      mockFetch.mockResolvedValueOnce({ incidents: [] }) // fetchIncidents

      await composable.update(1, updateRequest)

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/v1/incidents/1',
        expect.objectContaining({ method: 'PUT', body: updateRequest })
      )
    })

    it('refreshes incidents and closes dialog on success', async () => {
      const composable = await withComposable(10)
      composable.openEditDialog(mockIncident)
      mockFetch.mockResolvedValueOnce(undefined) // PUT
      mockFetch.mockResolvedValueOnce({ incidents: [mockIncident] }) // fetchIncidents

      await composable.update(1, updateRequest)

      expect(composable.dialogOpen.value).toBe(false)
    })

    it('does not close dialog on failure', async () => {
      const composable = await withComposable(10)
      composable.openEditDialog(mockIncident)
      mockFetch.mockRejectedValueOnce(new Error('Server error'))

      await composable.update(1, updateRequest)

      expect(composable.dialogOpen.value).toBe(true)
    })
  })

  describe('remove()', () => {
    it('calls DELETE to the correct endpoint', async () => {
      const composable = await withComposable(10)
      mockFetch.mockResolvedValueOnce(undefined) // DELETE
      mockFetch.mockResolvedValueOnce({ incidents: [] }) // fetchIncidents

      await composable.remove(1)

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/v1/incidents/1',
        expect.objectContaining({ method: 'DELETE' })
      )
    })

    it('refreshes incidents and closes delete modal on success', async () => {
      const composable = await withComposable(10)
      composable.openDeleteModal(mockIncident)
      mockFetch.mockResolvedValueOnce(undefined) // DELETE
      mockFetch.mockResolvedValueOnce({ incidents: [] }) // fetchIncidents

      await composable.remove(1)

      expect(composable.deleteModalOpen.value).toBe(false)
    })

    it('does not close delete modal on failure', async () => {
      const composable = await withComposable(10)
      composable.openDeleteModal(mockIncident)
      mockFetch.mockRejectedValueOnce(new Error('Server error'))

      await composable.remove(1)

      expect(composable.deleteModalOpen.value).toBe(true)
    })
  })
})
