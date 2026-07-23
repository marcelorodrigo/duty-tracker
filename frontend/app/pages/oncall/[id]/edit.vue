<script setup lang="ts">
import type { OnCallPeriodResponse } from '~/types/onCallPeriod'

useHead({ title: 'Edit on-call period' })

const route = useRoute()
const { $api } = useNuxtApp()
const periodId = Number(route.params.id)

const period = ref<OnCallPeriodResponse | null>(null)
const pending = ref(false)
const loadError = ref<string | null>(null)

onMounted(async () => {
  pending.value = true
  loadError.value = null
  try {
    period.value = await $api.get<OnCallPeriodResponse>(`/oncall-periods/${periodId}`)
  } catch (err) {
    loadError.value = getApiErrorMessage(err)
  } finally {
    pending.value = false
  }
})
</script>

<template>
  <UContainer>
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
    <div
      v-else-if="loadError"
      class="py-6 max-w-3xl mx-auto"
    >
      <div class="flex items-center gap-2 mb-6">
        <NuxtLink to="/">
          <UButton
            icon="i-lucide-arrow-left"
            variant="ghost"
            color="neutral"
            aria-label="Back to periods"
          />
        </NuxtLink>
        <h1 class="text-2xl font-semibold">
          Edit on-call period
        </h1>
      </div>
      <UAlert
        color="error"
        icon="i-lucide-alert-circle"
        title="Failed to load period"
        :description="loadError"
      />
    </div>

    <!-- Form -->
    <OnCallPeriodForm
      v-else-if="period"
      mode="edit"
      :period="period"
    />
  </UContainer>
</template>
