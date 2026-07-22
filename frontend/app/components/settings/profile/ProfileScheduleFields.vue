<script setup lang="ts">
import { computed } from 'vue'
import { PROFILE_DAYS, PROFILE_DAY_LABELS, PROFILE_DAY_NAMES, type ProfileDay } from '~/utils/profile'

interface Props {
  workingDays: string[]
  workStartTime: string
  workEndTime: string
}

interface Emits {
  'update:workStartTime': [time: string]
  'update:workEndTime': [time: string]
  toggleDay: [day: ProfileDay]
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const startTime = computed({
  get: () => props.workStartTime,
  set: value => emit('update:workStartTime', value)
})

const endTime = computed({
  get: () => props.workEndTime,
  set: value => emit('update:workEndTime', value)
})
</script>

<template>
  <section class="space-y-5">
    <div>
      <h2 class="text-sm font-semibold text-(--ui-text)">
        Work schedule
      </h2>
      <p class="mt-0.5 text-xs text-muted">
        Your typical working days and hours, used to calculate on-call effort.
      </p>
    </div>

    <USeparator />

    <UFormField
      name="workingDays"
      label="Working days"
    >
      <template #description>
        <span id="working-days-description">Select each day you normally work.</span>
      </template>
      <div
        role="group"
        aria-label="Working days"
        aria-describedby="working-days-description"
        class="flex flex-wrap gap-2"
      >
        <button
          v-for="day in PROFILE_DAYS"
          :key="day"
          type="button"
          :aria-label="PROFILE_DAY_NAMES[day]"
          :aria-pressed="props.workingDays.includes(day)"
          aria-describedby="working-days-description"
          class="rounded-md border px-3 py-1.5 text-sm font-medium transition-colors"
          :class="props.workingDays.includes(day)
            ? 'border-(--ui-color-primary-500) text-(--ui-color-primary-500) bg-(--ui-color-primary-50) dark:bg-(--ui-color-primary-950)'
            : 'border-default text-muted hover:text-default hover:border-accented'"
          @click="emit('toggleDay', day)"
        >
          {{ PROFILE_DAY_LABELS[day] }}
        </button>
      </div>
    </UFormField>

    <div class="grid grid-cols-2 gap-4">
      <UFormField
        name="workStartTime"
        label="Start time"
        description="Your normal workday start time."
      >
        <UInput
          id="work-start-time"
          v-model="startTime"
          type="time"
        />
      </UFormField>
      <UFormField
        name="workEndTime"
        label="End time"
        description="Your normal workday end time."
      >
        <UInput
          id="work-end-time"
          v-model="endTime"
          type="time"
        />
      </UFormField>
    </div>
  </section>
</template>
