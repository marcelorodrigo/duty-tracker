import { describe, it, expect } from 'vitest'
import { flushPromises } from '@vue/test-utils'
import { useIncidents } from '~/composables/useIncidents'
import { withComposable } from '../utils/test-composable'
import { setupFetchMock } from '../utils/mock-fetch'
import { mockNuxtImport } from '@nuxt/test-utils/runtime'
import { mockFetch } from '../utils/mock-ofetch'
import { buildIncident } from '../utils/factories'
import type { CreateIncidentRequest, UpdateIncidentRequest } from '~/types/incident'

mockNuxtImport('$fetch', async () => {
  const { mockFetch } = await import('../utils/mock-ofetch')
  return mockFetch
})

const mockIncident = buildIncident()

setupFetchMock({ content: [] })

describe('useIncidents', () => {
  describe('initial state', () => {
    it('starts with empty incidents, pending false, no error, dialogs closed', async () => {
      const composable = await withComposable(() => useIncidents(10))
      await flushPromises()

      expect(composable.incidents.value).toEqual([])
      expect(composable.pending.value).toBe(false)
      expect(composable.error.value).toBeNull()
      expect(composable.dialogOpen.value).toBe(false)
      expect(composable.deleteModalOpen.value).toBe(false)
    })
  })

  describe('list query', () => {
    it('fetches via GET /api/v1/incidents with onCallPeriodId param', async () => {
      await withComposable(() => useIncidents(42))
      await flushPromises()

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/v1/incidents',
        expect.objectContaining({ params: expect.objectContaining({ onCallPeriodId: 42 }) })
      )
    })

    it('populates incidents on success', async () => {
      mockFetch.mockResolvedValue({ content: [mockIncident] })
      const composable = await withComposable(() => useIncidents(10))
      await flushPromises()

      expect(composable.incidents.value).toEqual([mockIncident])
    })

    it('sets pending true while loading and false after', async () => {
      let resolveFetch!: (value: unknown) => void
      const deferred = new Promise<unknown>((resolve) => {
        resolveFetch = resolve
      })
      mockFetch.mockReturnValue(deferred)
      const { pending } = await withComposable(() => useIncidents(10))

      expect(pending.value).toBe(true)
      resolveFetch({ content: [] })
      await flushPromises()
      expect(pending.value).toBe(false)
    })

    it('surfaces the error when the request fails', async () => {
      mockFetch.mockRejectedValue(new Error('Network error'))
      const { error, incidents } = await withComposable(() => useIncidents(10))
      await flushPromises()

      expect(error.value).toBeInstanceOf(Error)
      expect(incidents.value).toEqual([])
    })
  })

  describe('dialog state', () => {
    it('openCreateDialog sets mode to create and opens dialog', async () => {
      const { openCreateDialog, dialogOpen, dialogMode, editingIncident } = await withComposable(() => useIncidents(10))

      openCreateDialog()

      expect(dialogOpen.value).toBe(true)
      expect(dialogMode.value).toBe('create')
      expect(editingIncident.value).toBeNull()
    })

    it('openEditDialog sets mode to edit, stores incident, and opens dialog', async () => {
      const { openEditDialog, dialogOpen, dialogMode, editingIncident } = await withComposable(() => useIncidents(10))

      openEditDialog(mockIncident)

      expect(dialogOpen.value).toBe(true)
      expect(dialogMode.value).toBe('edit')
      expect(editingIncident.value).toEqual(mockIncident)
    })

    it('closeDialog closes dialog and clears editingIncident', async () => {
      const composable = await withComposable(() => useIncidents(10))
      composable.openEditDialog(mockIncident)

      composable.closeDialog()

      expect(composable.dialogOpen.value).toBe(false)
      expect(composable.editingIncident.value).toBeNull()
    })
  })

  describe('delete modal state', () => {
    it('openDeleteModal sets deletingIncident and opens modal', async () => {
      const { openDeleteModal, deleteModalOpen, deletingIncident } = await withComposable(() => useIncidents(10))

      openDeleteModal(mockIncident)

      expect(deleteModalOpen.value).toBe(true)
      expect(deletingIncident.value).toEqual(mockIncident)
    })

    it('closeDeleteModal closes modal and clears deletingIncident', async () => {
      const composable = await withComposable(() => useIncidents(10))
      composable.openDeleteModal(mockIncident)

      composable.closeDeleteModal()

      expect(composable.deleteModalOpen.value).toBe(false)
      expect(composable.deletingIncident.value).toBeNull()
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
      const composable = await withComposable(() => useIncidents(10))
      mockFetch.mockResolvedValueOnce(undefined) // POST
      mockFetch.mockResolvedValueOnce({ content: [] }) // refetch

      await composable.create(createRequest)
      await flushPromises()

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/v1/incidents',
        expect.objectContaining({ method: 'POST', body: createRequest })
      )
    })

    it('refreshes incidents and closes dialog on success', async () => {
      const composable = await withComposable(() => useIncidents(10))
      composable.openCreateDialog()
      mockFetch.mockResolvedValueOnce(undefined) // POST
      mockFetch.mockResolvedValueOnce({ content: [mockIncident] }) // refetch

      await composable.create(createRequest)
      await flushPromises()

      expect(composable.incidents.value).toEqual([mockIncident])
      expect(composable.dialogOpen.value).toBe(false)
    })

    it('does not close dialog on failure', async () => {
      const composable = await withComposable(() => useIncidents(10))
      composable.openCreateDialog()
      mockFetch.mockRejectedValueOnce(new Error('Server error'))

      await composable.create(createRequest)
      await flushPromises()

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
      const composable = await withComposable(() => useIncidents(10))
      mockFetch.mockResolvedValueOnce(undefined) // PUT
      mockFetch.mockResolvedValueOnce({ content: [] }) // refetch

      await composable.update(1, updateRequest)
      await flushPromises()

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/v1/incidents/1',
        expect.objectContaining({ method: 'PUT', body: updateRequest })
      )
    })

    it('refreshes incidents and closes dialog on success', async () => {
      const composable = await withComposable(() => useIncidents(10))
      composable.openEditDialog(mockIncident)
      mockFetch.mockResolvedValueOnce(undefined) // PUT
      mockFetch.mockResolvedValueOnce({ content: [mockIncident] }) // refetch

      await composable.update(1, updateRequest)
      await flushPromises()

      expect(composable.dialogOpen.value).toBe(false)
    })

    it('does not close dialog on failure', async () => {
      const composable = await withComposable(() => useIncidents(10))
      composable.openEditDialog(mockIncident)
      mockFetch.mockRejectedValueOnce(new Error('Server error'))

      await composable.update(1, updateRequest)
      await flushPromises()

      expect(composable.dialogOpen.value).toBe(true)
    })
  })

  describe('fetchById()', () => {
    it('calls GET to the correct endpoint with the incident id', async () => {
      const composable = await withComposable(() => useIncidents(10))
      mockFetch.mockResolvedValueOnce(mockIncident)

      await composable.fetchById(5)

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/v1/incidents/5',
        expect.any(Object)
      )
    })

    it('returns the incident response from the API', async () => {
      const composable = await withComposable(() => useIncidents(10))
      mockFetch.mockResolvedValueOnce(mockIncident)

      const result = await composable.fetchById(5)

      expect(result).toEqual(mockIncident)
    })
  })

  describe('remove()', () => {
    it('calls DELETE to the correct endpoint', async () => {
      const composable = await withComposable(() => useIncidents(10))
      mockFetch.mockResolvedValueOnce(undefined) // DELETE
      mockFetch.mockResolvedValueOnce({ content: [] }) // refetch

      await composable.remove(1)
      await flushPromises()

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/v1/incidents/1',
        expect.objectContaining({ method: 'DELETE' })
      )
    })

    it('refreshes incidents and closes delete modal on success', async () => {
      const composable = await withComposable(() => useIncidents(10))
      composable.openDeleteModal(mockIncident)
      mockFetch.mockResolvedValueOnce(undefined) // DELETE
      mockFetch.mockResolvedValueOnce({ content: [] }) // refetch

      await composable.remove(1)
      await flushPromises()

      expect(composable.deleteModalOpen.value).toBe(false)
    })

    it('does not close delete modal on failure', async () => {
      const composable = await withComposable(() => useIncidents(10))
      composable.openDeleteModal(mockIncident)
      mockFetch.mockRejectedValueOnce(new Error('Server error'))

      await composable.remove(1)
      await flushPromises()

      expect(composable.deleteModalOpen.value).toBe(true)
    })
  })
})
