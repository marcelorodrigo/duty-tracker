<template>
  <div class="max-w-4xl mx-auto p-4">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold">On-Call Periods</h1>
      <UButton @click="showNewModal = true">+ New Period</UButton>
    </div>
    <USkeleton v-if="loading" class="h-32 w-full" />
    <div v-else class="overflow-x-auto">
      <UTable :rows="oncallStore.periods" :columns="columns">
        <template #startDateTime-data="{ row }">
          {{ formatDate(row.startDateTime) }}
        </template>
        <template #endDateTime-data="{ row }">
          {{ formatDate(row.endDateTime) }}
        </template>
        <template #actions-data="{ row }">
          <div class="flex gap-2">
            <UButton size="sm" :to="`/oncall/${row.id}`">View</UButton>
            <UButton size="sm" color="error" @click="confirmDelete(row)">Delete</UButton>
          </div>
        </template>
      </UTable>
    </div>

    <UModal v-model:open="showNewModal" title="New On-Call Period">
      <template #body>
        <div class="p-4">
          <PeriodForm @submit="onCreatePeriod" />
        </div>
      </template>
    </UModal>

    <ConfirmDeleteModal
      v-model:open="showDeleteModal"
      :item-label="deleteTarget?.startDateTime ?? ''"
      @confirmed="onDeleteConfirmed"
    />
  </div>
</template>

<script setup lang="ts">
import type { OnCallPeriod } from '~/stores/oncall'

const oncallStore = useOnCallStore()
const showNewModal = ref(false)
const showDeleteModal = ref(false)
const deleteTarget = ref<OnCallPeriod | null>(null)
const loading = ref(false)

const columns = [
  { key: 'startDateTime', label: 'Start' },
  { key: 'endDateTime', label: 'End' },
  { key: 'actions', label: '' },
]

function formatDate(dt: string) {
  return new Date(dt).toLocaleString('nl-NL', { dateStyle: 'medium', timeStyle: 'short' })
}

async function onCreatePeriod(data: { startDateTime: string; endDateTime: string }) {
  await oncallStore.createPeriod(data)
  showNewModal.value = false
}

function confirmDelete(period: OnCallPeriod) {
  deleteTarget.value = period
  showDeleteModal.value = true
}

async function onDeleteConfirmed() {
  if (deleteTarget.value) {
    await oncallStore.deletePeriod(deleteTarget.value.id)
    deleteTarget.value = null
  }
}

onMounted(async () => {
  loading.value = true
  try {
    await oncallStore.fetchPeriods()
  } finally {
    loading.value = false
  }
})
</script>
