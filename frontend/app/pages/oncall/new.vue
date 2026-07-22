<script setup lang="ts">
useHead({ title: 'New on-call period' })
</script>

<template>
  <AppPageShell max-width="narrow">
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
      New on-call period
    </template>

    <OnCallPeriodForm mode="create" />
  </AppPageShell>
</template>
