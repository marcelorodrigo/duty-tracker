<template>
  <div class="max-w-4xl mx-auto p-4">
    <div v-if="oncallStore.currentPeriod">
      <div class="flex items-center gap-4 mb-6">
        <UButton to="/oncall" variant="ghost" icon="i-heroicons-arrow-left" />
        <h1 class="text-2xl font-bold">
          {{ formatDate(oncallStore.currentPeriod.startDateTime) }} →
          {{ formatDate(oncallStore.currentPeriod.endDateTime) }}
        </h1>
      </div>

      <!-- Holiday Overrides -->
      <UCard class="mb-6">
        <template #header><h2 class="font-semibold">Holiday Overrides</h2></template>
        <HolidayOverrideList :period="oncallStore.currentPeriod" />
      </UCard>

      <!-- Calculate button -->
      <div class="flex gap-4 mb-6">
        <UButton :loading="calculating" @click="onCalculate" color="primary">
          Calculate On-Call Hours
        </UButton>
      </div>

      <!-- Day Entries -->
      <UCard v-if="oncallStore.dayEntries.length > 0">
        <template #header><h2 class="font-semibold">Day Entries</h2></template>
        <DayEntryTable :entries="oncallStore.dayEntries" @toggleDayOff="onToggleDayOff" />
      </UCard>
      <USkeleton v-else-if="loading" class="h-32 w-full" />

      <!-- Incidents -->
      <UCard class="mt-6">
        <template #header>
          <div class="flex items-center justify-between">
            <h2 class="font-semibold">Incidents</h2>
            <UButton size="sm" @click="showIncidentModal = true">+ Add Incident</UButton>
          </div>
        </template>
        <div v-if="oncallStore.incidents.length === 0" class="text-gray-400 text-sm py-4">No incidents recorded.</div>
        <div v-for="incident in oncallStore.incidents" :key="incident.id" class="border-b last:border-0 py-3">
          <div class="flex items-center justify-between">
            <span class="font-medium">{{ incident.date }} {{ incident.startTime }}–{{ incident.endTime }}</span>
            <div class="flex gap-2">
              <UButton size="xs" :loading="calculatingOvertime[incident.id]"
                :disabled="isDayOff(incident.date)"
                @click="onCalculateOvertime(incident.id)">
                Calculate Overtime
              </UButton>
              <UAlert v-if="isDayOff(incident.date)" color="info" class="mt-1 text-xs"
                description="Time-for-time applies for this day — discuss with your manager" />
              <UButton size="xs" color="error" @click="onDeleteIncident(incident.id)">Delete</UButton>
            </div>
          </div>
          <OvertimeEntryTable
            v-if="oncallStore.overtimeEntries[incident.id]?.length"
            :entries="oncallStore.overtimeEntries[incident.id]"
            class="mt-2"
          />
        </div>
      </UCard>

      <UModal v-model:open="showIncidentModal" title="Add Incident">
        <template #body>
          <div class="p-4">
            <IncidentForm :period-id="id" :on-submit-async="handleAddIncident" />
          </div>
        </template>
      </UModal>
    </div>
    <div v-else>
      <USkeleton class="h-8 w-64 mb-4" />
      <USkeleton class="h-32 w-full" />
    </div>
  </div>
</template>

<script setup lang="ts">
const route = useRoute()
const oncallStore = useOnCallStore()
const { calculate } = useOnCallCalculation()
const { calculate: calcOvertime } = useOvertimeCalculation()
const loading = ref(false)
const calculating = ref(false)
const id = Number(route.params.id)
const showIncidentModal = ref(false)
const calculatingOvertime = ref<Record<number, boolean>>({})

function formatDate(dt: string) {
  return new Date(dt).toLocaleString('nl-NL', { dateStyle: 'medium', timeStyle: 'short' })
}

async function onCalculate() {
  calculating.value = true
  await calculate(id)
  calculating.value = false
}

async function onToggleDayOff(entryId: number, value: boolean) {
  await oncallStore.overrideDayEntry(id, entryId, { timeForTimeFlag: value })
}

function isDayOff(date: string) {
  return oncallStore.dayEntries.some(e => e.date === date && e.timeForTimeFlag)
}

async function onCalculateOvertime(incidentId: number) {
  calculatingOvertime.value[incidentId] = true
  await calcOvertime(incidentId)
  calculatingOvertime.value[incidentId] = false
}

async function handleAddIncident(data: { onCallPeriodId: number; date: string; startTime: string; endTime: string }) {
  await oncallStore.logIncident(data)
  showIncidentModal.value = false
}

async function onDeleteIncident(incidentId: number) {
  await oncallStore.deleteIncident(incidentId)
}

onMounted(async () => {
  loading.value = true
  await oncallStore.fetchPeriod(id)
  await oncallStore.listIncidents(id)
  loading.value = false
})
</script>
