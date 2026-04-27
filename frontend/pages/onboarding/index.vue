<template>
  <div class="max-w-2xl mx-auto p-4">
    <h1 class="text-2xl font-bold mb-6">Setup Wizard</h1>

    <UStepper :items="items" v-model="currentStep" class="w-full">
      <template #content="{ item }">
        <div class="mt-8">
          <OnboardingProfileStep v-if="item.value === 'PROFILE'" @saved="onStepSaved" />
          <OnboardingPreferencesStep v-else-if="item.value === 'PREFERENCES'" @saved="onStepSaved" />
          <OnboardingCompensationRatesStep v-else-if="item.value === 'COMPENSATION_RATES'" @saved="onStepSaved" />
          <div v-else-if="item.value === 'COMPLETE'" class="text-center py-12">
            <p class="text-lg font-medium text-green-600">Setup complete! Redirecting...</p>
          </div>
        </div>
      </template>
    </UStepper>
  </div>
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
