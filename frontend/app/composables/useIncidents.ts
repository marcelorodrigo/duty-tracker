import type { IncidentResponse, CreateIncidentRequest, UpdateIncidentRequest } from '~/types/incident'

export function useIncidents(onCallPeriodId: number) {
  const toast = useToast()
  const { $api } = useNuxtApp()

  const { data, pending, error, refresh } = useApiResource<IncidentResponse[]>(
    async () => {
      const response = await $api.get<{ incidents: IncidentResponse[] }>('/incidents', {
        query: { onCallPeriodId }
      })
      return response.incidents
    },
    'Failed to fetch incidents'
  )

  const dialogOpen = ref(false)
  const dialogMode = ref<'create' | 'edit'>('create')
  const editingIncident = ref<IncidentResponse | null>(null)

  const deleteModalOpen = ref(false)
  const deletingIncident = ref<IncidentResponse | null>(null)

  function openCreateDialog(): void {
    dialogMode.value = 'create'
    editingIncident.value = null
    dialogOpen.value = true
  }

  function openEditDialog(incident: IncidentResponse): void {
    dialogMode.value = 'edit'
    editingIncident.value = incident
    dialogOpen.value = true
  }

  function closeDialog(): void {
    dialogOpen.value = false
    editingIncident.value = null
  }

  function openDeleteModal(incident: IncidentResponse): void {
    deletingIncident.value = incident
    deleteModalOpen.value = true
  }

  function closeDeleteModal(): void {
    deleteModalOpen.value = false
    deletingIncident.value = null
  }

  async function create(request: CreateIncidentRequest): Promise<void> {
    try {
      await $api.post('/incidents', request)
      await refresh()
      closeDialog()
      toast.add({
        title: 'Incident logged',
        color: 'success',
        icon: 'i-lucide-check'
      })
    } catch (err: unknown) {
      toast.add({
        title: 'Failed to log incident',
        description: getApiErrorMessage(err),
        color: 'error',
        icon: 'i-lucide-x'
      })
    }
  }

  async function update(id: number, request: UpdateIncidentRequest): Promise<void> {
    try {
      await $api.put(`/incidents/${id}`, request)
      await refresh()
      closeDialog()
      toast.add({
        title: 'Incident updated',
        color: 'success',
        icon: 'i-lucide-check'
      })
    } catch (err: unknown) {
      toast.add({
        title: 'Failed to update incident',
        description: getApiErrorMessage(err),
        color: 'error',
        icon: 'i-lucide-x'
      })
    }
  }

  async function remove(id: number): Promise<void> {
    try {
      await $api.delete(`/incidents/${id}`)
      await refresh()
      closeDeleteModal()
      toast.add({
        title: 'Incident deleted',
        color: 'success',
        icon: 'i-lucide-check'
      })
    } catch (err: unknown) {
      toast.add({
        title: 'Failed to delete incident',
        description: getApiErrorMessage(err),
        color: 'error',
        icon: 'i-lucide-x'
      })
    }
  }

  return {
    data,
    pending,
    error,
    refresh,
    dialogOpen,
    dialogMode,
    editingIncident,
    deleteModalOpen,
    deletingIncident,
    openCreateDialog,
    openEditDialog,
    closeDialog,
    openDeleteModal,
    closeDeleteModal,
    create,
    update,
    remove
  }
}
