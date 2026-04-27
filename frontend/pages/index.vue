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
         <p>{{ latestPeriod.formattedStart }} → {{ latestPeriod.formattedEnd }}</p>
         <UButton :to="`/oncall/${latestPeriod.id}`" class="mt-2">View Details</UButton>
       </UCard>
     </div>
    <UButton @click="router.push('/oncall')" color="primary" size="lg">+ New On-Call Period</UButton>
  </div>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'

const onCallStore = useOnCallStore()
const router = useRouter()

const latestPeriod = computed(() => {
  if (!onCallStore.periods || onCallStore.periods.length === 0) {
    return null
  }
  
  // Defensively sort by startDateTime descending and pick the first
  const sorted = [...onCallStore.periods].sort((a, b) => 
    new Date(b.startDateTime).getTime() - new Date(a.startDateTime).getTime()
  )
  
  const period = sorted[0]
  
  // Add formatted properties
  return {
    ...period,
    formattedStart: dayjs(period.startDateTime).format('MMM DD, YYYY HH:mm'),
    formattedEnd: dayjs(period.endDateTime).format('MMM DD, YYYY HH:mm'),
  }
})

onMounted(async () => {
  await onCallStore.fetchPeriods()
})
</script>
