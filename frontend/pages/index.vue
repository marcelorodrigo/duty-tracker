<template>
  <div class="max-w-4xl mx-auto p-4">
    <h1 class="text-3xl font-bold mb-8">On-Call Hours Tracker</h1>
    <div class="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-8">
      <UCard>
        <template #header><h2 class="font-semibold text-lg">On-Call Periods</h2></template>
        <p class="text-gray-500 mb-4">Manage and calculate your on-call hours.</p>
        <UButton to="/oncall">View Periods</UButton>
      </UCard>
      <UCard>
        <template #header><h2 class="font-semibold text-lg">Reports</h2></template>
        <p class="text-gray-500 mb-4">View and export registration summaries.</p>
        <UButton to="/report">View Reports</UButton>
      </UCard>
      <UCard>
        <template #header><h2 class="font-semibold text-lg">Settings</h2></template>
        <p class="text-gray-500 mb-4">Update profile, preferences, and compensation rates.</p>
        <UButton to="/settings">Open Settings</UButton>
      </UCard>
    </div>
    <div v-if="latestPeriod" class="mb-4">
      <UCard>
        <template #header><h2 class="font-semibold">Latest On-Call Period</h2></template>
        <p>{{ latestPeriod.startDateTime }} → {{ latestPeriod.endDateTime }}</p>
        <UButton :to="`/oncall/${latestPeriod.id}`" class="mt-2">View Details</UButton>
      </UCard>
    </div>
    <UButton @click="router.push('/oncall')" color="primary" size="lg">+ New On-Call Period</UButton>
  </div>
</template>

<script setup lang="ts">
const onCallStore = useOnCallStore()
const router = useRouter()

const latestPeriod = computed(() => onCallStore.periods[0] ?? null)

onMounted(async () => {
  await onCallStore.fetchPeriods()
})
</script>
