<template>
  <div class="max-w-4xl mx-auto p-4">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold">Reports</h1>
      <UButton @click="showNewModal = true">+ New Summary</UButton>
    </div>
    <USkeleton v-if="loading" class="h-32 w-full" />
    <div v-else class="overflow-x-auto">
      <UTable :data="reportStore.summaries" :columns="columns">
        <template #periodStart-cell="{ row }">
          {{ row.original.periodStart }} – {{ row.original.periodEnd }}
        </template>
        <template #actions-cell="{ row }">
          <div class="flex gap-2">
            <UButton size="sm" :to="`/report/${row.original.id}`">View</UButton>
            <UButton size="sm" color="error" @click="confirmDelete(row.original)">Delete</UButton>
          </div>
        </template>
      </UTable>
    </div>
    <UModal v-model:open="showNewModal" title="New Registration Summary">
      <template #body>
        <div class="p-4 space-y-3">
          <USelect v-model="newSummary.periodId" :items="periodOptions" value-attribute="value" label-attribute="label" placeholder="Select on-call period" />
          <UInput v-model="newSummary.label" placeholder="Label (optional)" />
          <UButton @click="onCreate" :loading="creating">Create Summary</UButton>
        </div>
      </template>
    </UModal>
    <ConfirmDeleteModal v-model:open="showDeleteModal" :item-label="deleteTarget?.label ?? ''" @confirmed="onDeleteConfirmed" />
  </div>
</template>

<script setup lang="ts">
import type { RegistrationSummary } from '~/stores/report'

const reportStore = useReportStore()
const oncallStore = useOnCallStore()
const showNewModal = ref(false)
const showDeleteModal = ref(false)
const deleteTarget = ref<RegistrationSummary | null>(null)
const creating = ref(false)
const loading = ref(false)
const newSummary = reactive({ periodId: null as number | null, label: '' })

const columns = [
  { accessorKey: 'label', header: 'Label' },
  { accessorKey: 'periodStart', header: 'Period' },
  { accessorKey: 'createdAt', header: 'Created' },
  { accessorKey: 'actions', header: '' },
]

const periodOptions = computed(() =>
  oncallStore.periods.map(p => ({
    value: p.id,
    label: `${p.startDateTime} – ${p.endDateTime}`,
  }))
)

async function onCreate() {
  if (!newSummary.periodId) return
  creating.value = true
  try {
    await reportStore.createSummary({ periodId: newSummary.periodId, label: newSummary.label || undefined })
    showNewModal.value = false
  } finally {
    creating.value = false
  }
}

function confirmDelete(summary: RegistrationSummary) {
  deleteTarget.value = summary
  showDeleteModal.value = true
}

async function onDeleteConfirmed() {
  if (deleteTarget.value) {
    await reportStore.deleteSummary(deleteTarget.value.id)
    deleteTarget.value = null
  }
}

onMounted(async () => {
  loading.value = true
  try {
    await reportStore.fetchSummaries()
    await oncallStore.fetchPeriods()
  } finally {
    loading.value = false
  }
})
</script>
