import type { IncidentResponse, CreateIncidentRequest, UpdateIncidentRequest } from '~/types/incident'

export function useIncidents(onCallPeriodId: number) {
  const config = useRuntimeConfig()
  const toast = useToast()

  const incidents = ref<IncidentResponse[]>([])
  const pending = ref(false)
  const error = ref<Error | null>(null)

  const dialogOpen = ref(false)
  const dialogMode = ref<'create' | 'edit'>('create')
  const editingIncident = ref<IncidentResponse | null>(null)

  const deleteModalOpen = ref(false)
  const deletingIncident = ref<IncidentResponse | null>(null)

  async function fetchById(id: number): Promise<IncidentResponse> {
    return await $fetch<IncidentResponse>(`/api/v1/incidents/${id}`, {
      baseURL: config.public.apiBase,
    })
  }

  async function fetchIncidents(): Promise<void> {
    pending.value = true
    error.value = null
    try {
      const response = await $fetch<{ incidents: IncidentResponse[] }>('/api/v1/incidents', {
        baseURL: config.public.apiBase,
        params: { onCallPeriodId }
      })
      incidents.value = response.incidents
    } catch (err) {
      error.value = err instanceof Error ? err : new Error('Failed to fetch incidents')
    } finally {
      pending.value = false
    }
  }

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
      await $fetch('/api/v1/incidents', {
        baseURL: config.public.apiBase,
        method: 'POST',
        body: request
      })
      await fetchIncidents()
      closeDialog()
      toast.add({
        title: 'Incident logged',
        color: 'success',
        icon: 'i-lucide-check'
      })
    } catch (err: unknown) {
      const detail = extractErrorDetail(err)
      toast.add({
        title: 'Failed to log incident',
        description: detail,
        color: 'error',
        icon: 'i-lucide-x'
      })
    }
  }

  async function update(id: number, request: UpdateIncidentRequest): Promise<void> {
    try {
      await $fetch(`/api/v1/incidents/${id}`, {
        baseURL: config.public.apiBase,
        method: 'PUT',
        body: request
      })
      await fetchIncidents()
      closeDialog()
      toast.add({
        title: 'Incident updated',
        color: 'success',
        icon: 'i-lucide-check'
      })
    } catch (err: unknown) {
      const detail = extractErrorDetail(err)
      toast.add({
        title: 'Failed to update incident',
        description: detail,
        color: 'error',
        icon: 'i-lucide-x'
      })
    }
  }

  async function remove(id: number): Promise<void> {
    try {
      await $fetch(`/api/v1/incidents/${id}`, {
        baseURL: config.public.apiBase,
        method: 'DELETE'
      })
      await fetchIncidents()
      closeDeleteModal()
      toast.add({
        title: 'Incident deleted',
        color: 'success',
        icon: 'i-lucide-check'
      })
    } catch (err: unknown) {
      const detail = extractErrorDetail(err)
      toast.add({
        title: 'Failed to delete incident',
        description: detail,
        color: 'error',
        icon: 'i-lucide-x'
      })
    }
  }

  return {
    incidents,
    pending,
    error,
    dialogOpen,
    dialogMode,
    editingIncident,
    deleteModalOpen,
    deletingIncident,
    fetchIncidents,
    fetchById,
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
