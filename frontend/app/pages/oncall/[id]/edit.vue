<script setup lang="ts">
import type { OnCallPeriodResponse } from '~/types/onCallPeriod'

useHead({ title: 'Edit on-call period' })

const route = useRoute()
const config = useRuntimeConfig()
const periodId = Number(route.params.id)

const period = ref<OnCallPeriodResponse | null>(null)
const pending = ref(false)
const loadError = ref<string | null>(null)

onMounted(async () => {
  pending.value = true
  loadError.value = null
  try {
    period.value = await $fetch<OnCallPeriodResponse>(`/api/v1/oncall-periods/${periodId}`, {
      baseURL: config.public.apiBase
    })
  } catch (err) {
    loadError.value = extractErrorDetail(err, 'Failed to load on-call period.')
  } finally {
    pending.value = false
  }
})
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
      Edit on-call period
    </template>

    <!-- Loading -->
    <div
      v-if="pending"
      class="flex justify-center py-16"
    >
      <UIcon
        name="i-lucide-loader-circle"
        class="animate-spin text-2xl text-(--ui-text-muted)"
      />
    </div>

    <!-- Error -->
    <UAlert
      v-else-if="loadError"
      color="error"
      icon="i-lucide-alert-circle"
      title="Failed to load period"
      :description="loadError"
    />

    <!-- Form -->
    <OnCallPeriodForm
      v-else-if="period"
      mode="edit"
      :period="period"
    />
  </AppPageShell>
</template>
