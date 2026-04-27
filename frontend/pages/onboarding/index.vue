<template>
  <UContainer class="max-w-3xl py-12">
    <UCard>
      <template #header>
        <div>
          <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Setup Wizard</h1>
          <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">Please complete your profile to get started.</p>
        </div>
      </template>

      <UStepper :items="items" v-model="currentStep" class="w-full">
        <template #content="{ item }">
          <div class="mt-8 py-4">
            <OnboardingProfileStep v-if="item.value === 'PROFILE'" @saved="onStepSaved" />
            <OnboardingPreferencesStep v-else-if="item.value === 'PREFERENCES'" @saved="onStepSaved" />
            <OnboardingCompensationRatesStep v-else-if="item.value === 'COMPENSATION_RATES'" @saved="onStepSaved" />
            <div v-else-if="item.value === 'COMPLETE'" class="text-center py-12 flex flex-col items-center justify-center">
              <UIcon name="i-lucide-check-circle" class="w-16 h-16 text-green-500 mb-4" />
              <p class="text-lg font-medium text-green-600 dark:text-green-400">Setup complete! Redirecting...</p>
            </div>
          </div>
        </template>
      </UStepper>
    </UCard>
  </UContainer>
</template>

<script setup lang="ts">
import type { StepperItem } from '@nuxt/ui'

definePageMeta({ middleware: [] }) // Disable onboarding middleware for this page

const api = useApi()
const router = useRouter()

const stepOrder = ['PROFILE', 'PREFERENCES', 'COMPENSATION_RATES', 'COMPLETE']
const currentStep = ref('PROFILE')
const currentStepIndex = computed(() => stepOrder.indexOf(currentStep.value))

// Compute disabled states dynamically to prevent skipping ahead
const items = computed<StepperItem[]>(() => [
  {
    value: 'PROFILE',
    title: 'Profile',
    icon: 'i-lucide-user',
    disabled: currentStepIndex.value < 0
  },
  {
    value: 'PREFERENCES',
    title: 'Preferences',
    icon: 'i-lucide-settings',
    disabled: currentStepIndex.value < 1
  },
  {
    value: 'COMPENSATION_RATES',
    title: 'Compensation',
    icon: 'i-lucide-circle-dollar-sign',
    disabled: currentStepIndex.value < 2
  },
  {
    value: 'COMPLETE',
    title: 'Complete',
    icon: 'i-lucide-check-circle',
    disabled: currentStepIndex.value < 3
  }
])

onMounted(async () => {
  const status = await api.get<{ step: string; completed: boolean }>('/onboarding')
  currentStep.value = status.step
  if (status.completed) {
    router.push('/')
  }
})

async function onStepSaved() {
  try {
    const res = await api.post<{ step: string; completed: boolean }>('/onboarding', {
      currentStep: currentStep.value,
    })
    currentStep.value = res.step
    if (res.completed) {
      await router.push('/')
    }
  } catch (error) {
    // Error is already handled and displayed via useApi toast
    // This catch prevents unhandled rejection
  }
}
</script>
