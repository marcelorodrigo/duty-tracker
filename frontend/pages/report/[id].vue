<template>
  <div class="max-w-4xl mx-auto p-4" id="report-content">
    <!-- Not Found State -->
    <div v-if="isNotFound" class="text-center py-12">
      <h1 class="text-2xl font-bold text-red-600 mb-4">Report Not Found</h1>
      <p class="text-gray-600 mb-6">The report ID is invalid or malformed.</p>
      <UButton to="/report" variant="soft">Back to Reports</UButton>
    </div>

    <!-- Report Content -->
    <div v-else-if="reportStore.currentSummary">
      <div class="flex items-center justify-between mb-6">
        <div>
          <UButton to="/report" variant="ghost" icon="i-heroicons-arrow-left" class="mb-2" />
          <h1 class="text-2xl font-bold">{{ reportStore.currentSummary.label }}</h1>
          <p class="text-gray-500">{{ reportStore.currentSummary.periodStart }} – {{ reportStore.currentSummary.periodEnd }}</p>
        </div>
        <UButton icon="i-heroicons-printer" @click="printReport">Print / Save as PDF</UButton>
      </div>

      <!-- On-Call Day Entries -->
      <UCard class="mb-6">
        <template #header>
          <div class="flex items-center justify-between">
            <h2 class="font-semibold">On-Call Day Entries</h2>
            <UButton size="sm" @click="showAddOnCallModal = true">+ Add Entry</UButton>
          </div>
        </template>
        <SummaryEntryTable
          :entries="reportStore.currentSummary.onCallEntries"
          type="oncall"
          @edit="onEditOnCallEntry"
          @delete="onDeleteOnCallEntry"
        />
      </UCard>

      <!-- Overtime Entries -->
      <UCard>
        <template #header>
          <div class="flex items-center justify-between">
            <h2 class="font-semibold">Overtime Entries</h2>
            <UButton size="sm" @click="showAddOvertimeModal = true">+ Add Entry</UButton>
          </div>
        </template>
        <SummaryEntryTable
          :entries="reportStore.currentSummary.overtimeEntries"
          type="overtime"
          @edit="onEditOvertimeEntry"
          @delete="onDeleteOvertimeEntry"
        />
      </UCard>
    </div>
    <USkeleton v-else class="h-64 w-full" />

    <!-- Quick Edit Modals -->
    <QuickEditPopup v-model:open="showEditModal" :entry="editTarget" :type="editType" @saved="onSaved" />
    <AddEntryModal v-model:open="showAddOnCallModal" type="oncall" :summary-id="id" @added="onAdded" />
    <AddEntryModal v-model:open="showAddOvertimeModal" type="overtime" :summary-id="id" @added="onAdded" />
  </div>
</template>

<script setup lang="ts">
const route = useRoute()
const reportStore = useReportStore()

// Validate and parse the report ID from the route parameter
function parseAndValidateId(rawId: unknown): number | null {
  const parsed = parseInt(String(rawId), 10)
  return Number.isFinite(parsed) && Number.isInteger(parsed) && parsed > 0 ? parsed : null
}

const validatedId = parseAndValidateId(route.params.id)
const isNotFound = validatedId === null

const showEditModal = ref(false)
const editTarget = ref<unknown>(null)
const editType = ref<'oncall' | 'overtime'>('oncall')
const showAddOnCallModal = ref(false)
const showAddOvertimeModal = ref(false)

// Extract the id from validation, use it safely
const id = validatedId!

function onEditOnCallEntry(row: unknown) {
  editTarget.value = row
  editType.value = 'oncall'
  showEditModal.value = true
}

function onEditOvertimeEntry(row: unknown) {
  editTarget.value = row
  editType.value = 'overtime'
  showEditModal.value = true
}

async function onDeleteOnCallEntry(entryId: number) {
  await reportStore.deleteOnCallEntry(id, entryId)
}

async function onDeleteOvertimeEntry(entryId: number) {
  await reportStore.deleteOvertimeEntry(id, entryId)
}

async function onSaved() {
  await reportStore.fetchSummary(id)
}

async function onAdded() {
   await reportStore.fetchSummary(id)
}

function printReport() {
  window.print()
}

onMounted(() => reportStore.fetchSummary(id))
</script>
