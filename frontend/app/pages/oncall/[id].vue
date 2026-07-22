<script setup lang="ts">
import type { OnCallPeriodResponse } from '~/types/onCallPeriod'
import type { CreateIncidentRequest, UpdateIncidentRequest } from '~/types/incident'
import { formatDateTime, getPeriodStatus, getStatusColors } from '~/utils/dates'

const route = useRoute()
const config = useRuntimeConfig()
const periodId = Number(route.params.id)

const period = ref<OnCallPeriodResponse | null>(null)
const periodPending = ref(false)
const periodError = ref<Error | null>(null)
const incidentSubmitting = shallowRef(false)
const incidentDeleting = shallowRef(false)

const {
  incidents,
  pending: incidentsPending,
  error: incidentsError,
  dialogOpen,
  dialogMode,
  editingIncident,
  deleteModalOpen,
  deletingIncident,
  fetchIncidents,
  openCreateDialog,
  openEditDialog,
  closeDialog,
  openDeleteModal,
  closeDeleteModal,
  create,
  update,
  remove
} = useIncidents(periodId)

async function fetchPeriod(): Promise<void> {
  periodPending.value = true
  periodError.value = null
  try {
    period.value = await $fetch<OnCallPeriodResponse>(`/api/v1/oncall-periods/${periodId}`, {
      baseURL: config.public.apiBase
    })
  } catch (err) {
    periodError.value = err instanceof Error ? err : new Error('Failed to load period')
  } finally {
    periodPending.value = false
  }
}

onMounted(() => {
  fetchPeriod()
  fetchIncidents()
})

async function handleDialogSubmit(request: CreateIncidentRequest | UpdateIncidentRequest): Promise<void> {
  incidentSubmitting.value = true
  try {
    if (dialogMode.value === 'create') {
      await create(request as CreateIncidentRequest)
    } else if (editingIncident.value) {
      await update(editingIncident.value.id, request as UpdateIncidentRequest)
    }
  } finally {
    incidentSubmitting.value = false
  }
}

async function handleDeleteConfirm(): Promise<void> {
  if (!deletingIncident.value) return

  incidentDeleting.value = true
  try {
    await remove(deletingIncident.value.id)
  } finally {
    incidentDeleting.value = false
  }
}

const status = computed(() => period.value ? getPeriodStatus(period.value.startDateTime, period.value.endDateTime) : 'past')

const statusText = computed(() => {
  switch (status.value) {
    case 'scheduled':
      return 'Scheduled'
    case 'active':
      return 'Active'
    case 'past':
      return 'Past'
  }
})

const colors = computed(() => getStatusColors(status.value))

// Check if there's a child route (e.g., /oncall/[id]/report)
const hasChildRoute = computed(() => route.path !== `/oncall/${periodId}`)
</script>

<template>
  <!-- Show period content only when on the main route (no child route) -->
  <AppPageShell v-if="!hasChildRoute">
    <template #navigation>
      <UButton
        to="/"
        icon="i-lucide-arrow-left"
        variant="ghost"
        color="neutral"
        aria-label="Back to periods"
      />
    </template>

    <template #title>
      On-call period
    </template>

    <template
      v-if="period && status !== 'scheduled'"
      #actions
    >
      <UButton
        :to="`/oncall/${periodId}/report`"
        icon="i-lucide-file-text"
        variant="outline"
        color="neutral"
      >
        Generate Report
      </UButton>
      <UButton
        :to="`/oncall/${periodId}/earnings`"
        icon="i-lucide-coins"
        variant="outline"
        color="neutral"
      >
        My Earnings
      </UButton>
    </template>

    <!-- Loading -->
    <div
      v-if="periodPending"
      class="flex justify-center py-12"
    >
      <UIcon
        name="i-lucide-loader-circle"
        class="animate-spin text-2xl text-(--ui-text-muted)"
      />
    </div>

    <!-- Error -->
    <UAlert
      v-else-if="periodError"
      color="error"
      icon="i-lucide-alert-circle"
      title="Failed to load period"
      description="Please go back and try again."
    />

    <!-- Period loaded -->
    <template v-else-if="period">
      <!-- Period info card -->
      <div class="border border-(--ui-border) rounded-lg p-5 mb-8">
        <div class="flex items-center gap-3 mb-3">
          <span
            class="inline-flex items-center gap-1.5 text-xs font-medium px-2.5 py-1 rounded-full"
            :class="colors.badge"
          >
            <span
              class="size-1.5 rounded-full"
              :class="colors.dot"
            />
            {{ statusText }}
          </span>
        </div>
        <p class="text-sm font-medium">
          {{ formatDateTime(period.startDateTime) }} → {{ formatDateTime(period.endDateTime) }}
        </p>
        <p
          v-if="period.holidays.length > 0"
          class="text-xs text-(--ui-text-muted) mt-1"
        >
          {{ period.holidays.length }} {{ period.holidays.length === 1 ? 'holiday' : 'holidays' }}
        </p>
      </div>

      <!-- Incidents section -->
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-lg font-semibold">
          Incidents
        </h2>
        <UButton
          v-if="status !== 'scheduled'"
          icon="i-lucide-plus"
          size="sm"
          @click="openCreateDialog"
        >
          Log incident
        </UButton>
      </div>

      <!-- Incidents loading -->
      <div
        v-if="incidentsPending"
        class="flex justify-center py-8"
      >
        <UIcon
          name="i-lucide-loader-circle"
          class="animate-spin text-xl text-(--ui-text-muted)"
        />
      </div>

      <!-- Incidents error -->
      <UAlert
        v-else-if="incidentsError"
        color="error"
        icon="i-lucide-alert-circle"
        title="Failed to load incidents"
        description="Please reload the page to try again."
      />

      <!-- Empty incidents -->
      <div
        v-else-if="incidents.length === 0"
        class="py-8 text-center"
      >
        <UIcon
          name="i-lucide-shield-check"
          class="text-3xl text-(--ui-text-muted) mx-auto mb-2"
        />
        <p class="text-sm text-(--ui-text-muted)">
          No incidents logged for this period.
        </p>
      </div>

      <!-- Incidents list -->
      <div
        v-else
        class="space-y-3"
      >
        <div
          v-for="incident in incidents"
          :key="incident.id"
          class="border border-(--ui-border) rounded-lg p-4 flex items-center justify-between hover:bg-(--ui-bg-elevated) transition-colors"
        >
          <div class="flex-1">
            <p class="text-sm font-medium">
              {{ incident.name }}
            </p>
            <p class="text-xs text-(--ui-text-muted) mt-1">
              {{ formatDateTime(incident.startDateTime) }}–{{ formatDateTime(incident.endDateTime) }}
            </p>
          </div>
          <div class="flex gap-2">
            <UButton
              aria-label="Edit incident"
              icon="i-lucide-pencil"
              variant="ghost"
              size="sm"
              color="neutral"
              @click="openEditDialog(incident)"
            />
            <UButton
              aria-label="Delete incident"
              icon="i-lucide-trash-2"
              variant="ghost"
              size="sm"
              color="error"
              @click="openDeleteModal(incident)"
            />
          </div>
        </div>
      </div>
    </template>
  </AppPageShell>

  <!-- Child route (e.g., report page) -->
  <NuxtPage />

  <!-- Incident Create/Edit Dialog -->
  <IncidentDialog
    v-if="period"
    :open="dialogOpen"
    :mode="dialogMode"
    :incident="editingIncident"
    :on-call-period-id="periodId"
    :on-call-period="period"
    :submitting="incidentSubmitting"
    @close="closeDialog"
    @submit="handleDialogSubmit"
  />

  <!-- Incident Delete Modal -->
  <IncidentDeleteModal
    :open="deleteModalOpen"
    :incident="deletingIncident"
    :deleting="incidentDeleting"
    @close="closeDeleteModal"
    @confirm="handleDeleteConfirm"
  />
</template>
