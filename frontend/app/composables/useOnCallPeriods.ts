import type { OnCallPeriodResponse, CreateOnCallPeriodRequest, UpdateOnCallPeriodRequest } from '~/types/onCallPeriod'
import { getPeriodStatus } from '~/utils/dates'

export function useOnCallPeriods() {
  const config = useRuntimeConfig()
  const toast = useToast()

  const periods = ref<OnCallPeriodResponse[]>([])
  const pending = ref(false)
  const error = ref<Error | null>(null)

  const dialogOpen = ref(false)
  const dialogMode = ref<'create' | 'edit'>('create')
  const editingPeriod = ref<OnCallPeriodResponse | null>(null)

  const deleteModalOpen = ref(false)
  const deletingPeriod = ref<OnCallPeriodResponse | null>(null)

  const activePeriods = computed(() => {
    return periods.value
      .filter(p => {
        const status = getPeriodStatus(p.startDateTime, p.endDateTime)
        return status === 'active' || status === 'scheduled'
      })
  })

  const pastPeriods = computed(() => {
    return periods.value
      .filter(p => {
        const status = getPeriodStatus(p.startDateTime, p.endDateTime)
        return status === 'past'
      })
  })

  async function fetchPeriods(): Promise<void> {
    pending.value = true
    error.value = null
    try {
      const response = await $fetch<{ periods: OnCallPeriodResponse[] }>('/api/v1/oncall-periods', {
        baseURL: config.public.apiBase
      })
      periods.value = response.periods
    } catch (err) {
      error.value = err instanceof Error ? err : new Error('Failed to fetch periods')
    } finally {
      pending.value = false
    }
  }

  function openCreateDialog(): void {
    dialogMode.value = 'create'
    editingPeriod.value = null
    dialogOpen.value = true
  }

  function openEditDialog(period: OnCallPeriodResponse): void {
    dialogMode.value = 'edit'
    editingPeriod.value = period
    dialogOpen.value = true
  }

  function closeDialog(): void {
    dialogOpen.value = false
    editingPeriod.value = null
  }

  function openDeleteModal(period: OnCallPeriodResponse): void {
    deletingPeriod.value = period
    deleteModalOpen.value = true
  }

  function closeDeleteModal(): void {
    deleteModalOpen.value = false
    deletingPeriod.value = null
  }

  async function create(request: CreateOnCallPeriodRequest): Promise<void> {
    try {
      await $fetch('/api/v1/oncall-periods', {
        baseURL: config.public.apiBase,
        method: 'POST',
        body: request
      })
      await fetchPeriods()
      closeDialog()
      toast.add({
        title: 'On-call period created',
        color: 'success',
        icon: 'i-lucide-check'
      })
    } catch (err: unknown) {
      throw err
    }
  }

  async function update(id: number, request: UpdateOnCallPeriodRequest): Promise<void> {
    try {
      await $fetch(`/api/v1/oncall-periods/${id}`, {
        baseURL: config.public.apiBase,
        method: 'PUT',
        body: request
      })
      await fetchPeriods()
      closeDialog()
      toast.add({
        title: 'On-call period updated',
        color: 'success',
        icon: 'i-lucide-check'
      })
    } catch (err: unknown) {
      throw err
    }
  }

  async function remove(id: number): Promise<void> {
    try {
      await $fetch(`/api/v1/oncall-periods/${id}`, {
        baseURL: config.public.apiBase,
        method: 'DELETE'
      })
      await fetchPeriods()
      closeDeleteModal()
      toast.add({
        title: 'On-call period deleted',
        color: 'success',
        icon: 'i-lucide-check'
      })
    } catch (err: unknown) {
      toast.add({
        title: 'Failed to delete period',
        description: extractErrorDetail(err),
        color: 'error',
        icon: 'i-lucide-x'
      })
    }
  }

  return {
    periods,
    pending,
    error,
    activePeriods,
    pastPeriods,
    dialogOpen,
    dialogMode,
    editingPeriod,
    deleteModalOpen,
    deletingPeriod,
    fetchPeriods,
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
