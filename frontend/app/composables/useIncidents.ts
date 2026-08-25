import type { IncidentResponse, CreateIncidentRequest, UpdateIncidentRequest } from '~/types/incident'
import { computed, ref } from 'vue'
import { useQuery } from '@pinia/colada'
import {
  incidentsQuery,
  useCreateIncident,
  useUpdateIncident,
  useDeleteIncident
} from '~/queries/incidents'

export function useIncidents(onCallPeriodId: number) {
  const {
    state: incidentsState,
    data: incidentsData,
    asyncStatus
  } = useQuery(() => ({ ...incidentsQuery(onCallPeriodId) }))

  const incidents = computed<IncidentResponse[]>(() => incidentsData.value?.incidents ?? [])
  const pending = computed(() => asyncStatus.value === 'loading')
  const error = computed(() => incidentsState.value.error as Error | null)

  const dialogOpen = ref(false)
  const dialogMode = ref<'create' | 'edit'>('create')
  const editingIncident = ref<IncidentResponse | null>(null)

  const deleteModalOpen = ref(false)
  const deletingIncident = ref<IncidentResponse | null>(null)

  async function fetchById(id: number): Promise<IncidentResponse> {
    return await $fetch<IncidentResponse>(`/api/v1/incidents/${id}`, {
      baseURL: useRuntimeConfig().public.apiBase
    })
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

  const { mutateAsync: createMutation } = useCreateIncident()
  const { mutateAsync: updateMutation } = useUpdateIncident()
  const { mutateAsync: deleteMutation } = useDeleteIncident()

  async function create(request: CreateIncidentRequest): Promise<void> {
    try {
      await createMutation(request)
      closeDialog()
    } catch {
      // error toast is shown by the mutation's onError handler
    }
  }

  async function update(id: number, request: UpdateIncidentRequest): Promise<void> {
    try {
      await updateMutation({ id, request, onCallPeriodId })
      closeDialog()
    } catch {
      // error toast is shown by the mutation's onError handler
    }
  }

  async function remove(id: number): Promise<void> {
    try {
      await deleteMutation({ id, onCallPeriodId })
      closeDeleteModal()
    } catch {
      // error toast is shown by the mutation's onError handler
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
