<template>
  <UForm :schema="schema" :state="form" @submit="onSubmit" class="space-y-6">
    <div class="grid grid-cols-1 sm:grid-cols-2 gap-6">
      <UFormField label="Employee Type" name="employeeType" class="sm:col-span-2">
        <USelect v-model="form.employeeType" :items="employeeTypeOptions" value-attribute="value" label-attribute="label" icon="i-lucide-briefcase" class="w-full" />
      </UFormField>

      <UFormField label="Working Days" name="workingDays" class="sm:col-span-2">
        <UCheckboxGroup
          v-model="form.workingDays"
          :items="workingDaysOptions"
          orientation="horizontal"
          class="gap-4 flex-wrap"
        />
      </UFormField>

      <UFormField label="Work Start Time" name="workStartTime">
        <UInput type="time" v-model="form.workStartTime" icon="i-lucide-clock" class="w-full" />
      </UFormField>

      <UFormField label="Work End Time" name="workEndTime">
        <UInput type="time" v-model="form.workEndTime" icon="i-lucide-clock" class="w-full" />
      </UFormField>
    </div>

    <UAlert v-if="error" color="error" :description="error" class="mt-4" />

    <div class="flex justify-end pt-4 border-t border-gray-200 dark:border-gray-800">
      <UButton type="submit" :loading="loading" trailing-icon="i-lucide-arrow-right" color="primary">
        Save & Continue
      </UButton>
    </div>
  </UForm>
</template>

<script setup lang="ts">
import { z } from 'zod'

const emit = defineEmits<{ saved: [] }>()

const profileStore = useProfileStore()
const loading = ref(false)
const error = ref<string | null>(null)

const allDays = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY']

const employeeTypeOptions = [
  { value: 'INTERNAL', label: 'Internal' },
  { value: 'EXTERNAL', label: 'External' },
]

const workingDaysOptions = allDays.map(day => ({
  value: day,
  label: day.charAt(0) + day.substring(1, 3).toLowerCase(),
}))

const timeToMinutes = (timeStr: string): number => {
  const [hours, minutes] = timeStr.split(':').map(Number)
  return hours * 60 + minutes
}

const schema = z.object({
  employeeType: z.enum(['INTERNAL', 'EXTERNAL']),
  workingDays: z.array(z.string()).min(1, 'Select at least one working day'),
  workStartTime: z.string(),
  workEndTime: z.string(),
}).refine(
  (data) => timeToMinutes(data.workEndTime) > timeToMinutes(data.workStartTime),
  {
    message: 'End time must be after start time',
    path: ['workEndTime'],
  }
)

const form = reactive({
  employeeType: (profileStore.profile?.employeeType ?? 'INTERNAL') as 'INTERNAL' | 'EXTERNAL',
  workingDays: profileStore.profile?.workingDays ? [...profileStore.profile.workingDays] : ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'],
  workStartTime: profileStore.profile?.workStartTime ?? '09:00',
  workEndTime: profileStore.profile?.workEndTime ?? '17:00',
})

// Re-seed form when profile arrives (handles late profile fetch)
watch(() => profileStore.profile, (newProfile) => {
  if (newProfile) {
    form.employeeType = newProfile.employeeType
    form.workingDays = [...newProfile.workingDays]
    form.workStartTime = newProfile.workStartTime
    form.workEndTime = newProfile.workEndTime
  }
}, { deep: true })

async function onSubmit() {
  loading.value = true
  error.value = null
  try {
    if (profileStore.profile) {
      await profileStore.updateProfile(form)
    } else {
      await profileStore.createProfile(form)
    }
    emit('saved')
  } catch (e: unknown) {
    // Extract backend error details
    const problemDetail = (e as { data?: { type?: string; title?: string; detail?: string } }).data
    if (problemDetail?.detail || problemDetail?.title) {
      error.value = problemDetail.detail || problemDetail.title || 'Failed to save profile.'
    } else if (e instanceof Error) {
      error.value = e.message
    } else {
      error.value = 'Failed to save profile.'
    }
  } finally {
    loading.value = false
  }
}
</script>
