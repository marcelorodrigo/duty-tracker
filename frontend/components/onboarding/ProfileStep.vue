<template>
  <UForm :schema="schema" :state="form" @submit="onSubmit">
    <UFormField label="Employee Type" name="employeeType">
      <USelect v-model="form.employeeType" :items="employeeTypeOptions" value-attribute="value" label-attribute="label" />
    </UFormField>
    <UFormField label="Working Days" name="workingDays">
      <div class="flex flex-wrap gap-2">
        <label v-for="day in allDays" :key="day" class="flex items-center gap-1 cursor-pointer">
          <input type="checkbox" :value="day" v-model="form.workingDays" class="rounded" />
          {{ day.substring(0, 3) }}
        </label>
      </div>
    </UFormField>
    <UFormField label="Work Start Time" name="workStartTime">
      <UInput type="time" v-model="form.workStartTime" />
    </UFormField>
    <UFormField label="Work End Time" name="workEndTime">
      <UInput type="time" v-model="form.workEndTime" />
    </UFormField>
    <div class="mt-4">
      <UButton type="submit" :loading="loading">Save & Continue</UButton>
    </div>
    <UAlert v-if="error" color="error" :description="error" class="mt-2" />
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
  workingDays: profileStore.profile?.workingDays ?? ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'],
  workStartTime: profileStore.profile?.workStartTime ?? '09:00',
  workEndTime: profileStore.profile?.workEndTime ?? '17:00',
})

// Re-seed form when profile arrives (handles late profile fetch)
watch(() => profileStore.profile, (newProfile) => {
  if (newProfile) {
    form.employeeType = newProfile.employeeType
    form.workingDays = newProfile.workingDays
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
    error.value = 'Failed to save profile.'
  } finally {
    loading.value = false
  }
}
</script>
