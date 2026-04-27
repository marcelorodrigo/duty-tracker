<template>
  <div class="max-w-lg mx-auto p-4">
    <h1 class="text-2xl font-bold mb-6">Setup Wizard</h1>
    <div class="flex gap-2 mb-6">
      <span v-for="(step, i) in steps" :key="step"
        :class="['px-3 py-1 rounded-full text-sm font-medium',
          currentStepIndex === i ? 'bg-primary text-white' :
          currentStepIndex > i ? 'bg-green-500 text-white' : 'bg-gray-200 text-gray-600']">
        {{ i + 1 }}. {{ step }}
      </span>
    </div>
    <ProfileStep v-if="currentStep === 'PROFILE'" @saved="onStepSaved" />
    <PreferencesStep v-else-if="currentStep === 'PREFERENCES'" @saved="onStepSaved" />
    <CompensationRatesStep v-else-if="currentStep === 'COMPENSATION_RATES'" @saved="onStepSaved" />
    <div v-else-if="currentStep === 'COMPLETE'" class="text-center">
      <p class="text-lg font-medium text-green-600">Setup complete! Redirecting...</p>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: [] }) // Disable onboarding middleware for this page

const api = useApi()
const router = useRouter()

const stepDefs = [
  { id: 'PROFILE', label: 'Profile' },
  { id: 'PREFERENCES', label: 'Preferences' },
  { id: 'COMPENSATION_RATES', label: 'Compensation Rates' },
  { id: 'COMPLETE', label: 'Complete' },
]

const steps = stepDefs.map(s => s.label)
const stepOrder = stepDefs.map(s => s.id)
const currentStep = ref('PROFILE')
const currentStepIndex = computed(() => stepOrder.indexOf(currentStep.value))

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
