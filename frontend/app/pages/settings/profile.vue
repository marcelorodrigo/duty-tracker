<script setup lang="ts">
import type { UpdateProfileRequest } from '~/types/profile'

const { profile, pending, error, save } = useProfile()

const DAYS_ORDER = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'] as const
const DAY_LABELS: Record<string, string> = {
  MONDAY: 'Mon',
  TUESDAY: 'Tue',
  WEDNESDAY: 'Wed',
  THURSDAY: 'Thu',
  FRIDAY: 'Fri',
  SATURDAY: 'Sat',
  SUNDAY: 'Sun'
}

// Form state — initialised from profile once loaded
const workingDays = ref<string[]>([])
const workStartTime = ref('')
const workEndTime = ref('')
const saving = ref(false)

// Sync form state when profile loads
watch(profile, (p) => {
  if (!p) return
  workingDays.value = [...p.workingDays]
  workStartTime.value = p.workStartTime.slice(0, 5)
  workEndTime.value = p.workEndTime.slice(0, 5)
}, { immediate: true })

function toggleDay(day: string) {
  const idx = workingDays.value.indexOf(day)
  if (idx >= 0) {
    workingDays.value = workingDays.value.filter(d => d !== day)
  } else {
    workingDays.value = [...workingDays.value, day]
  }
}

async function onSubmit() {
  saving.value = true
  const request: UpdateProfileRequest = {
    workingDays: DAYS_ORDER.filter(d => workingDays.value.includes(d)),
    workStartTime: workStartTime.value + ':00',
    workEndTime: workEndTime.value + ':00'
  }
  await save(request)
  saving.value = false
}
</script>

<template>
  <div>
    <div
      v-if="pending"
      class="flex justify-center py-12"
    >
      <UIcon
        name="i-lucide-loader-circle"
        class="animate-spin text-2xl text-(--ui-text-muted)"
      />
    </div>

    <UAlert
      v-else-if="error"
      color="error"
      icon="i-lucide-alert-circle"
      title="Failed to load profile"
      description="Please reload the page to try again."
    />

    <div
      v-else-if="!profile"
      class="py-12 text-center text-(--ui-text-muted)"
    >
      <p class="text-sm">
        No profile found.
      </p>
    </div>

    <form
      v-else
      class="space-y-6 max-w-md"
      @submit.prevent="onSubmit"
    >
      <!-- Working days -->
      <div>
        <label class="block text-sm font-medium mb-2">Working days</label>
        <div class="flex gap-2 flex-wrap">
          <button
            v-for="day in DAYS_ORDER"
            :key="day"
            type="button"
            class="px-3 py-1.5 rounded-md border text-sm font-medium transition-colors"
            :class="workingDays.includes(day)
              ? 'border-(--ui-color-primary-500) text-(--ui-color-primary-500) bg-(--ui-color-primary-50)'
              : 'border-(--ui-border) text-(--ui-text-muted) hover:text-(--ui-text) hover:border-(--ui-border-accented)'"
            @click="toggleDay(day)"
          >
            {{ DAY_LABELS[day] }}
          </button>
        </div>
      </div>

      <!-- Work hours -->
      <div class="flex gap-4">
        <div class="flex-1">
          <label
            class="block text-sm font-medium mb-2"
            for="work-start-time"
          >Start time</label>
          <UInput
            id="work-start-time"
            v-model="workStartTime"
            type="time"
          />
        </div>
        <div class="flex-1">
          <label
            class="block text-sm font-medium mb-2"
            for="work-end-time"
          >End time</label>
          <UInput
            id="work-end-time"
            v-model="workEndTime"
            type="time"
          />
        </div>
      </div>

      <UButton
        type="submit"
        :loading="saving"
        :disabled="saving"
        icon="i-lucide-save"
      >
        Save profile
      </UButton>
    </form>
  </div>
</template>
