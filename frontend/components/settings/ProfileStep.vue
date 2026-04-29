<template>
  <UForm :schema="schema" :state="form" @submit="onSubmit" class="space-y-6">
    <div class="grid grid-cols-1 sm:grid-cols-2 gap-6">
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

    <div class="flex justify-end pt-4 border-t border-gray-200 dark:border-gray-800">
      <UButton type="submit" color="primary">
        Save
      </UButton>
    </div>
  </UForm>
</template>

<script setup lang="ts">
import { z } from 'zod'

const profileStore = useProfileStore()
const toast = useToast()

const allDays = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY']

const workingDaysOptions = allDays.map(day => ({
  value: day,
  label: day.charAt(0) + day.substring(1, 3).toLowerCase(),
}))

const timeToMinutes = (timeStr: string): number => {
  const [hours, minutes] = timeStr.split(':').map(Number)
  return hours * 60 + minutes
}

const schema = z.object({
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
  workingDays: profileStore.profile?.workingDays ? [...profileStore.profile.workingDays] : ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'],
  workStartTime: profileStore.profile?.workStartTime ?? '09:00',
  workEndTime: profileStore.profile?.workEndTime ?? '17:00',
})

// Re-seed form when profile arrives (handles late profile fetch)
watch(() => profileStore.profile, (newProfile) => {
  if (newProfile) {
    form.workingDays = [...newProfile.workingDays]
    form.workStartTime = newProfile.workStartTime
    form.workEndTime = newProfile.workEndTime
  }
}, { deep: true })

async function onSubmit() {
  try {
    if (profileStore.profile) {
      await profileStore.updateProfile(form)
    } else {
      await profileStore.createProfile(form)
    }
    toast.add({ title: 'Profile saved', color: 'success' })
  } catch (e: unknown) {
    const problemDetail = (e as { data?: { type?: string; title?: string; detail?: string } }).data
    const message = problemDetail?.detail || problemDetail?.title || (e instanceof Error ? e.message : 'Failed to save profile.')
    toast.add({ title: 'Failed to save profile', description: message, color: 'error' })
  }
}
</script>
