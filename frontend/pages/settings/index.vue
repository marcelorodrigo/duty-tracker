<template>
  <div class="max-w-2xl mx-auto p-4">
    <h1 class="text-2xl font-bold mb-6">Settings</h1>
    <UTabs :items="tabs">
      <template #profile>
        <div class="pt-4">
          <UAlert v-if="profileStore.isLocked" color="warning" title="Profile locked"
            description="Your profile is locked because registration summaries exist." class="mb-4" />
          <ProfileStep @saved="onProfileSaved" />
        </div>
      </template>
      <template #preferences>
        <div class="pt-4">
          <PreferencesStep @saved="() => {}" />
        </div>
      </template>
      <template #compensation>
        <div class="pt-4">
          <UAlert color="warning" title="WCA Placeholder Values"
            description="Compensation percentages are placeholders (0.0000). Update them from the WCA PDF (Jumbo Logistics WCA, version P7-2025) before recording any registrations."
            class="mb-4" />
          <CompensationRatesStep @saved="() => {}" />
        </div>
      </template>
    </UTabs>
  </div>
</template>

<script setup lang="ts">
const profileStore = useProfileStore()

const tabs = [
  { label: 'Profile', slot: 'profile' },
  { label: 'Preferences', slot: 'preferences' },
  { label: 'Compensation Rates', slot: 'compensation' },
]

function onProfileSaved() {
  // Profile saved; no redirect needed from settings
}

onMounted(async () => {
  await profileStore.fetchProfile()
})
</script>
